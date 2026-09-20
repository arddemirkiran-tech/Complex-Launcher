/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.mods;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.ProgressListener;
import com.lubv.launcher.mods.CurseForgeApi;
import com.lubv.launcher.mods.ModrinthApi;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static File registryFile(File modsDir) {
        return new File(modsDir, "installed_mods.json");
    }

    public static List<InstalledMod> loadRegistry(File modsDir) {
        File f = ModManager.registryFile(modsDir);
        if (!f.exists()) {
            return new ArrayList<InstalledMod>();
        }
        try (FileReader r = new FileReader(f)) {
            List<InstalledMod> list2 = (List<InstalledMod>)GSON.fromJson((Reader)r, new TypeToken<List<InstalledMod>>(){}.getType());
            return list2 != null ? list2 : new ArrayList<InstalledMod>();
        }
        catch (Exception e) {
            // Bozuk registry (yarida kesilen yazim, disk dolu, crash): dosyayi
            // .corrupt olarak yedekle ki ayni kirli durum her acilista tekrar
            // tekrar sessizce bosa donmesin; jar dosyalari diskte duruyor,
            // kullanici refreshInstalled ile yeniden tarayabilir.
            try {
                java.nio.file.Files.move(f.toPath(),
                    new File(modsDir, "installed_mods.json.corrupt").toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            catch (Exception ignored) {
            }
            return new ArrayList<InstalledMod>();
        }
    }

    public static void saveRegistry(File modsDir, List<InstalledMod> mods) {
        modsDir.mkdirs();
        // V36.2 DUPE KORUMASI: ayni projectId icin birden fazla kayit
        // (farkli dosya adiyla dupelik) varsa yalnizca EN GUNCEL kayit
        // kalir; digerlerinin jar dosyalari diskten silinir.
        mods = dedupeByProject(modsDir, mods);
        // Atomic yazim: once .tmp'ye yaz, sonra move ile kesinlestir. Yarida
        // kesilen yazim (crash, guc kesintisi) artik installed_mods.json'u
        // bozamaz - ya eski dosya kalir ya da tamamen yazilmis yenisi gelir.
        File tmp = new File(modsDir, "installed_mods.json.tmp");
        try (FileWriter w = new FileWriter(tmp);) {
            GSON.toJson(mods, (Appendable)w);
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            java.nio.file.Files.move(tmp.toPath(), ModManager.registryFile(modsDir).toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        }
        catch (IOException e) {
            try {
                java.nio.file.Files.move(tmp.toPath(), ModManager.registryFile(modsDir).toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public static void installMod(File modsDir, ModrinthApi.ModResult mod, ModrinthApi.ModVersion version) throws IOException {
        ModManager.installMod(modsDir, mod, version, null);
    }

    public static void installMod(File modsDir, ModrinthApi.ModResult mod, ModrinthApi.ModVersion version, HttpUtil.ByteProgress progress) throws IOException {
        ModManager.installMod(modsDir, mod, version, progress, null);
    }

    public static void installMod(File modsDir, ModrinthApi.ModResult mod, ModrinthApi.ModVersion version, HttpUtil.ByteProgress progress, java.util.concurrent.atomic.AtomicBoolean cancelled) throws IOException {
        File dest = new File(modsDir, version.fileName);
        // SHA-1 dogrulamali indirme: Modrinth her dosyanin hash'ini verir.
        // Kopuk/bozuk indirme 2 kez otomatik denenir, hala bozuksa IOException
        // firlatir - yarim jar bir daha asla "Kuruldu" diye kaydedilmez.
        HttpUtil.downloadFileVerified(version.downloadUrl, dest, progress, version.sha1, cancelled);
        List<InstalledMod> mods = ModManager.loadRegistry(modsDir);
        mods.removeIf(m -> m.projectId != null && m.projectId.equals(mod.id));
        InstalledMod im = new InstalledMod();
        im.fileName = version.fileName;
        im.projectId = mod.id;
        im.projectTitle = mod.title;
        im.versionNumber = version.versionNumber;
        im.iconUrl = mod.iconUrl;
        mods.add(im);
        ModManager.saveRegistry(modsDir, mods);
    }

    public static synchronized InstallResult installWithDependencies(File modsDir, ModrinthApi.ModResult mod, ModrinthApi.ModVersion version, String loader, String mcVersion) throws IOException {
        return ModManager.installWithDependencies(modsDir, mod, version, loader, mcVersion, null);
    }

    public static synchronized InstallResult installWithDependencies(File modsDir, ModrinthApi.ModResult mod, ModrinthApi.ModVersion version, String loader, String mcVersion, ProgressListener progress) throws IOException {
        return ModManager.installWithDependencies(modsDir, mod, version, loader, mcVersion, progress, null);
    }

    public static synchronized InstallResult installWithDependencies(File modsDir, ModrinthApi.ModResult mod, ModrinthApi.ModVersion version, String loader, String mcVersion, ProgressListener progress, java.util.concurrent.atomic.AtomicBoolean cancelled) throws IOException {
        InstallResult result = new InstallResult();
        long epoch = mcSwitchEpoch.get();
        ModManager.checkEpoch(epoch);
        // V30 KOK COZUM - ZINCIR GIRISI DOGRULAMASI: bu metoda gelen surumun
        // gercekten hedef MC surumunu destekledigi BURADA kesinlestirilir.
        // Eskiden panelden/otomatik kurulumdan gelen hazir surum nesnesine
        // guveniliyordu; yanlis MC surumu icin derlenmis bir jar elimden
        // her yola girebiliyordu. Artik uyusmazlikta KURULUM REDDEDILIR.
        String effMc = ModrinthApi.effectiveMcVersion(mcVersion);
        if (effMc != null && (version.gameVersions == null || !version.gameVersions.contains(effMc))) {
            throw new IOException("Surum uyusmazligi: " + mod.title + " " + version.versionNumber + " -> MC " + effMc + " desteklemiyor, kurulum reddedildi");
        }
        HashSet<String> visited = new HashSet<String>();
        visited.add(mod.id);
        ModManager.report(progress, 0, "Mod indiriliyor: " + mod.title);
        ModManager.installMod(modsDir, mod, version, (done, total) -> ModManager.report(progress, total > 0L ? (int)(done * 80L / total) : -1, "Mod indiriliyor: " + mod.title), cancelled);
        for (ModrinthApi.Dependency d : version.dependencies) {
            ModManager.checkEpoch(epoch);
            ModManager.installDependencyRecursive(modsDir, d, loader, mcVersion, visited, result, progress, cancelled);
        }
        ModManager.checkEpoch(epoch);
        ModManager.report(progress, 100, "Kurulum tamamland\u0131");
        return result;
    }

    private static void installDependencyRecursive(File modsDir, ModrinthApi.Dependency d, String loader, String mcVersion, Set<String> visited, InstallResult result, ProgressListener progress, java.util.concurrent.atomic.AtomicBoolean cancelled) throws IOException {
        if (d == null || !"required".equals(d.type)) {
            return;
        }
        // V32: parseVersion artik version_id'den project_id'yi cozer; burada
        // yine de null kaldiysa dep kaydi bostur (cozulemedi) - skipped yaz.
        if (d.projectId == null) {
            result.skippedDependencies.add(d.versionId != null ? "version:" + d.versionId : "bilinmeyen-bagimlilik");
            return;
        }
        if (cancelled != null && cancelled.get()) {
            throw new IOException("Iptal edildi");
        }
        String projectId = d.projectId;
        if (!visited.add(projectId)) {
            return;
        }
        ModrinthApi.ModVersion v = ModManager.resolveDependencyVersion(d, loader, mcVersion);
        if (v == null) {
            // Artik sessiz degil: hicbir surum cozulemediyse skippd listesine
            // yazilir, UI kullaniciya "X surumu bulunamadi" raporu gosterir.
            ModrinthApi.ModResult projForName = ModrinthApi.getProject(d.projectId);
            result.skippedDependencies.add(projForName != null && projForName.title != null ? projForName.title : d.projectId);
            return;
        }
        String title = ModManager.resolveTitle(projectId);
        // V35.3: secilen dep surumu hedef MC'yi (birebir veya ayni minor cizgi)
        // desteklemiyorsa kuruluma GIRMEDEN raporla - ensureVersion'in firlat
        // gisi tum zinciri dusurmesin, dep "atlandi" olarak listelensin.
        if (!ModrinthApi.versionSupportsMc(v, mcVersion)) {
            result.skippedDependencies.add(title + " (MC " + mcVersion + " uyumsuz)");
            return;
        }
        ModManager.report(progress, -1, "Gerekli mod kuruluyor: " + title);
        ChangeType change = ModManager.ensureVersion(modsDir, projectId, v, (done, total) -> ModManager.report(progress, total > 0L ? 80 + (int)(done * 20L / total) : -1, "Gerekli mod kuruluyor: " + title), cancelled, mcVersion);
        if (change != ChangeType.NONE) {
            result.installedDependencies.add(title);
        }
        for (ModrinthApi.Dependency sub : v.dependencies) {
            ModManager.installDependencyRecursive(modsDir, sub, loader, mcVersion, visited, result, progress, cancelled);
        }
    }

    private static String resolveTitle(String projectId) {
        ModrinthApi.ModResult proj = ModrinthApi.getProject(projectId);
        if (proj != null && proj.title != null && !proj.title.isBlank()) {
            return proj.title;
        }
        return projectId;
    }

    private static void report(ProgressListener p, int percent, String stage) {
        if (p != null) {
            p.onProgress(percent, stage);
        }
    }

    /**
     * Bagimlilik surumu cozumlemede %100 kapsama icin 3 katman:
     *  1) pinli versionId (Modrinth'in verdiği birebir surum)
     *  2) filtreli liste (loader+mc surumu facet'li) -> en iyi surum
     *  3) FILTRESIZ liste -> tum surumler + CLIENT-SIDE uyumluluk suzgeci
     *     (dep yeni MC surumu icin build edilmemisse katman 2 bos doner ve
     *     dep SESSIZCE ATLANIYORDU -> oyun icinde mod yuklenmezdi. Katman 3
     *     gameVersions/loaders alanlarindan en yakin uyumlu surumu secer.)
     */
    private static ModrinthApi.ModVersion resolveDependencyVersion(ModrinthApi.Dependency d, String loader, String mcVersion) {
        ModrinthApi.ModVersion v = ModManager.resolveDependencyVersionOnce(d, loader, mcVersion, false);
        if (v != null) {
            return v;
        }
        // Tek seferlik taze deneme: cache doldugu anda gecici bir API hatasi
        // olmus olabilir -> cache'i bypass edip tum katmanlari bir kez daha dene.
        try {
            Thread.sleep(400L);
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null;
        }
        return ModManager.resolveDependencyVersionOnce(d, loader, mcVersion, true);
    }

    private static ModrinthApi.ModVersion resolveDependencyVersionOnce(ModrinthApi.Dependency d, String loader, String mcVersion, boolean forceFresh) {
        String effMc = ModrinthApi.effectiveMcVersion(mcVersion);
        if (d.versionId != null && !d.versionId.isBlank()) {
            try {
                ModrinthApi.ModVersion pinned = ModrinthApi.getVersion(d.versionId);
                // V30: pinli surum de DOGRULANIR - Modrinth bazi dep'leri baska
                // MC surumu icin pinlemis olabilir; yanlis jar artikkace girmez,
                // yerine ayni dep'nin dogru surumu cozulur (asagi dus).
                if (effMc == null || (pinned.gameVersions != null && pinned.gameVersions.contains(effMc))) {
                    return pinned;
                }
            }
            catch (IOException iOException) {
                // pinned version fetch failed -> fall through to list resolution
            }
        }
        String safeMc = ModrinthApi.looksLikeMcVersion(mcVersion) ? mcVersion : null;
        try {
            List<ModrinthApi.ModVersion> versions = ModManager.getVersionsMaybeCached(d.projectId, loader, safeMc, forceFresh);
            // V29.7: pickBestVersion artik mcVersion biliniyorsa SADECE o
            // surumu destekleyen adaylar arasindan secer; yoksa null.
            ModrinthApi.ModVersion best = ModrinthApi.pickBestVersion(versions, safeMc);
            if (best != null) {
                return best;
            }
        }
        catch (IOException e) {
            // filtered lookup failed -> fall through to unfiltered
        }
        // Katman 3: filtresiz liste + client-side uyumluluk suzgeci (artik
        // o SNK surum cizgisine uymayan adaylari da reddeder).
        try {
            List<ModrinthApi.ModVersion> all = ModManager.getVersionsMaybeCached(d.projectId, null, null, forceFresh);
            return ModrinthApi.pickBestCompatible(all, loader, safeMc);
        }
        catch (IOException e) {
            return null;
        }
    }

    /**
     * Launch hizini arttirmak icin: auto-install her baslatmada ayni
     * (slug,loader,mc) icin Modrinth API'sini tekrar tekrar dopuyor.
     * 10 dakikalik cache ile tekrar baslatmalarda hic API cagrisi yapilmaz.
     */
    private static final long VERSION_CACHE_TTL_MS = 10L * 60 * 1000;
    private static final long EMPTY_CACHE_TTL_MS = 15L * 1000;
    private static final java.util.Map<String, java.util.List<ModrinthApi.ModVersion>> versionCache = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<String, Long> versionCacheTime = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * V35.3 YARIS KORUMASI: kullanici MC surumunu degistirdiginde (autoUpdate
     * thread'i devam ederken) ESKI surum icin baslatilan install/ensure
     * islemleri bitebilir ve yanlis surumlu jar'i yeniden indirebilir. Her
     * surum degisiminde epoch artar; burada epoch eski ise islem HEMEN
     * iptal edilir (IOException - "surum degisti" mesaji). degisim noktasi
     * (a) kurulumdan ONCE kontrol, (b) registry-yazma sirasinda tekrar kontrol.
     */
    private static final java.util.concurrent.atomic.AtomicLong mcSwitchEpoch = new java.util.concurrent.atomic.AtomicLong(0);

    /** Kullanicinin MC surumu degistiginde MainWindow'dan cagrilir. */
    public static void onMcVersionSwitched() {
        mcSwitchEpoch.incrementAndGet();
        // 10dk cache'i de dusur: eski surumun version listesi yenide kullanilmasin.
        versionCache.clear();
        versionCacheTime.clear();
    }

    /** Aktif surum-epoch'u (Max-Perf dongusu yarıs korumasi icin). */
    public static long epochNow() {
        return mcSwitchEpoch.get();
    }

    private static void checkEpoch(long captured) throws IOException {
        if (captured != mcSwitchEpoch.get()) {
            throw new IOException("MC surumu degisti - eski surum icin kurulum iptal edildi");
        }
    }

    static List<ModrinthApi.ModVersion> getVersionsMaybeCached(String projectId, String loader, String mcVersion, boolean forceFresh) throws IOException {
        if (forceFresh) {
            // Cache'i bypass et: taze API cagrisi (gecici hata sonrasi ikinci sans).
            return ModrinthApi.getVersionsRetry(projectId, loader, mcVersion);
        }
        return ModManager.getCachedVersions(projectId, loader, mcVersion);
    }

    static List<ModrinthApi.ModVersion> getCachedVersions(String projectId, String loader, String mcVersion) throws IOException {
        String key = projectId + "|" + loader + "|" + mcVersion;
        java.util.List<ModrinthApi.ModVersion> cached = versionCache.get(key);
        Long at = versionCacheTime.get(key);
        if (cached != null && at != null && System.currentTimeMillis() - at < VERSION_CACHE_TTL_MS) {
            // V32: bos sonucu kisa sureligine cache'le (rate-limit korumasi);
            // TTL sonunda gercek liste tekrar cekilir. Eskiden bos liste
            // tam TTL boyunca cache'leniyor ve sonraki kurulum denemeleri de
            // bos donuyordu.
            if (cached.isEmpty() && System.currentTimeMillis() - at < EMPTY_CACHE_TTL_MS) {
                return cached;
            }
            if (!cached.isEmpty()) {
                return cached;
            }
        }
        List<ModrinthApi.ModVersion> fresh = ModrinthApi.getVersions(projectId, loader, mcVersion);
        versionCache.put(key, fresh);
        versionCacheTime.put(key, System.currentTimeMillis());
        return fresh;
    }

    /**
     * V34.8 HIZ: Modrinth version listelerini one tuslemek icin arka plan
     * istegi. Maks Performans kurulumu gibi ard arda cok sayida mod
     * kurulacanda her modun version listesi TEK TEK beklenerek cekilirdi
     * (her biri ~150-300ms RTT). Bu yordam tum slugin listesini PARALEL
     * istekle cache'e doldurur; installProject sonrasinda getCachedVersions
     * ag beklemeden done. Hata/bos sonuclar cache'e YAZILMAZ (kurulumun
     * kendi yolu tekrar dener); yani dogruluk korunur, sadece hiz kazanilir.
     */
    public static void warmVersionCacheAsync(java.util.List<String> projectIds, String loader, String mcVersion) {
        if (projectIds == null || projectIds.isEmpty()) {
            return;
        }
        Thread t = new Thread(() -> {
            java.util.concurrent.Semaphore gate = new java.util.concurrent.Semaphore(4);
            java.util.List<Thread> workers = new java.util.ArrayList<>();
            for (String pid : projectIds) {
                if (pid == null || pid.isBlank()) continue;
                String key = pid + "|" + loader + "|" + mcVersion;
                java.util.List<ModrinthApi.ModVersion> cached = versionCache.get(key);
                Long at = versionCacheTime.get(key);
                if (cached != null && at != null && !cached.isEmpty() && System.currentTimeMillis() - at < VERSION_CACHE_TTL_MS) {
                    continue; // zaten sicak
                }
                Runnable job = () -> {
                    try {
                        gate.acquire();
                        try {
                            java.util.List<ModrinthApi.ModVersion> fresh = ModrinthApi.getVersions(pid, loader, mcVersion);
                            if (fresh != null && !fresh.isEmpty()) {
                                versionCache.put(key, fresh);
                                versionCacheTime.put(key, System.currentTimeMillis());
                            }
                        }
                        finally {
                            gate.release();
                        }
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    catch (Exception ignored) {
                        // on-yukleme hatasi cache'i kirletmez
                    }
                };
                Thread w = new Thread(job, "modrinth-warm-" + pid);
                w.setDaemon(true);
                workers.add(w);
                w.start();
            }
            for (Thread w : workers) {
                try {
                    w.join(4000); // max 4sn bekle; kalanlar arkada devam eder
                }
                catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "modrinth-cache-warm");
        t.setDaemon(true);
        t.start();
    }

    /**
     * V35 OTO-KURULUM GUVENLIGI: oto-kurulum anahtarlari (Ayarlar'daki
     * "Baslarken Otomatik Kurulacaklar" kutulari) KAPALI iken bu modlar
     * HICBIR otomatik yoldan kurulamaz. installProject tum oto-kurulum
     * akislarinin (loader kurulumu, baslatma, surum guncelleme) ortak
     * giris noktasidir; burada son savunma hattini kurariz. Manuel
     * panel kurulumlari (installVersion/ensureVersion/installExternal)
     * bu kapidan gectigi icin etkilenmez.
     *
     * NOT: iris/sodium/oculus/embeddium ciftleri birbirinin cagrilarina
     * bagimlidir (kullanici ikisini de secenekte aciksa pair kurulur);
     * pref kontrolu anahtar basina yapilir, ikisi de acik degilse
     * cagiran katman zaten tek-mod yolunu secer.
     */
    private static final java.util.Set<String> AUTO_INSTALL_PREF_KEYS = new java.util.HashSet<>(java.util.Arrays.asList(
        "sodium", "iris", "vulkanmod", "embeddium", "oculus", "bsl", "complementary",
        "jei", "journeymap", "replaymod", "amusemod", "tweakeroo"
    ));

    private static volatile java.util.function.BiPredicate<String, String> autoInstallGate = null;

    // V36.2: kullanacinin oto-kurulum listesinde TIKINI KALDIRDIGI anahtarlar.
    // Maks Performans acik olsa bile bu anahtarlar otomatik KURULAMAZ -
    // "sodium'u kapatmistim ama yine kuruldu" bugunun kok cözümü.
    private static volatile java.util.Set<String> userOptedOut = java.util.Collections.emptySet();

    /** MainWindow, oto-kurulum kutularindaki KAPALI anahtarlari bildirir. */
    public static void setUserOptedOutKeys(java.util.Set<String> keys) {
        userOptedOut = keys == null ? java.util.Collections.emptySet() : keys;
    }

    public static java.util.Set<String> getUserOptedOutKeys() {
        return userOptedOut;
    }

    /** UI katmani (MainWindow) bu yontemle kapı saglar: (prefKey, modsDirPath) -> izinli mi. */
    public static void setAutoInstallGate(java.util.function.BiPredicate<String, String> gate) {
        autoInstallGate = gate;
    }

    private static boolean autoInstallAllowed(String projectId, File modsDir) {
        if (projectId == null || modsDir == null) {
            return true;
        }
        String p = projectId.toLowerCase();
        String prefKey = null;
        for (String k : AUTO_INSTALL_PREF_KEYS) {
            if (p.equals(k) || p.contains(k)) {
                prefKey = k;
                break;
            }
        }
        if (prefKey == null) {
            return true; // oto-kurulum listesinde yok -> serbest
        }
        // V36.2 KOK COZUM: kullanici bu modu oto-kurulumdan cikarmissa
        // (tiki kapali) Maks Performans dahil HICBIR otomatik yol kuramaz.
        if (userOptedOut.contains(prefKey)) {
            return false;
        }
        var g = autoInstallGate;
        return g != null && g.test(prefKey, modsDir.getAbsolutePath());
    }

    private static volatile boolean forceInstallBypass = false;

    /**
     * V39.1: Maks Performans kurulum dongusu icin bypass. Bu acikken
     * installProject oto-kurulum kapisi + opt-out listesini atlar — cunku
     * kullanicinin acik istegi "Maks Performans acinca tum performans
     * modlari (sodium/iris/sodium-extra/more-culling...) kurulsun".
     * Sadece bu dongunun thread'i bu modu set eder; diger otomatik
     * yollar (surum guncellemesi vb.) etkilenmez.
     */
    public static void setForceInstallBypass(boolean on) {
        forceInstallBypass = on;
    }

    public static boolean isForceInstallBypass() {
        return forceInstallBypass;
    }

    public static synchronized boolean installProject(File modsDir, String projectId, String loader, String mcVersion) throws IOException {
        // V35: oto-kurulum anahtari kapaliysa bu mod kurulamaz (bug fix:
        // "secmedigim halde Sodium/Iris/Embeddium/Oculus kurulu").
        // V39.1: Maks Performans dongusunden cagrildiysa bypass gecerli.
        if (!forceInstallBypass && !autoInstallAllowed(projectId, modsDir)) {
            return false;
        }
        List<InstalledMod> mods = ModManager.loadRegistry(modsDir);
        for (InstalledMod m : mods) {
            if (!projectId.equals(m.projectId)) continue;
            return false;
        }
        List<ModrinthApi.ModVersion> versions = ModManager.getCachedVersions(projectId, loader, mcVersion);
        if (versions.isEmpty()) {
            return false;
        }
        // V29.7: auto-install yolu da siki surum gecidinden gecer - MC
        // surumu biliniyorsa baska surum icin derlenmis jar kurulamaz.
        ModrinthApi.ModVersion best = ModrinthApi.pickBestVersion(versions, ModrinthApi.looksLikeMcVersion(mcVersion) ? mcVersion : null);
        if (best == null) {
            return false; // o MC surumu icin surum yok - yanlis surum kurma
        }
        ModrinthApi.ModResult proj = ModrinthApi.getProject(projectId);
        if (proj == null) {
            // Proje bilgisi alinamadiysa en azindan ana modu kur, bagimlilik
            // cozumleme icin proje objesine ihtiyac var.
            ModManager.installVersion(modsDir, projectId, best);
            return true;
        }
        // Onceden sadece ana mod indirilip bagimliliklar (fabric-api,
        // architectury, cloth-config vb.) kurulmuyordu - bu da modun
        // oyun icinde hic yuklenmemesine (dolayisiyla "kurulmuyor" hissine)
        // yol aciyordu. Artik gerekli tum bagimliliklar da recursive
        // olarak kuruluyor.
        ModManager.installWithDependencies(modsDir, proj, best, loader, mcVersion);
        return true;
    }

    private static void installVersion(File modsDir, String projectId, ModrinthApi.ModVersion version) throws IOException {
        ModManager.installVersion(modsDir, projectId, version, null);
    }

    private static void installVersion(File modsDir, String projectId, ModrinthApi.ModVersion version, HttpUtil.ByteProgress progress) throws IOException {
        ModManager.installVersion(modsDir, projectId, version, progress, null);
    }

    private static void installVersion(File modsDir, String projectId, ModrinthApi.ModVersion version, HttpUtil.ByteProgress progress, java.util.concurrent.atomic.AtomicBoolean cancelled) throws IOException {
        long epoch = mcSwitchEpoch.get();
        // V35.3: indirmeden ONCE de kontrol - eski surum icin ag trafigi bile yapma.
        ModManager.checkEpoch(epoch);
        ModrinthApi.ModResult proj = ModrinthApi.getProject(projectId);
        File dest = new File(modsDir, version.fileName);
        HttpUtil.downloadFileVerified(version.downloadUrl, dest, progress, version.sha1, cancelled);
        // V35.3: indirme sirasinda surum degistiysa jar'i registry'ye YAZMA -
        // eski surumun dosyasi diskte kalip yeni surumle cakisirdi.
        ModManager.checkEpoch(epoch);
        List<InstalledMod> mods = ModManager.loadRegistry(modsDir);
        // V29.6: ayni projenin ESKI dosyalarini da diskten sil. Onceden
        // sadece registry kaydi siliniyordu; eski jar farkli isimliyse
        // (surum numarasi isimde) diskte kalir ve oyun iki kopyayi yukleyip
        // cakismadir. keepFileName (yeni indirilen) haric tutulur.
        ModManager.removeProjectFiles(modsDir, mods, projectId, version.fileName);
        mods.removeIf(m -> m.projectId != null && m.projectId.equals(projectId));
        InstalledMod im = new InstalledMod();
        im.fileName = version.fileName;
        im.projectId = projectId;
        im.projectTitle = proj != null ? proj.title : projectId;
        im.versionNumber = version.versionNumber;
        im.iconUrl = proj != null ? proj.iconUrl : null;
        mods.add(im);
        ModManager.saveRegistry(modsDir, mods);
    }

    public static synchronized PairResult installShaderPair(File modsDir, String shaderProjectId, String baseProjectId, String loader, String mcVersion) throws IOException {
        PairResult result = new PairResult();
        ModrinthApi.ModVersion shader = ModrinthApi.pickBestVersion(ModManager.getCachedVersions(shaderProjectId, loader, mcVersion), ModrinthApi.looksLikeMcVersion(mcVersion) ? mcVersion : null);
        if (shader == null) {
            throw new IOException("Shader surumu bulunamadi: " + shaderProjectId + " (MC " + mcVersion + ")");
        }
        ModrinthApi.ModVersion base = null;
        if (shader != null) {
            String baseVersionId = null;
            for (ModrinthApi.Dependency d : shader.dependencies) {
                if (!"required".equals(d.type) || !baseProjectId.equals(d.projectId)) continue;
                baseVersionId = d.versionId;
                break;
            }
            if (baseVersionId != null) {
                try {
                    base = ModrinthApi.getVersion(baseVersionId);
                    // V35.3: pinli base de dogrulanir - farkli MC surumu icin
                    // ise ayni yolu düs, filtreli listeden dogru surumu sec.
                    if (!ModrinthApi.versionSupportsMc(base, mcVersion)) {
                        base = null;
                    }
                }
                catch (IOException e) {
                    base = null;
                }
            }
            if (base == null) {
                base = ModrinthApi.pickBestVersion(ModManager.getCachedVersions(baseProjectId, loader, mcVersion), ModrinthApi.looksLikeMcVersion(mcVersion) ? mcVersion : null);
            }
        } else {
            base = ModrinthApi.pickBestVersion(ModManager.getCachedVersions(baseProjectId, loader, mcVersion), ModrinthApi.looksLikeMcVersion(mcVersion) ? mcVersion : null);
        }
        if (shader != null) {
            result.shader = ModManager.ensureVersion(modsDir, shaderProjectId, shader, mcVersion);
        }
        if (base != null) {
            result.base = ModManager.ensureVersion(modsDir, baseProjectId, base, mcVersion);
        }
        return result;
    }

    public static boolean isInstalled(File modsDir, String projectId) {
        return ModManager.hasProject(ModManager.loadRegistry(modsDir), projectId);
    }

    private static boolean hasProject(List<InstalledMod> mods, String projectId) {
        for (InstalledMod m : mods) {
            if (!projectId.equals(m.projectId)) continue;
            return true;
        }
        return false;
    }

    private static ChangeType ensureVersion(File modsDir, String projectId, ModrinthApi.ModVersion version, String mcVersion) throws IOException {
        return ModManager.ensureVersion(modsDir, projectId, version, null, null, mcVersion);
    }

    private static ChangeType ensureVersion(File modsDir, String projectId, ModrinthApi.ModVersion version, HttpUtil.ByteProgress progress, String mcVersion) throws IOException {
        return ModManager.ensureVersion(modsDir, projectId, version, progress, null, mcVersion);
    }

    private static ChangeType ensureVersion(File modsDir, String projectId, ModrinthApi.ModVersion version, HttpUtil.ByteProgress progress, java.util.concurrent.atomic.AtomicBoolean cancelled, String mcVersion) throws IOException {
        List<InstalledMod> mods = ModManager.loadRegistry(modsDir);
        // V35.3 SIKI GECIT: bagimlilik/zincir/pinli kurulumlarindan gelen
        // surum hedef MC'yi desteklemiyorsa REDDET. Eskiden ensureVersion
        // dogrulama yapmiyordu - pinned dep farkli MC surumu icinse bile
        // sessizce kuruluyor ve oyun icinde cakisiyordu.
        if (!ModrinthApi.versionSupportsMc(version, mcVersion)) {
            throw new IOException("Surum uyusmazligi: " + projectId + " " + version.versionNumber + " -> MC " + (mcVersion == null ? "?" : mcVersion) + " desteklemiyor");
        }
        boolean correct = false;
        boolean hadAny = false;
        for (InstalledMod m : mods) {
            if (!projectId.equals(m.projectId)) continue;
            hadAny = true;
            if (!version.fileName.equals(m.fileName)) continue;
            correct = true;
        }
        if (correct) {
            return ChangeType.NONE;
        }
        ArrayList<InstalledMod> snapshot = new ArrayList<InstalledMod>(mods);
        for (InstalledMod m : snapshot) {
            if (!projectId.equals(m.projectId)) continue;
            ModManager.removeMod(modsDir, m);
        }
        ModManager.installVersion(modsDir, projectId, version, progress, cancelled);
        return hadAny ? ChangeType.UPDATED : ChangeType.INSTALLED;
    }

    /**
     * V29.6: verilen projenin eski dosyalarini diskten siler (registry'de
     * kayitli olanlardan keepFileName haric olanlari; aktif ve .disabled
     * varyantlariyla). Yeni surum kurulmadan/registry guncellenmeden once
     * cagrilir - boylece ayni modun iki farkli isimli jar'i bir arada
     * kalamaz (cift yukleme = ClassFormatError/crash).
     */
    public static void removeProjectFiles(File modsDir, List<InstalledMod> registry, String projectId, String keepFileName) {
        Set<String> toDelete = new HashSet<String>();
        for (InstalledMod m : registry) {
            if (m == null || m.projectId == null || !m.projectId.equals(projectId) || m.fileName == null) continue;
            if (m.fileName.equals(keepFileName)) continue;
            toDelete.add(m.fileName);
        }
        for (String f : toDelete) {
            new File(modsDir, f).delete();
            new File(modsDir, f + ".disabled").delete();
        }
    }

    /**
     * Kurulu modlar arasinda verilen anahtar kelimeleri (dosya adi + proje
     * adi, kucuk harf) iceren CAKISAN modlari dondurur. "Maks Performans"
     * acilirken cagrilir: bu modlar performans setiyle cakisir, oyundan
     * once silinmeleri teklif edilir. Hem aktif hem .disabled jar'lar taranir.
     */
    public static List<InstalledMod> findConflictingMods(File modsDir, List<String> keywords) {
        List<InstalledMod> conflicts = new ArrayList<InstalledMod>();
        for (InstalledMod m : ModManager.loadRegistry(modsDir)) {
            String fname = m.fileName == null ? "" : m.fileName.toLowerCase();
            String title = m.projectTitle == null ? "" : m.projectTitle.toLowerCase();
            for (String k : keywords) {
                String key = k == null ? "" : k.toLowerCase().trim();
                if (key.isEmpty()) continue;
                if (fname.contains(key) || title.contains(key)) {
                    conflicts.add(m);
                    break;
                }
            }
        }
        return conflicts;
    }

    /**
     * V36.2 DUPE KORUMASI: registry'de ayni projectId'ye sahip kayitlari
     * tekillestirir. Kurulum sirasinda ayni modun eski/farkli adli jar'lari
     * diskte kaliyordu; oyun ayni modu iki kez yukleyip cakisiyordu.
     * Kazanan kayit: null olmayan projectId'li ILK kayit (cagiran kodun
     * ekledigi en yeni kayit genelde listede sona eklenir; removeIf'ler
     * once calistigi icin kalanlar tek proje icin nadirdir). Diger
     * kayitlarin dosyalari (.disabled dahil) silinir.
     */
    static List<InstalledMod> dedupeByProject(File modsDir, List<InstalledMod> mods) {
        if (mods == null || mods.isEmpty()) return mods;
        // V36.2: iki gecis - (1) projectId birebir, (2) normalize edilmis
        // proje adi (slug-vs-UUID farkindan dogan dupelikleri yakalar:
        // ayni mod "sodium" slug'iyla VE Modrinth UUID'siyle kaydedilmis
        // olabilir; ikisi de diskte jar = oyun iki kopya yukler).
        // SON eklenen kayit kazanir (guncel kurulum her zaman en sondadir);
        // kaybeden kayitlarin jar'lari (.disabled dahil) diskten silinir.
        Map<String, InstalledMod> winner = new java.util.LinkedHashMap<>();
        List<InstalledMod> losers = new ArrayList<>();
        for (InstalledMod m : mods) {
            if (m == null) continue;
            List<String> keys = new ArrayList<>(3);
            if (m.projectId != null && !m.projectId.isBlank()) keys.add("id:" + m.projectId.toLowerCase());
            String norm = normalizeTitle(m.projectTitle);
            if (!norm.isEmpty()) keys.add("t:" + norm);
            // Ucuncu anahtar: dosya adinin bas kelimesi ("jei-1.21.1-forge" ->
            // "jei"). YANLIS POZITIF KORUMASI: f: eslesmesi tek basina
            // birlestirme KARARI degildir; asil karar cift-eslesme mantiginda
            // (asagida) verilir. Anahtar siralama amaciyla kullanilir.
            String fk = fileNameKey(m.fileName);
            if (fk.length() >= 3) keys.add("f:" + fk);
            if (keys.isEmpty()) { winner.put("raw:" + System.identityHashCode(m), m); continue; }
            InstalledMod prev = null;
            java.util.LinkedHashSet<String> prevKeys = new java.util.LinkedHashSet<>();
            for (String k : keys) {
                InstalledMod w = winner.remove(k);
                if (w != null && (prev == null || w != prev)) {
                    prev = w;
                    prevKeys.add(k);
                }
            }
            if (prev != null && prev != m) {
                // V36.2.1 KARAR MATIGI:
                //  - id: birebir VEYA t: tam-baslik eslesmesi = GUCLU kanit
                //    (ayni projenin API verisi her zaman aynidir) -> birlestir.
                //  - Yalnizca f: (dosya adi ailesi) eslestiyse ek kanit iste:
                //    id ayni-kelime devami VEYA baslik akronimi. "sodium" vs
                //    "sodium-extra" burada REDDEDILIR (farkli modlar).
                boolean strong = false;
                for (String k : prevKeys) {
                    if (k.startsWith("id:") || k.startsWith("t:")) { strong = true; break; }
                }
                if (!strong) {
                    String id1 = prev.projectId == null ? "" : prev.projectId.toLowerCase();
                    String id2 = m.projectId == null ? "" : m.projectId.toLowerCase();
                    boolean idContain = !id1.isEmpty() && !id2.isEmpty()
                        && ((id1.length() > id2.length() && isIdPrefix(id1, id2))
                            || (id2.length() > id1.length() && isIdPrefix(id2, id1)));
                    boolean acronym = isAcronymMatch(prev.projectTitle, m.projectTitle, fk);
                    // V36.2.1: id'lerden biri gercek Modrinth UUID'siyse
                    // (>=16 karakter, tamamen alnum) ve dosya aileleri de
                    // ayniysa, ayni modun slug-vs-UUID cifti oldugu kesindir.
                    boolean uuidPair = !id1.isEmpty() && !id2.isEmpty() && id1.length() != id2.length()
                        && ((isRealUuid(id1) && id2.length() < 24) || (isRealUuid(id2) && id1.length() < 24))
                        && fileNameKey(prev.fileName).equals(fileNameKey(m.fileName));
                    if (!idContain && !acronym && !uuidPair) {
                        // farkli modlar - birlestirme YOK. KRITIK: prev daha
                        // once winner.remove() ile cikarildi; geri KOYMAZSAK
                        // prev'in kaydi harcaniyor.
                        String pid1 = prev.projectId == null ? "" : prev.projectId.toLowerCase();
                        if (!pid1.isEmpty()) winner.put("id:" + pid1, prev);
                        String pNorm = normalizeTitle(prev.projectTitle);
                        if (!pNorm.isEmpty()) winner.put("t:" + pNorm, prev);
                        String pFk = fileNameKey(prev.fileName);
                        if (pFk.length() >= 3) winner.put("f:" + pFk, prev);
                        prev = null;
                    }
                }
            }
            if (prev != null && prev != m) {
                losers.add(prev);
                // BUGFIX: kaybeden kaydin DIER anahtarlari da map'ten
                // temizlenmeli (id: + t:) - yoksa eski kayit map'te kalip
                // tekillesme saglanmiyor.
                final InstalledMod loser = prev;
                winner.values().removeIf(v -> v == loser);
            }
            for (String k : keys) winner.put(k, m);
        }
        // kazanan kayitlar (sirayi koru, tekillesmis)
        java.util.LinkedHashSet<InstalledMod> uniq = new java.util.LinkedHashSet<>(winner.values());
        List<InstalledMod> out = new ArrayList<>(uniq);
        for (InstalledMod stale : losers) {
            if (!out.contains(stale) && stale.fileName != null && !stale.fileName.isBlank()) {
                new File(modsDir, stale.fileName).delete();
                new File(modsDir, stale.fileName + ".disabled").delete();
            }
        }
        return out;
    }

    /** Gercek Modrinth UUID'si: >=20 karakter, tamamen alnum. */
    private static boolean isRealUuid(String id) {
        if (id == null || id.length() < 20) return false;
        for (char c : id.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) return false;
        }
        return true;
    }

    /** "0", "6", "0", "mc1", "21", "v2" -> true; "extra", "shaders" -> false. */
    private static boolean isVersionish(String w) {
        if (w == null || w.isEmpty()) return false;
        boolean hasDigit = false;
        for (char c : w.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isLetter(c)) return false; // karisik krkt reddet
        }
        // harf iceriyorsa yalnizca v/mc/b/alpha/beta/pre gibi on ekler olabilir
        if (hasDigit) return true;
        String l = w.toLowerCase();
        return l.equals("v") || l.equals("mc") || l.equals("rc")
            || l.equals("alpha") || l.equals("beta") || l.equals("pre")
            || l.equals("snapshot") || l.equals("build");
    }

    /** V36.2.1: uzun id, kisa id'nin AYNI KELIME devami mi? ("jei" -> "jeixyz9"
     *  true; "sodium" -> "sodium-extra" false - separator yeni mod isareti.) */
    private static boolean isIdPrefix(String longId, String shortId) {
        if (!longId.startsWith(shortId)) return false;
        if (longId.length() == shortId.length()) return true;
        char next = longId.charAt(shortId.length());
        return Character.isLetterOrDigit(next);
    }

    /**
     * V36.2: iki baslik akronim iliskisinde mi? "JEI" <-> "Just Enough Items"
     * gibi. Ikisi de kisa olanlar icin dogrudan eslesme; biri tek kelimeyse
     * diger basligin kelimelerinin bas harfleriyle kiyaslanir.
     */
    private static boolean isAcronymMatch(String t1, String t2, String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) return false;
        String a = t1 == null ? "" : t1.trim();
        String b = t2 == null ? "" : t2.trim();
        if (a.isEmpty() || b.isEmpty()) return false;
        String[] w1 = a.toLowerCase().split("[\s_+.-]+");
        String[] w2 = b.toLowerCase().split("[\s_+.-]+");
        if (w1.length == 0 || w2.length == 0) return false;
        // V36.2.1 BUGFIX: kiyas ILK kelimeyle yapilmali - cok kelimeli
        // baslikta son kelime surum numarasi ("Sodium 0.6.0+mc1.21" ->
        // "21") oluyor ve dosya adiyla hic eslesmiyordu.
        String f = fileKey.toLowerCase();
        String first1 = w1[0], first2 = w2[0];
        boolean ok1 = first1.startsWith(f) || f.startsWith(first1);
        boolean ok2 = first2.startsWith(f) || f.startsWith(first2);
        if (w1.length == 1 && w2.length == 1) return ok1 && ok2;
        // V36.2.1: kisa taraf tek kelime + uzun taraf ILK kelimesiyle ayni ->
        // uzun tarafin kalan kelimeleri surum benzeriyse (0.6.0, mc1, 21, v2)
        // ayni modun surum etiketli adi demektir -> ESLESIR.
        // "Sodium Extra" gibi ikinci GERCEK kelime icerenler eslesmez.
        // V36.2.1 GENEL KURAL: kisa baslik, uzun basligin kelime-sinirinda
        // ONEKI olmali ve ek kelimeler YALNIZCA surum-vari olmali.
        //   "Iris Shaders"  vs "Iris Shaders 1.7"  -> esles (ekler surum)
        //   "Sodium"        vs "Sodium Extra"      -> RED (extra surum degil)
        //   "Sodium"        vs "Sodium 0.6.0 mc1"  -> esles (ekler surum)
        String[] shorter = w1.length <= w2.length ? w1 : w2;
        String[] longer = w1.length <= w2.length ? w2 : w1;
        if (shorter.length < longer.length) {
            boolean prefix = true;
            for (int i = 0; i < shorter.length; i++) {
                if (!shorter[i].equals(longer[i])) { prefix = false; break; }
            }
            if (prefix) {
                boolean restAreVersion = true;
                for (int i = shorter.length; i < longer.length; i++) {
                    if (!isVersionish(longer[i])) { restAreVersion = false; break; }
                }
                if (restAreVersion) return true;
            }
        }
        // akronim: kisa taraf (<=4 harf) uzun tarafin bas harfleri mi?
        String short_ = w1.length <= w2.length ? a.toLowerCase() : b.toLowerCase();
        String long_ = w1.length <= w2.length ? b.toLowerCase() : a.toLowerCase();
        String[] longWords = long_.split("[\s_+.-]+");
        if (short_.length() <= 5 && short_.length() == longWords.length) {
            StringBuilder sb = new StringBuilder();
            for (String w : longWords) sb.append(w.isEmpty() ? ' ' : w.charAt(0));
            return sb.toString().equals(short_);
        }
        return false;
    }

    /** "jei-1.21.1-forge.jar" -> "jei"; "Sodium 0.6.jar.disabled" -> "sodium". */
    private static String fileNameKey(String name) {
        if (name == null) return "";
        String n = name.toLowerCase();
        if (n.endsWith(".disabled")) n = n.substring(0, n.length() - 9);
        if (n.endsWith(".jar")) n = n.substring(0, n.length() - 4);
        StringBuilder sb = new StringBuilder();
        for (char c : n.toCharArray()) {
            if (Character.isLetterOrDigit(c)) sb.append(c);
            else break;
        }
        return sb.toString();
    }

    /** V36.2.1: TAM baslik normalize edilir - "Sodium Extra" -> "sodiumextra".
     *  Ayni projenin API basligi her zaman birebir ayni oldugu icin bu guclu
     *  anahtardir; "Sodium" ile "Sodium Extra" artik KARISMAZ. */
    private static String normalizeTitle(String t) {
        if (t == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : t.toLowerCase().toCharArray()) {
            if (Character.isLetterOrDigit(c)) sb.append(c);
        }
        return sb.toString();
    }

    public static void removeMod(File modsDir, InstalledMod mod) {
        new File(modsDir, mod.fileName).delete();
        new File(modsDir, mod.fileName + ".disabled").delete();
        List<InstalledMod> mods = ModManager.loadRegistry(modsDir);
        mods.removeIf(m -> m.fileName.equals(mod.fileName));
        ModManager.saveRegistry(modsDir, mods);
    }

    /**
     * V33 CAKISMA COZUCU: iki modun AYNI bagimliligi farkli surumlerde
     * istemesi durumunu bulur. Kurulu jar'lar diskten taranir (fabric.mod.json
     * / mods.toml / mcmod.info), her modun provides/bagimlilik kimligi ile
     * dosya adi surumunden (isim-1.2.3.jar -> 1.2.3) surum cikarilir.
     * Ayni kimligi paylasip farkli surumde olan ciftler raporlanir.
     */
    public static List<DepConflict> findDepVersionConflicts(File modsDir) {
        Map<String, InstalledMod> byProvide = new HashMap<String, InstalledMod>();
        Map<String, String> verByProvide = new HashMap<String, String>();
        List<DepConflict> out = new ArrayList<DepConflict>();
        // Registry'yi dosya-adi indeksine cevir: diskte olup registry'de
        // kaydi olmayan (elle atilan) jar'lar da taranabilsin.
        Map<String, InstalledMod> registryByFile = new HashMap<String, InstalledMod>();
        for (InstalledMod rm : ModManager.loadRegistry(modsDir)) {
            if (rm.fileName != null) registryByFile.put(rm.fileName, rm);
        }
        File[] jars = modsDir.listFiles((d, n) -> n != null && n.toLowerCase().endsWith(".jar"));
        if (jars == null) return out;
        for (File jar : jars) {
            String fname = jar.getName();
            InstalledMod m = registryByFile.get(fname);
            if (m == null) {
                m = new InstalledMod();
                m.fileName = fname;
                m.projectTitle = fname;
                m.enabled = true;
            }
            String modId = ModManager.probeModId(jar);
            if (modId == null || modId.isEmpty()) modId = ModManager.guessIdFromFilename(fname);
            if (modId == null || modId.isEmpty()) continue;
            String ver = ModManager.versionFromFilename(fname);
            String prevVer = verByProvide.get(modId);
            if (prevVer == null) {
                byProvide.put(modId, m);
                verByProvide.put(modId, ver);
            } else if (ver != null && prevVer != null && !ver.equals(prevVer)) {
                out.add(new DepConflict(modId, byProvide.get(modId), prevVer, m, ver));
                // Daha yeni surumu temsilci yap: sonraki ciftler onunla karsilastirilsin.
                if (ModManager.compareVer(ver, prevVer) > 0) {
                    byProvide.put(modId, m);
                    verByProvide.put(modId, ver);
                }
            }
        }
        return out;
    }

    /** Iki surum dizisini sayisal karsilastirir ("0.9.2" vs "0.9.10"). */
    private static int compareVer(String a, String b) {
        try {
            String[] pa = a.split("[^0-9A-Za-z]+");
            String[] pb = b.split("[^0-9A-Za-z]+");
            int n = Math.max(pa.length, pb.length);
            for (int i = 0; i < n; i++) {
                String sa = i < pa.length ? pa[i] : "0";
                String sb = i < pb.length ? pb[i] : "0";
                try {
                    int ia = Integer.parseInt(sa);
                    int ib = Integer.parseInt(sb);
                    if (ia != ib) return Integer.compare(ia, ib);
                } catch (NumberFormatException e) {
                    int c = sa.compareToIgnoreCase(sb);
                    if (c != 0) return c;
                }
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /** "sodium-0.9.2-mc26.2.jar" gibi dosya adindan surumu cikarir. */
    private static String versionFromFilename(String fname) {
        String base = fname.toLowerCase();
        if (base.endsWith(".jar")) base = base.substring(0, base.length() - 4);
        if (base.endsWith(".disabled")) base = base.substring(0, base.length() - 9);
        // Minecraft surum etiketlerini (mc1.21.1, mc26.2, +1.21.1) at - bunlar
        // mod surumu degil hedef oyun surumudur.
        base = base.replaceAll("mc[0-9]+(?:\\.[0-9]+)*", " ");
        base = base.replaceAll("\\+1\\.[0-9]+(?:\\.[0-9]+)*", " ");
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)+)").matcher(base);
        return m.find() ? m.group(1) : null;
    }

    /** Dosya adindan kaba mod kimligi (metin ön işaretli ilk token). */
    private static String guessIdFromFilename(String fname) {
        String base = fname.toLowerCase();
        if (base.endsWith(".jar")) base = base.substring(0, base.length() - 4);
        if (base.endsWith(".disabled")) base = base.substring(0, base.length() - 9);
        base = base.replaceAll("[^a-z0-9]+", "-");
        return base;
    }

    /** jar icindeki metadata'dan mod id'sini okur (fabric.mod.json | mods.toml | mcmod.info). */
    private static String probeModId(File jarFile) {
        try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(jarFile)) {
            java.util.zip.ZipEntry e = zf.getEntry("fabric.mod.json");
            if (e != null) {
                String s = ModManager.readZipEntry(zf, e);
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"").matcher(s);
                if (m.find()) return m.group(1);
            }
            e = zf.getEntry("META-INF/mods.toml");
            if (e == null) e = zf.getEntry("META-INF/neoforge.mods.toml");
            if (e != null) {
                String s = ModManager.readZipEntry(zf, e);
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?im)^\\s*modId\\s*=\\s*\"([^\"]+)\"").matcher(s);
                if (m.find()) return m.group(1);
            }
            e = zf.getEntry("mcmod.info");
            if (e != null) {
                String s = ModManager.readZipEntry(zf, e);
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"modid\"\\s*:\\s*\"([^\"]+)\"").matcher(s);
                if (m.find()) return m.group(1);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String readZipEntry(java.util.zip.ZipFile zf, java.util.zip.ZipEntry e) throws java.io.IOException {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.InputStream is = zf.getInputStream(e)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) > 0) bos.write(buf, 0, r);
        }
        return new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }

    /** Cakisan iki kurulum: ayni bagimlilik kimligi, farkli surumler. */
    public static class DepConflict {
        public final String provideId;
        public final InstalledMod modA;
        public final String versionA;
        public final InstalledMod modB;
        public final String versionB;
        /** Cozum onerisi: surumu daha YUKSEK olan tutulur, eskisi kaldirilir. */
        public final InstalledMod keepMod;
        public final String keepVersion;
        public final InstalledMod removeMod;
        public final String removeVersion;
        public DepConflict(String provideId, InstalledMod a, String va, InstalledMod b, String vb) {
            this.provideId = provideId;
            this.modA = a;
            this.versionA = va;
            this.modB = b;
            this.versionB = vb;
            if (ModManager.compareVer(va == null ? "" : va, vb == null ? "" : vb) >= 0) {
                this.keepMod = a;
                this.keepVersion = va;
                this.removeMod = b;
                this.removeVersion = vb;
            } else {
                this.keepMod = b;
                this.keepVersion = vb;
                this.removeMod = a;
                this.removeVersion = va;
            }
        }
        public String title(InstalledMod m) {
            return m.projectTitle != null && !m.projectTitle.isEmpty() ? m.projectTitle : m.fileName;
        }
    }

    public static void setEnabled(File modsDir, InstalledMod mod, boolean enabled) {
        File current = new File(modsDir, (String)(mod.enabled ? mod.fileName : mod.fileName + ".disabled"));
        File target = new File(modsDir, (String)(enabled ? ModManager.stripDisabled(mod.fileName) : mod.fileName + ".disabled"));
        if (current.exists() && !current.equals(target)) {
            current.renameTo(target);
        }
        mod.enabled = enabled;
        List<InstalledMod> mods = ModManager.loadRegistry(modsDir);
        for (InstalledMod m : mods) {
            if (!m.fileName.equals(mod.fileName)) continue;
            m.enabled = enabled;
        }
        ModManager.saveRegistry(modsDir, mods);
    }

    private static String stripDisabled(String name) {
        return name.endsWith(".disabled") ? name.substring(0, name.length() - 9) : name;
    }

    public static File installedManualJar(File modsDir, File jarFile) throws IOException {
        File dest = new File(modsDir, jarFile.getName());
        Files.copy(jarFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        List<InstalledMod> mods = ModManager.loadRegistry(modsDir);
        mods.removeIf(m -> m.fileName.equals(dest.getName()));
        InstalledMod im = new InstalledMod();
        im.fileName = dest.getName();
        String base = dest.getName();
        if (base.toLowerCase().endsWith(".jar")) {
            base = base.substring(0, base.length() - 4);
        }
        im.projectTitle = base;
        im.versionNumber = "Yerel JAR";
        mods.add(im);
        ModManager.saveRegistry(modsDir, mods);
        return dest;
    }

    public static void installExternal(File modsDir, String fileName, String title, String versionNumber, String iconUrl, String downloadUrl, HttpUtil.ByteProgress progress) throws IOException {
        ModManager.installExternal(modsDir, fileName, title, versionNumber, iconUrl, downloadUrl, progress, null, null);
    }

    /**
     * CurseForge indirmeleri icin hash'li + iptal edilebilir varyant.
     * AutoModInstaller ve diger eski cagiranlar degismeden kaliyor.
     */
    public static void installExternal(File modsDir, String fileName, String title, String versionNumber, String iconUrl, String downloadUrl, HttpUtil.ByteProgress progress, String expectedSha1, java.util.concurrent.atomic.AtomicBoolean cancelled) throws IOException {
        File dest = new File(modsDir, fileName);
        HttpUtil.downloadFileVerified(downloadUrl, dest, progress, expectedSha1, cancelled);
        List<InstalledMod> mods = ModManager.loadRegistry(modsDir);
        mods.removeIf(m -> m.fileName.equals(fileName));
        InstalledMod im = new InstalledMod();
        im.fileName = fileName;
        im.projectId = "curseforge:" + fileName;
        im.projectTitle = title;
        im.versionNumber = versionNumber;
        im.iconUrl = iconUrl;
        mods.add(im);
        ModManager.saveRegistry(modsDir, mods);
    }

    public static synchronized CurseForgeResult installCurseforgeWithDependencies(String apiKey, File modsDir, CurseForgeApi.ModResult mod, CurseForgeApi.FileResult file, String loader, String mcVersion, ProgressListener progress) throws IOException {
        return ModManager.installCurseforgeWithDependencies(apiKey, modsDir, mod, file, loader, mcVersion, progress, null);
    }

    public static synchronized CurseForgeResult installCurseforgeWithDependencies(String apiKey, File modsDir, CurseForgeApi.ModResult mod, CurseForgeApi.FileResult file, String loader, String mcVersion, ProgressListener progress, java.util.concurrent.atomic.AtomicBoolean cancelled) throws IOException {
        CurseForgeResult result = new CurseForgeResult();
        // V30 KOK COZUM - CF ZINCIRI GIRISI DOGRULAMASI: gelen dosya hedef
        // MC surumunu desteklemiyorsa kurulum reddedilir (yanlis surum
        // hicbir yoldan giremez).
        String effMc = ModrinthApi.effectiveMcVersion(mcVersion);
        if (effMc != null && !file.gameVersions.contains(effMc)) {
            throw new IOException("Surum uyusmazligi: " + mod.name + " " + file.displayName + " -> MC " + effMc + " desteklemiyor, kurulum reddedildi");
        }
        HashSet<Integer> visited = new HashSet<Integer>();
        visited.add(mod.id);
        ModManager.installCurseforgeFile(apiKey, modsDir, mod, file, loader, mcVersion, visited, result, progress, cancelled);
        ModManager.report(progress, 100, "Kurulum tamamland\u0131");
        return result;
    }

    private static void installCurseforgeFile(String apiKey, File modsDir, CurseForgeApi.ModResult mod, CurseForgeApi.FileResult file, String loader, String mcVersion, Set<Integer> visited, CurseForgeResult result, ProgressListener progress, java.util.concurrent.atomic.AtomicBoolean cancelled) throws IOException {
        String url = CurseForgeApi.getDownloadUrl(apiKey, mod.id, file.id);
        if (url == null) {
            return;
        }
        String fileName = file.fileName != null && !file.fileName.isEmpty() ? file.fileName : ModManager.fileNameFromUrl(url);
        String versionNumber = file.displayName != null && !file.displayName.isEmpty() ? file.displayName : fileName;
        ModManager.report(progress, 0, "\u0130ndiriliyor: " + fileName);
        ModManager.installExternal(modsDir, fileName, mod.name, versionNumber, mod.logoUrl, url, (done, total) -> ModManager.report(progress, total > 0L ? (int)(done * 100L / total) : -1, "\u0130ndiriliyor: " + fileName), file.sha1, cancelled);
        for (CurseForgeApi.FileDependency d : file.dependencies) {
            if (d.relationType != 3 || visited.contains(d.modId)) continue;
            ModManager.installCurseforgeDependency(apiKey, modsDir, d.modId, loader, mcVersion, visited, result, progress, cancelled);
        }
    }

    private static void installCurseforgeDependency(String apiKey, File modsDir, int modId, String loader, String mcVersion, Set<Integer> visited, CurseForgeResult result, ProgressListener progress, java.util.concurrent.atomic.AtomicBoolean cancelled) throws IOException {
        CurseForgeApi.ModResult m;
        visited.add(modId);
        String effMc = ModrinthApi.effectiveMcVersion(mcVersion);
        // 2 denemeli dosya aramasi: ilk denemede gecici API hatasi olursa
        // (timeout/5xx) dependency'i NADIR ama sessizce atlama yerine bir kez
        // daha dene (filtreli -> filtresiz fallback her denemede uygulanir).
        CurseForgeApi.FileResult best = null;
        for (int attempt = 0; attempt < 2 && best == null; ++attempt) {
            if (attempt > 0) {
                try { Thread.sleep(400L); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
            }
            // V32: loader-filtreli arama exception atarsa (gecici 5xx vb.)
            // tum dep kurulumu gidiyordu - try ile sarip filtresiz fallback'e dus.
            try {
                List<CurseForgeApi.FileResult> files = CurseForgeApi.getFiles(apiKey, modId, loader, effMc);
                // V30: surum-blind pickBestFile YOK - hedef MC biliniyorsa
                // SADECE o surumu destekleyen dosyalar arasindan secilir.
                best = CurseForgeApi.pickBestCompatibleFile(files, effMc);
            } catch (IOException filteredFail) {
                best = null;
            }
            if (best == null) {
                List<CurseForgeApi.FileResult> allFiles = CurseForgeApi.getFiles(apiKey, modId, null, null);
                best = CurseForgeApi.pickBestCompatibleFile(allFiles, effMc);
            }
            if (best != null) break;
        }
        if (best == null) {
            CurseForgeApi.ModResult skipProj = CurseForgeApi.getMod(apiKey, modId);
            result.skippedDependencies.add(skipProj != null && skipProj.name != null ? skipProj.name : ("mod-" + modId));
            return;
        }
        CurseForgeApi.ModResult depMod = CurseForgeApi.getMod(apiKey, modId);
        if (depMod != null) {
            m = depMod;
        } else {
            m = new CurseForgeApi.ModResult();
            m.id = modId;
            m.name = "mod-" + modId;
        }
        ModManager.report(progress, -1, "Gerekli mod kuruluyor: " + m.name);
        ModManager.installCurseforgeFile(apiKey, modsDir, m, best, loader, mcVersion, visited, result, progress, cancelled);
        result.installedDependencies.add(m.name);
    }

    private static String fileNameFromUrl(String url) {
        try {
            String p = URI.create(url).getPath();
            String n = p.substring(p.lastIndexOf(47) + 1);
            return URLDecoder.decode(n, StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            return "mod-" + System.currentTimeMillis() + ".jar";
        }
    }

    public static class InstalledMod {
        public String fileName;
        public String projectId;
        public String projectTitle;
        public String versionNumber;
        public String iconUrl;
        public boolean enabled = true;
    }

    public static class InstallResult {
        public List<String> installedDependencies = new ArrayList<String>();
        public List<String> skippedDependencies = new ArrayList<String>();
    }

    public static enum ChangeType {
        NONE,
        INSTALLED,
        UPDATED;

    }

    public static class PairResult {
        public ChangeType shader = ChangeType.NONE;
        public ChangeType base = ChangeType.NONE;
    }

    public static class CurseForgeResult {
        public List<String> installedDependencies = new ArrayList<String>();
        public List<String> skippedDependencies = new ArrayList<String>();
    }
}


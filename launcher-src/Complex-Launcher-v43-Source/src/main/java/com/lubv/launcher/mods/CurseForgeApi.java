/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.mods;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lubv.launcher.core.HttpUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CurseForgeApi {
    private static final String BASE = "https://api.curseforge.com";

    /**
     * Retry'li JSON GET: Modrinth tarafindaki transient-hata korumasinin
     * aynisi (3 deneme, artan bekleme; 4xx aninda firlatir). Tek atislik
     * istek tek bir API/CDN hiccup'inda tum kurulumu olduruyordu.
     */
    private static JsonObject getJsonRetry(String url, String apiKey) throws IOException {
        return com.google.gson.JsonParser.parseString(HttpUtil.getTextWithRetry(url, apiKey, 3)).getAsJsonObject();
    }

    public static final int GAME_ID = 432;
    public static final int CLASS_MODS = 6;
    public static final int CLASS_MODPACKS = 4471;
    public static final int CLASS_BUKKIT_PLUGINS = 5;

    /**
     * Launcher'a gomulu, uygulama-sahibi CurseForge Core API anahtari.
     * Prism Launcher / HMCL gibi diger launcher'lar da ayni yontemi
     * kullanir: kullanicidan anahtar istemek yerine, gelistiricinin
     * kendi console.curseforge.com hesabindan aldigi anahtari uygulamaya
     * gomup varsayilan olarak kullanirlar - kullanici ayrica bir sey
     * yapmak zorunda kalmaz.
     *
     * ONEMLI: Bu deger su an BOS - CurseForge, API anahtarlarinin
     * gelistirici hesabina ozel oldugunu ve ucuncu sahislar tarafindan
     * (bu dahil) baskasi adina talep edilemeyecegini belirtir. Bu
     * satiri kullanabilmeniz icin:
     *   1) https://console.curseforge.com adresinden kendi hesabinizla
     *      giris yapip bir API anahtari talep edin (uygulama adi,
     *      website URL'si gibi bilgiler istenir)
     *   2) Onaylanan anahtari asagidaki DEFAULT_API_KEY sabitine yapistirin
     * Bu yapildiktan sonra kullanicilar CurseForge modlarini/modpack'lerini
     * ayrica bir anahtar girmeden arayip kurabilir; kullanici yine de
     * isterse Ayarlar'dan kendi anahtarini girip bu varsayilani gecersiz
     * kilabilir.
     */
    private static final String DEFAULT_API_KEY = "";

    /**
     * Cagiran taraf (UI) bir anahtar vermediyse (null/bos), gomulu
     * varsayilan anahtari kullanir. Boylece mevcut tum metotlar (ki
     * hepsi zaten "apiKey" parametresi aliyor) degismeden, sadece
     * cagri yapan yerlerde kullanici anahtari yoksa otomatik olarak
     * gomulu anahtara dusulur.
     */
    private static String resolveApiKey(String apiKey) {
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        return DEFAULT_API_KEY.isBlank() ? null : DEFAULT_API_KEY;
    }

    private static List<ModResult> search(String apiKey, String query, int classId, String loader, String mcVersion) throws IOException {
        int lt;
        // V34.7 BUG FIX: bos query'ye searchFilter= eklenince CF API 422/400
        // donebiliyordu ("populer modlar" listesi bosta kaliyordu). Query
        // bosken searchFilter parametresi HIC gonderilmez - sortField=2
        // (indirme sayisi) zaten populerleri getirir.
        StringBuilder url = new StringBuilder("https://api.curseforge.com/v1/mods/search?gameId=432&classId=" + classId);
        if (query != null && !query.isBlank()) {
            url.append("&searchFilter=").append(CurseForgeApi.enc(query));
        }
        url.append("&sortField=2&sortOrder=desc&pageSize=50");
        // V29.7.1: cop/"latest" surum gameVersion filtresine girerse CF de
        // 0 sonuc dondurur - filtre SADECE gercek MC surumunde eklenir.
        if (ModrinthApi.looksLikeMcVersion(mcVersion) && !"latest".equalsIgnoreCase(mcVersion)) {
            url.append("&gameVersion=").append(CurseForgeApi.enc(mcVersion.trim()));
        }
        if ((lt = CurseForgeApi.loaderType(loader)) > 0) {
            url.append("&modLoaderType=").append(lt);
        }
        JsonObject resp = CurseForgeApi.getJsonRetry(url.toString(), CurseForgeApi.resolveApiKey(apiKey));
        JsonArray data = resp.getAsJsonArray("data");
        ArrayList<ModResult> results = new ArrayList<ModResult>();
        for (int i = 0; i < data.size(); ++i) {
            JsonObject m = data.get(i).getAsJsonObject();
            ModResult r = new ModResult();
            r.id = m.get("id").getAsInt();
            r.name = m.get("name").getAsString();
            r.summary = m.has("summary") && !m.get("summary").isJsonNull() ? m.get("summary").getAsString() : "";
            r.downloadCount = m.has("downloadCount") && !m.get("downloadCount").isJsonNull() ? m.get("downloadCount").getAsLong() : 0L;
            r.classId = m.has("classId") ? m.get("classId").getAsInt() : classId;
            r.logoUrl = CurseForgeApi.logo(m);
            results.add(r);
        }
        return results;
    }

    public static List<ModResult> searchMods(String apiKey, String query, String loader, String mcVersion) throws IOException {
        return CurseForgeApi.search(apiKey, query, 6, loader, mcVersion);
    }

    public static List<ModResult> searchPlugins(String apiKey, String query, String mcVersion) throws IOException {
        return CurseForgeApi.search(apiKey, query, 5, null, mcVersion);
    }

    public static List<ModResult> searchModpacks(String apiKey, String query) throws IOException {
        return CurseForgeApi.search(apiKey, query, 4471, null, null);
    }

    public static List<FileResult> getFiles(String apiKey, int modId, String loader, String mcVersion) throws IOException {
        int lt;
        StringBuilder url = new StringBuilder("https://api.curseforge.com/v1/mods/" + modId + "/files?pageSize=50");
        // V29.7.1: cop surum filtresi bos sonuc dondurur - filtresiz listeye
        // dus, asagidaki siki istemci tarafi gecidi yanlisi yine reddeder.
        if (ModrinthApi.looksLikeMcVersion(mcVersion) && !"latest".equalsIgnoreCase(mcVersion)) {
            url.append("&gameVersion=").append(CurseForgeApi.enc(mcVersion));
        }
        if ((lt = CurseForgeApi.loaderType(loader)) > 0) {
            url.append("&modLoaderType=").append(lt);
        }
        JsonObject resp;
        try {
            resp = CurseForgeApi.getJsonRetry(url.toString(), CurseForgeApi.resolveApiKey(apiKey));
        }
        catch (IOException loaderFilteredFail) {
            // V32: loader+surum filtreli arama bos/422 donebiliyor - filtresiz
            // tekrar dene (asagida pickBestCompatibleFile yine de siki secer).
            StringBuilder fb = new StringBuilder("https://api.curseforge.com/v1/mods/" + modId + "/files?pageSize=50");
            if (ModrinthApi.looksLikeMcVersion(mcVersion) && !"latest".equalsIgnoreCase(mcVersion)) {
                fb.append("&gameVersion=").append(CurseForgeApi.enc(mcVersion));
            }
            resp = CurseForgeApi.getJsonRetry(fb.toString(), CurseForgeApi.resolveApiKey(apiKey));
        }
        JsonArray data = resp.getAsJsonArray("data");
        ArrayList<FileResult> files = new ArrayList<FileResult>();
        for (int i = 0; i < data.size(); ++i) {
            int j;
            JsonObject f = data.get(i).getAsJsonObject();
            FileResult fr = new FileResult();
            fr.id = f.get("id").getAsInt();
            fr.modId = f.get("modId").getAsInt();
            fr.displayName = f.has("displayName") && !f.get("displayName").isJsonNull() ? f.get("displayName").getAsString() : "";
            fr.fileName = f.has("fileName") && !f.get("fileName").isJsonNull() ? f.get("fileName").getAsString() : "";
            fr.releaseType = f.has("releaseType") ? f.get("releaseType").getAsInt() : 1;
            long l = fr.fileLength = f.has("fileLength") ? f.get("fileLength").getAsLong() : 0L;
            // hashes: [{value, algo}] - algo 1 = SHA-1. Indirme sonrasi
            // butunluk dogrulamasi icin saklanir (yoksa null kalir, dogrulama atlanir).
            if (f.has("hashes") && !f.get("hashes").isJsonNull()) {
                JsonArray hs = f.getAsJsonArray("hashes");
                for (int h = 0; h < hs.size(); ++h) {
                    JsonObject ho = hs.get(h).getAsJsonObject();
                    int algo = ho.has("algo") ? ho.get("algo").getAsInt() : 0;
                    if (algo == 1 && ho.has("value") && !ho.get("value").isJsonNull()) {
                        fr.sha1 = ho.get("value").getAsString();
                        break;
                    }
                }
            }
            if (f.has("gameVersions")) {
                JsonArray gv = f.getAsJsonArray("gameVersions");
                for (j = 0; j < gv.size(); ++j) {
                    fr.gameVersions.add(gv.get(j).getAsString());
                }
            }
            if (f.has("dependencies")) {
                JsonArray deps = f.getAsJsonArray("dependencies");
                for (j = 0; j < deps.size(); ++j) {
                    JsonObject dep = deps.get(j).getAsJsonObject();
                    FileDependency fd = new FileDependency();
                    fd.modId = dep.has("modId") ? dep.get("modId").getAsInt() : 0;
                    fd.relationType = dep.has("relationType") ? dep.get("relationType").getAsInt() : 0;
                    fr.dependencies.add(fd);
                }
            }
            files.add(fr);
        }
        return files;
    }

    /**
     * Filtresiz CF dosya listesinden MC surumune en yakin uyumlu dosya.
     * Skor: birebir gameVersion eslesmesi > ayni minor cizgi (1.21.x) > herhangi.
     * Release dosyalari tercih edilir; esitlikte en yeni fileId kazanir.
     */
    public static FileResult pickBestCompatibleFile(List<FileResult> files, String mcVersion) {
        if (files == null || files.isEmpty()) {
            return null;
        }
        String mcMinor = null;
        if (mcVersion != null) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(\\d+\\.\\d+)").matcher(mcVersion);
            if (m.find()) {
                mcMinor = m.group(1);
            }
        }
        // V29.7 SIKI GECIT: mcVersion biliniyorsa SADECE ayni MC surum
        // cizgisinden (birebir veya ayni minor) dosyalar degerlendirilir.
        // Farkli surum icin derlenmis dosya kurmak yerine null doner.
        List<FileResult> candidates = files;
        if (mcVersion != null) {
            candidates = new ArrayList<FileResult>();
            for (FileResult f : files) {
                if (f == null) continue;
                if (f.gameVersions.contains(mcVersion)) {
                    candidates.add(f);
                    continue;
                }
                if (mcMinor != null) {
                    for (String gv : f.gameVersions) {
                        if (gv != null && gv.startsWith(mcMinor)) {
                            candidates.add(f);
                            break;
                        }
                    }
                }
            }
            if (candidates.isEmpty()) {
                return null;
            }
        }
        FileResult best = null;
        int bestScore = Integer.MIN_VALUE;
        for (FileResult f : candidates) {
            if (f == null || f.fileName == null || f.fileName.isEmpty()) continue;
            boolean exact = mcVersion != null && f.gameVersions.contains(mcVersion);
            int score = (exact ? 4 : 0) + (f.releaseType == 1 ? 1 : 0);
            if (score > bestScore || (score == bestScore && best != null && f.id > best.id)) {
                bestScore = score;
                best = f;
            }
        }
        return best;
    }

    public static String getDownloadUrl(String apiKey, int modId, int fileId) throws IOException {
        JsonObject resp = CurseForgeApi.getJsonRetry("https://api.curseforge.com/v1/mods/" + modId + "/files/" + fileId + "/download-url", CurseForgeApi.resolveApiKey(apiKey));
        if (resp.has("data") && !resp.get("data").isJsonNull()) {
            return resp.get("data").getAsString();
        }
        return null;
    }

    public static ModResult getMod(String apiKey, int modId) {
        try {
            JsonObject resp = CurseForgeApi.getJsonRetry("https://api.curseforge.com/v1/mods/" + modId, CurseForgeApi.resolveApiKey(apiKey));
            JsonObject d = resp.getAsJsonObject("data");
            ModResult r = new ModResult();
            r.id = d.get("id").getAsInt();
            r.name = d.get("name").getAsString();
            r.summary = d.has("summary") && !d.get("summary").isJsonNull() ? d.get("summary").getAsString() : "";
            r.downloadCount = d.has("downloadCount") && !d.get("downloadCount").isJsonNull() ? d.get("downloadCount").getAsLong() : 0L;
            r.classId = d.has("classId") ? d.get("classId").getAsInt() : 0;
            r.logoUrl = CurseForgeApi.logo(d);
            return r;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static FileResult pickBestFile(List<FileResult> files) {
        if (files == null || files.isEmpty()) {
            return null;
        }
        for (FileResult f : files) {
            if (f.releaseType != 1) continue;
            return f;
        }
        for (FileResult f : files) {
            if (f.releaseType != 2) continue;
            return f;
        }
        return files.get(0);
    }

    private static int loaderType(String loader) {
        if (loader == null) {
            return 0;
        }
        // V29.7.1: buyuk/kucuk harf duyarsiz - "vanilla" gibi bilinmeyen
        // yukleyiciler 0 (filtresiz) dondurur; filtre yanlis yukleyiciye
        // kilitlenip bos sonuc dondurmesin.
        String l = loader.trim().toLowerCase();
        return switch (l) {
            case "fabric" -> 4;
            case "forge" -> 1;
            case "quilt" -> 5;
            case "neoforge" -> 6;
            case "paper" -> 3;
            case "spigot", "bukkit" -> 2;
            default -> 0;
        };
    }

    private static String logo(JsonObject m) {
        if (m.has("logo") && m.get("logo").isJsonObject()) {
            JsonObject logo = m.getAsJsonObject("logo");
            if (logo.has("thumbnailUrl") && !logo.get("thumbnailUrl").isJsonNull()) {
                return logo.get("thumbnailUrl").getAsString();
            }
            if (logo.has("url") && !logo.get("url").isJsonNull()) {
                return logo.get("url").getAsString();
            }
        }
        return null;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public static class ModResult {
        public int id;
        public String name;
        public String summary;
        public String logoUrl;
        public long downloadCount;
        public int classId;
    }

    public static class FileResult {
        public int id;
        public int modId;
        public String displayName;
        public String fileName;
        public int releaseType;
        public long fileLength;
        public String sha1;
        public List<String> gameVersions = new ArrayList<String>();
        public List<FileDependency> dependencies = new ArrayList<FileDependency>();
    }

    public static class FileDependency {
        public int modId;
        public int relationType;
    }
}


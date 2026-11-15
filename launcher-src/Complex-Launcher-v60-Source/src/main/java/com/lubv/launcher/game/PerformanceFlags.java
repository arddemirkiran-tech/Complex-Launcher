package com.lubv.launcher.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Sistemin RAM'ine, CPU cekirdek sayisina ve secilen ayrilmis RAM
 * miktarina gore, o an icin en uygun JVM GC/JIT bayraklarini secer.
 * "Maks Performans" tuşu acildiginda GameLauncher tarafindan cagirilir.
 *
 * Secim mantigi (yaygin olarak Minecraft topluluğunda kullanilan,
 * test edilmis Aikar's Flags ve modern JVM GC tavsiyelerine dayanir):
 *  - 6GB ve altinda ayrilmis RAM: G1GC + kucuk heap'ler icin optimize
 *    edilmis "Aikar's Flags" varyanti (dusuk duraklama, ufak heap'te
 *    G1GC'nin bolge boyutu kucuk tutulur).
 *  - 6GB uzeri: ZGC (Java 17+'ta production-ready, cok dusuk duraklama
 *    suresi verir, buyuk heap'lerde G1GC'den daha iyi sonuc verir).
 *  - CPU cekirdek sayisina gore paralel GC thread sayisi ayarlanir.
 */
public final class PerformanceFlags {
    private PerformanceFlags() {
    }

    /**
     * Mevcut sistem icin onerilen JVM bayraklarinin tam listesini dondurur.
     * V29 DUZELTMESI: ZGC karari icin artik OYUNUN gercek Java major
     * surumu kullanilir - onceden launcher'in kendi JVM'i sorgulaniyordu,
     * oysa oyun baska bir runtime ile (orn. paketlenmis JDK 21) aciliyor
     * olabilir. gameJavaMajor <= 0 ise (bilinmiyor) launcher'in surumune
     * geri donulur - eski davranis.
     */
    public static List<String> recommendedJvmFlags(int ramGB, int gameJavaMajor) {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors());
        List<String> flags = new ArrayList<>();

        // ZGC yalnizca oyunun calisacagi JVM Java 21+ ise. -XX:+ZGenerational
        // bayragi Java 21'den onceki JVM'lerde TANINMAZ ve JVM acilirken
        // tamamen reddeder - bu yuzden karar OYUNUN Java surumune gore verilir.
        int effectiveJava = gameJavaMajor > 0 ? gameJavaMajor : inferLauncherJavaMajor();
        boolean useZgc = ramGB >= 7 && effectiveJava >= 21;
        if (useZgc) {
            // ZGC: cok dusuk duraklama suresi, buyuk heap'lerde ideal.
            flags.add("-XX:+UseZGC");
            flags.add("-XX:+ZGenerational");
        } else {
            // G1GC tabanli, Aikar's Flags'in kucuk-heap icin ayarlanmis hali.
            flags.add("-XX:+UseG1GC");
            flags.add("-XX:+ParallelRefProcEnabled");
            flags.add("-XX:MaxGCPauseMillis=130");
            flags.add("-XX:+UnlockExperimentalVMOptions");
            flags.add("-XX:+DisableExplicitGC");
            // AlwaysPreTouch bilincli olarak KALDIRILDI: acilista tum heap
            // sayfalarini dokunarak isaretler -> oyun acilma suresini uzatir.
            flags.add("-XX:G1NewSizePercent=" + (ramGB <= 3 ? 20 : 30));
            flags.add("-XX:G1MaxNewSizePercent=" + (ramGB <= 3 ? 30 : 40));
            flags.add("-XX:G1HeapRegionSize=" + (ramGB <= 3 ? "4M" : "8M"));
            flags.add("-XX:G1ReservePercent=" + (ramGB <= 3 ? 15 : 20));
            flags.add("-XX:InitiatingHeapOccupancyPercent=15");
            flags.add("-XX:G1MixedGCLiveThresholdPercent=90");
            flags.add("-XX:G1RSetUpdatingPauseTimePercent=5");
            flags.add("-XX:SurvivorRatio=32");
            flags.add("-XX:MaxTenuringThreshold=1");
            // --- Tam Aikar seti (test edilmis ekstra ince ayarlar) ---
            flags.add("-XX:TargetSurvivorRatio=90");
            flags.add("-XX:G1MixedGCCountTarget=3");
            flags.add("-XX:G1HeapWastePercent=5");
        }

        // JIT/derleyici tarafinda daha agresif inlining - Minecraft'in
        // sik cagirilan kucuk metotlarinda (render/tick dongusu) belirgin
        // fayda saglar.
        flags.add("-XX:+PerfDisableSharedMem");
        flags.add("-XX:CompileThreshold=1500");
        flags.add("-XX:ParallelGCThreads=" + Math.min(cores, 10));
        flags.add("-XX:ConcGCThreads=" + Math.max(1, Math.min(cores / 2, 5)));

        // Baslangic suresini kisaltmak icin: class-data sharing acik,
        // tiered compilation'in ilk asamasi hizlandirilir.
        flags.add("-Xss1M");
        flags.add("-XX:+UseStringDeduplication");
        // --- Maks Performans ekstra ayarlari ---
        // KRITIK DUZELTME: -XX:TieredStopAtLevel=1 C2 optimizing derleyiciyi
        // tamamen KAPATIR -> JIT sadece C1 ile derler, uzun vadede FPS
        // belirgin dususer (yaygin bir yanlis tavsiye). TieredCompilation
        // zaten varsayilan olarak aciktir; sinirlandirici bayrak kaldirildi.
        // Daha buyuk kod cikartma alani: buyuk mod paketlerinde JIT
        // derlenen metodlarin dolmasini engeller.
        flags.add("-XX:ReservedCodeCacheSize=512M");
        flags.add("-XX:+UseCodeCacheFlushing");
        // Bellek baskisini azaltan ucuz ayarlar:
        flags.add("-XX:+OptimizeStringConcat");
        flags.add("-XX:+UseCompressedOops");
        // NIO sinifi yukleme rekabetini azaltir (acilis aninda FPS
        // dususunu yumusatir).
        flags.add("-Djdk.nio.maxCachedBufferSize=262144");
        // LWJGL'nin off-heap tamponlarini daha hizli tahsis etmesi icin.
        flags.add("-Dorg.lwjgl.util.Debug=false");
        // --- V29 acilis/FPS ekstrasi ---
        // CICompilerCount: JIT derleyici thread sayisini cekirdek sayisina
        // olcekle - az cekirdekte derleyicilerin gc'yi bogmasini, cok
        // cekirdekte derleme kuyrugunun tikanmasini engeller (acilis
        // sinif yuklemesi belirgin hizlanir).
        flags.add("-XX:CICompilerCount=" + Math.max(2, Math.min(cores / 2, 8)));
        // GC thread sayisini yuge/duruma gore dinamik ayarlar - bos tahta
        // CPU harcamasini dusurur, yogun chunk yuklemede thread arttirir.
        flags.add("-XX:+UseDynamicNumberOfGCThreads");

        return flags;
    }

    /** Uyumluluk icin eski imza: oyunun Java surumu bilinmiyorsa launcher'in surumune bakar. */
    public static List<String> recommendedJvmFlags(int ramGB) {
        return PerformanceFlags.recommendedJvmFlags(ramGB, 0);
    }

    /** Launcher'in kendi JVM major surumu (geri donus senaryosu icin). */
    private static int inferLauncherJavaMajor() {
        try {
            String version = System.getProperty("java.version", "17");
            String head = version.split("\\.")[0];
            return Integer.parseInt(head.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 17;
        }
    }

    /** JVM bayraklarini tek bir satirda (bosluklarla ayrilmis) dondurur - extraJvmArgs alanina eklenebilir. */
    public static String recommendedJvmFlagsAsString(int ramGB, int gameJavaMajor) {
        return String.join(" ", PerformanceFlags.recommendedJvmFlags(ramGB, gameJavaMajor));
    }

    /**
     * "Maks Performans" acikken otomatik kurulmasi onerilen mod paketi.
     *
     * V29.1 KURALLI LISTE - SADECE PERFORMANS KAZANDIRAN MODLAR:
     * Politika: buradaki her mod ya FPS arttirir, ya acilis suresini
     * kisaltir, ya da bellek/GC load dusurur. Performans DUSUREN veya
     * performansla ilgisiz olan her sey listeden cikarildi:
     *  - Iris/Oculus (shader destek EKLER; shader'lar FPS dusurur)
     *  - Chunky (dunya on-uretimi = AMACLI YUKsek CPU yuku)
     *  - spark (profilleyici; tarama araci, performans kazandirmaz)
     *  - FPS Reducer / fps-reducer (FPS'i SINIRLAR - kazandirmaz)
     *  - Indium/sodium-extra/reeses-sodium-options/embeddium-plus/
     *    textrues-embeddium-options (Sodium'un AYAR/uyum arayuzleri;
     *    motorun kendisi yeterli)
     *  - Bobby (ekstra chunk dosyalari tutar -> disk/bellek yuku)
     *  - Dynamic View (render mesafesini oynatır; tahmin edilemez)
     *  - Exordium/Ebe (HUD/BEM tampon hileleri; gorunumu bozabilir)
     *  - Smooth Boot (mod paketi thread ayari; modern JVM'de etkisiz)
     *  - debugify (hata duzeltmeleri; performans degil)
     *  - Get It Together Drops! / Alternate Current / ServerCore /
     *    ThreadTweak / fast-ip-ping / async-locator / cull leaves
     *    ailesi / fastanim / biome blend (marjinal veya oyunbasi)
     *
     * Bagimliliklar listenin en basinda tutulur ki once kurulurlar.
     * Loader'a uymayan modlar Modrinth'in surum filtresiyle sessizce
     * atlanir. Cakisma notlari: Forge'da Sodium yerine Embeddium,
     * lithium yerine Canary (portlari ikiser adet kurmak sinif
     * cakismasi uretir).
     */
    public static List<PerfMod> recommendedPerformanceMods(String loader) {
        List<PerfMod> mods = new ArrayList<>();
        String l = loader == null ? "" : loader.toLowerCase();
        boolean fabricLike = l.contains("fabric") || l.contains("quilt") || l.contains("vanilla") || l.isEmpty();
        boolean forgeLike = l.contains("forge") || l.contains("neoforge");

        if (fabricLike) {
            // --- Bagimliliklar (once kurulur) ---
            mods.add(new PerfMod("fabric-api", "Fabric API (temel bagimlilik)"));
            mods.add(new PerfMod("cloth-config", "Cloth Config API"));

            // --- Render motoru ---
            mods.add(new PerfMod("sodium", "Sodium (render motoru)"));
            // V39.1: shader destegi ve Sodium ekstrasi — kullanicinin istegiyle
            // Maks Performans setinin sabit parcalari (oyun ayarlarindan
            // bagimsiz olarak kurulur; shader kullanilmazsa harici yuk yapmaz).
            mods.add(new PerfMod("iris", "Iris (shader motoru)"));
            mods.add(new PerfMod("sodium-extra", "Sodium Extra (ek ayarlar)"));

            // --- Tick / dunya / chunk (net kazanc) ---
            mods.add(new PerfMod("lithium", "Lithium (tick optimizasyonu)"));
            mods.add(new PerfMod("c2me-fabric", "C2ME (paralel chunk uretimi)"));
            mods.add(new PerfMod("krypton", "Krypton (ag optimizasyonu)"));
            mods.add(new PerfMod("ksyxis", "Ksyxis (spawn chunk yukunu kaldirir)"));
            mods.add(new PerfMod("fastload", "Fastload (dunya yukleme)"));

            // --- Bellek / JVM / acilis ---
            mods.add(new PerfMod("ferrite-core", "FerriteCore (RAM dusurucu)"));
            mods.add(new PerfMod("memoryleakfix", "Memory Leak Fix"));
            mods.add(new PerfMod("lazydfu", "LazyDFU (acilis hizlandirici)"));
            mods.add(new PerfMod("modernfix", "ModernFix (acilis/bellek)"));

            // --- Render mikro optimizasyonlari ---
            mods.add(new PerfMod("entityculling", "Entity Culling"));
            mods.add(new PerfMod("moreculling", "More Culling"));
            mods.add(new PerfMod("immediatelyfast", "ImmediatelyFast (batching)"));
            mods.add(new PerfMod("badoptimizations", "BadOptimizations"));
            mods.add(new PerfMod("particle-core", "Particle Core"));

            // --- Diger net kazançlar ---
            mods.add(new PerfMod("dynamic-fps", "Dynamic FPS (arka planda tasarruf)"));
            mods.add(new PerfMod("clumps", "Clumps (XP orb birlestirme)"));
            mods.add(new PerfMod("faster-random", "Faster Random"));
        }

        if (forgeLike) {
            // --- Bagimliliklar (once kurulur) ---
            mods.add(new PerfMod("cloth-config", "Cloth Config API"));
            mods.add(new PerfMod("modernfix", "ModernFix (acilis/bellek)"));

            // --- Render motoru ---
            mods.add(new PerfMod("embeddium", "Embeddium (Sodium render port)"));
            // V39.1: Forge tarafinda shader motoru portlari
            mods.add(new PerfMod("oculus", "Oculus (Iris portu)"));

            // --- Tick / dunya / chunk (net kazanc) ---
            mods.add(new PerfMod("canary", "Canary (lithium portu)"));
            mods.add(new PerfMod("ai-improvements", "AI Improvements"));
            mods.add(new PerfMod("ksyxis", "Ksyxis (spawn chunk yukunu kaldirir)"));
            mods.add(new PerfMod("fastload", "Fastload (dunya yukleme)"));

            // --- Bellek / JVM / acilis ---
            mods.add(new PerfMod("ferrite-core", "FerriteCore (RAM dusurucu)"));
            mods.add(new PerfMod("memoryleakfix", "Memory Leak Fix"));
            mods.add(new PerfMod("saturn", "Saturn (bellek tuketimi)"));

            // --- Render mikro optimizasyonlari ---
            mods.add(new PerfMod("entityculling", "Entity Culling"));
            mods.add(new PerfMod("moreculling", "More Culling"));
            mods.add(new PerfMod("immediatelyfast", "ImmediatelyFast (batching)"));
            mods.add(new PerfMod("badoptimizations", "BadOptimizations"));
            mods.add(new PerfMod("particle-core", "Particle Core"));

            // --- Diger net kazançlar ---
            mods.add(new PerfMod("dynamic-fps", "Dynamic FPS (arka planda tasarruf)"));
            mods.add(new PerfMod("clumps", "Clumps (XP orb birlestirme)"));
        }
        return mods;
    }

    /** Bilgi amacli: Starlight'in otomatik kurulmama sebebi. */
    public static String starlightNote() {
        return "Starlight 1.20+ surumlerde Minecraft'ta yerlesiktir; eski surumlerde otomatik kurulur.";
    }

    /**
     * "Maks Performans" ACILIRKEN mevcut kurulu modlarla CAKISACAK modlarin
     * dosya-adi anahtar kelimeleri. Kural: performans setinin O YUKLEYICI
     * icin kurmayacagi ama ayni isi yapan modlar (karsi yukleyicinin portu,
     * OptiFine, VulkanMod vb.) listeye girer - bunlar kurulumla cakisir ve
     * oyunun hic acilmamasina yol acabilir.
     *
     * Not: karsi yukleyiciden kalmis jar'lar zaten yuklenmez ama dosya
     * clutter yaratir; silinmeleri guvenlidir. Ayrica "sodium-extra" gibi
     * eklentiler bilinçli olarak LISTEDE DEGIL - onlar cakismaz, sadece
     * performans setine dahil edilmedi.
     */
    public static java.util.List<String> conflictKeywords(String loader) {
        java.util.List<String> keys = new ArrayList<>();
        // Her iki yukleyicide de cakisanlar:
        keys.add("optifine");     // Sodium/Embeddium ile tamamen uyumsuz
        keys.add("vulkanmod");    // OpenGL render motoruyla cakisir
        String l = loader == null ? "" : loader.toLowerCase();
        boolean fabricLike = l.contains("fabric") || l.contains("quilt") || l.contains("vanilla") || l.isEmpty();
        boolean forgeLike = l.contains("forge") || l.contains("neoforge");
        if (fabricLike) {
            // Forge portlari - Sodium/Lithium ile cakisir:
            keys.add("embeddium");
            keys.add("rubidium");
            keys.add("magnesium");
            keys.add("canary");
            keys.add("oculus");
        }
        if (forgeLike) {
            // Fabric jar'lari - Forge'ta yuklenmez, cakisma/duzensizlik kaynagi:
            keys.add("sodium");
            keys.add("lithium");
            keys.add("krypton");
            keys.add("c2me");
            keys.add("iris");
        }
        return keys;
    }

    public static class PerfMod {
        public final String slug;
        public final String title;
        public PerfMod(String slug, String title) {
            this.slug = slug;
            this.title = title;
        }
    }
}

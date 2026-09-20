/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.core;

import java.util.HashMap;
import java.util.Map;

public final class L10n {
    private static String currentLang = "tr";
    private static final Map<String, String> TR = new HashMap<String, String>();
    private static final Map<String, String> EN = new HashMap<String, String>();

    private static void r(String string, String string2, String string3) {
        TR.put(string, string2);
        EN.put(string, string3);
    }

    public static void setLanguage(String string) {
        currentLang = string != null && string.equals("en") ? "en" : "tr";
    }

    public static String getLanguage() {
        return currentLang;
    }

    public static boolean isEnglish() {
        return "en".equals(currentLang);
    }

    public static String get(String string) {
        Map<String, String> map = L10n.isEnglish() ? EN : TR;
        return map.getOrDefault(string, string);
    }

    public static String fmt(String string, Object ... objectArray) {
        return String.format(L10n.get(string), objectArray);
    }

    private L10n() {
    }

    static {
        L10n.r("ok", "Tamam", "OK");
        L10n.r("cancel", "\u0130ptal", "Cancel");
        L10n.r("yes", "Evet", "Yes");
        L10n.r("no", "Hay\u0131r", "No");
        L10n.r("error", "Hata", "Error");
        L10n.r("warning", "Uyar\u0131", "Warning");
        L10n.r("info", "Bilgi", "Info");
        L10n.r("close", "Kapat", "Close");
        L10n.r("save", "Kaydet", "Save");
        L10n.r("delete", "Sil", "Delete");
        L10n.r("rename", "Yeniden Adland\u0131r", "Rename");
        L10n.r("browse", "G\u00f6zat", "Browse");
        L10n.r("folder", "Klas\u00f6r", "Folder");
        L10n.r("open_folder", "Klas\u00f6r\u00fc A\u00e7", "Open Folder");
        L10n.r("loading", "Y\u00fckleniyor...", "Loading...");
        L10n.r("refresh", "Yenile", "Refresh");
        L10n.r("install", "Kur", "Install");
        L10n.r("update", "G\u00fcncelle", "Update");
        L10n.r("download", "\u0130ndir", "Download");
        L10n.r("settings", "Ayarlar", "Settings");
        L10n.r("version", "S\u00fcr\u00fcm", "Version");
        L10n.r("language", "Dil", "Language");
        L10n.r("language.tr", "T\u00fcrk\u00e7e", "Turkish");
        L10n.r("language.en", "\u0130ngilizce", "English");
        L10n.r("restart_required", "De\u011fi\u015fiklik i\u00e7in launcher'\u0131 yeniden ba\u015flat\u0131n.", "Restart the launcher to apply changes.");
        L10n.r("export", "D\u0131\u015fa Aktar", "Export");
        L10n.r("import", "\u0130\u00e7e Aktar", "Import");
        L10n.r("back", "Geri", "Back");
        L10n.r("copy", "Kopyala", "Copy");
        L10n.r("search", "Ara", "Search");
        L10n.r("apply", "Uygula", "Apply");
        L10n.r("title", "Complex Launcher v35", "Complex Launcher v35");
        L10n.r("tab.mods", "Modlar", "Mods");
        L10n.r("tab.shaders", "Shaderlar", "Shaders");
        L10n.r("tab.resourcepacks", "Kaynak Paketleri", "Resource Packs");
        L10n.r("tab.modpacks", "Modpacklar", "Modpacks");
        L10n.r("tab.servers", "Sunucular", "Servers");
        L10n.r("tab.settings", "Ayarlar", "Settings");
        L10n.r("instance.new", "Yeni \u00f6rnek", "New instance");
        L10n.r("instance.list", "\u00d6rnekler", "Instances");
        L10n.r("instance.click_hint", "T\u0131kla: se\u00e7 ve Ana Sayfa'ya git  \u00b7  Sa\u011f t\u0131k: d\u00fczenle", "Click: select and go Home  \u00b7  Right-click: edit");
        L10n.r("instance.edit", "\u00d6rne\u011fi D\u00fczenle", "Edit Instance");
        L10n.r("instance.name", "\u00d6rnek ad\u0131:", "Instance name:");
        L10n.r("instance.mc_version", "Minecraft s\u00fcr\u00fcm\u00fc:", "Minecraft version:");
        L10n.r("instance.loader", "Mod y\u00fckleyici:", "Mod loader:");
        L10n.r("instance.jvm_args", "Ekstra JVM arg\u00fcmanlar\u0131:", "Extra JVM arguments:");
        L10n.r("instance.java_path", "\u00d6zel Java yolu:", "Custom Java path:");
        L10n.r("instance.screenshots", "Ekran G\u00f6r\u00fcnt\u00fcleri", "Screenshots");
        L10n.r("instance.screenshots_tip", "screenshots/ klas\u00f6r\u00fcn\u00fc a\u00e7", "Open screenshots/ folder");
        L10n.r("instance.export_tip", "Bu instance'\u0131 ZIP olarak kaydet", "Save this instance as ZIP");
        L10n.r("instance.delete_confirm", "'%s' silinsin mi? (Modlar, d\u00fcnyalar dahil)", "Delete '%s'? (Includes mods, worlds)");
        L10n.r("instance.delete_title", "\u00d6rne\u011fi Sil", "Delete Instance");
        L10n.r("instance.deleted", "\u00d6rnek silindi: ", "Instance deleted: ");
        L10n.r("instance.need_one", "En az bir \u00f6rnek kalmal\u0131.", "At least one instance must remain.");
        L10n.r("instance.invalid_name", "Ge\u00e7ersiz ad.", "Invalid name.");
        L10n.r("instance.name_taken", "Bu adda bir \u00f6rnek zaten var.", "An instance with this name already exists.");
        L10n.r("instance.rename_failed", "Klas\u00f6r yeniden adland\u0131r\u0131lamad\u0131.", "Failed to rename folder.");
        L10n.r("instance.select_first", "\u00d6nce bir instance se\u00e7in!", "Please select an instance first!");
        L10n.r("instance.invalid_name2", "Ge\u00e7ersiz isim! Sadece harf, rakam ve alt \u00e7izgi, en fazla 16 karakter.", "Invalid name! Letters, digits and underscore only, max 16 characters.");
        L10n.r("instance.show_snapshots", "Snapshot'lar\u0131 g\u00f6ster", "Show snapshots");
        L10n.r("launch", "BA\u015eLAT", "LAUNCH");
        L10n.r("launch.launching", "BA\u015eLATIYOR...", "LAUNCHING...");
        L10n.r("launch.version_changed", "S\u00fcr\u00fcm de\u011fi\u015fti: %s \u2014 modlar g\u00fcncelleniyor\u2026", "Version changed: %s \u2014 updating mods\u2026");
        L10n.r("launch.started", "Minecraft ba\u015flat\u0131ld\u0131!", "Minecraft launched!");
        L10n.r("account.not_logged_in", "Giri\u015f yap\u0131lmad\u0131", "Not logged in");
        L10n.r("account.offline_login", "\u00c7evrimd\u0131\u015f\u0131 Giri\u015f", "Offline Login");
        L10n.r("account.ms_login", "Microsoft Giri\u015f", "Microsoft Login");
        L10n.r("crash.title", "Crash Tespit Edildi", "Crash Detected");
        L10n.r("crash.jvm_title", "JVM \u00c7\u00f6kmesi", "JVM Crash");
        L10n.r("crash.history_title", "Crash Ge\u00e7mi\u015fi", "Crash History");
        L10n.r("crash.error_log", "Hata G\u00fcnl\u00fc\u011f\u00fc", "Error Log");
        L10n.r("crash.error_log_empty", "(Hata g\u00fcnl\u00fc\u011f\u00fc bo\u015f)", "(Error log is empty)");
        L10n.r("crash.error_log_notfound", "Hata g\u00fcnl\u00fc\u011f\u00fc bulunamad\u0131.\nlauncher_errors.log hen\u00fcz olu\u015fturulmam\u0131\u015f.", "Error log not found.\nlauncher_errors.log has not been created yet.");
        L10n.r("crash.error_log_dialog", "Hata G\u00fcnl\u00fc\u011f\u00fc \u2014 launcher_errors.log", "Error Log \u2014 launcher_errors.log");
        L10n.r("crash.analysis_title", "Hata Analizi", "Error Analysis");
        L10n.r("crash.mod_issue", "[MOD] Eksik/uyumsuz mod. Modlar\u0131 g\u00fcncellemeyi deneyin.", "[MOD] Missing/incompatible mod. Try updating your mods.");
        L10n.r("crash.oom", "[RAM] Bellek yetersiz (OutOfMemoryError). RAM'i art\u0131r\u0131n.", "[RAM] Out of memory (OutOfMemoryError). Increase your RAM allocation.");
        L10n.r("crash.driver", "[DRIVER] OpenGL/GPU hatas\u0131. Ekran kart\u0131 s\u00fcr\u00fcc\u00fcn\u00fcz\u00fc g\u00fcncelleyin.", "[DRIVER] OpenGL/GPU error. Update your graphics card driver.");
        L10n.r("crash.java", "[JAVA] Java uyumsuzlu\u011fu. Farkl\u0131 bir Java s\u00fcr\u00fcm\u00fc deneyin.", "[JAVA] Java incompatibility. Try a different Java version.");
        L10n.r("crash.mixins", "[MIXIN] Mixin \u00e7ak\u0131\u015fmas\u0131. Mod \u00e7ak\u0131\u015fmas\u0131 olabilir.", "[MIXIN] Mixin conflict. There may be a mod conflict.");
        L10n.r("crash.native", "[NATIVE] JVM native crash. Log dosyas\u0131 analiz edildi.", "[NATIVE] JVM native crash. Log file analyzed.");
        L10n.r("crash.unknown", "Bilinmeyen crash sebebi. Tam log i\u00e7in log sekmesine bak\u0131n.", "Unknown crash cause. See the log tab for the full log.");
        L10n.r("crash.open_report", "Raporu A\u00e7", "Open Report");
        L10n.r("crash.no_history", "Hen\u00fcz crash ge\u00e7mi\u015fi yok.", "No crash history yet.");
        // --- Genisletilmis crash kategorileri: Minecraft'ta yasanabilecek
        // hemen hemen her yaygin hata turu icin ayri, aksiyon onerili
        // mesajlar. Onceden sadece 6 kategori vardi (OOM, mod, mixin,
        // driver, java surumu, bilinmeyen) - artik cok daha genis.
        L10n.r("crash.corrupt_world", "[DUNYA] Kay\u0131tl\u0131 d\u00fcnya/chunk verisi bozulmu\u015f olabilir. D\u00fcnyan\u0131n bir yedeğini geri y\u00fckleyin veya bozuk chunk'lar\u0131 onaran bir arac kullan\u0131n.", "[WORLD] Saved world/chunk data may be corrupted. Restore a world backup or use a chunk-repair tool.");
        L10n.r("crash.port_in_use", "[SUNUCU] Port zaten kullan\u0131mda. Ba\u015fka bir sunucu/uygulama ayn\u0131 portu kullan\u0131yor olabilir - farkl\u0131 bir port deneyin.", "[SERVER] Port already in use. Another server/application may be using the same port - try a different port.");
        L10n.r("crash.class_not_found", "[MOD] Eksik bir s\u0131n\u0131f/ba\u011f\u0131ml\u0131l\u0131k bulunamad\u0131 (ClassNotFoundException/NoClassDefFoundError). Bir mod\u0131n gerektirdiği ba\u015fka bir mod eksik olabilir.", "[MOD] A required class/dependency was not found (ClassNotFoundException/NoClassDefFoundError). A mod may be missing one of its dependencies.");
        L10n.r("crash.stack_overflow", "[MOD] Sonsuz d\u00f6ng\u00fc / a\u015f\u0131r\u0131 derin \u00e7a\u011fr\u0131 zinciri (StackOverflowError). Genelde bir mod hatas\u0131ndan kaynaklan\u0131r.", "[MOD] Infinite loop / excessively deep call chain (StackOverflowError). Usually caused by a mod bug.");
        L10n.r("crash.concurrent_modification", "[MOD] E\u015f zamanl\u0131 veri de\u011fi\u015fikli\u011fi hatas\u0131 (ConcurrentModificationException). Genelde iki mod\u0131n ayn\u0131 veriye ayn\u0131 anda eri\u015fmesinden kaynaklan\u0131r.", "[MOD] Concurrent data modification error (ConcurrentModificationException). Usually caused by two mods accessing the same data at once.");
        L10n.r("crash.texture_pack", "[DOKU] Doku paketi/resource pack y\u00fcklenirken hata olu\u015ftu. Doku paketini kald\u0131r\u0131p tekrar deneyin.", "[TEXTURE] Error while loading a resource pack. Try removing the resource pack and relaunching.");
        L10n.r("crash.shader_error", "[SHADER] Shader paketi y\u00fcklenirken/derlenirken hata olu\u015ftu. Farkl\u0131 bir shader paketi deneyin veya GPU s\u00fcr\u00fcc\u00fcn\u00fcz\u00fc g\u00fcncelleyin.", "[SHADER] Error while loading/compiling a shader pack. Try a different shader pack or update your GPU driver.");
        L10n.r("crash.disk_space", "[DISK] Disk alan\u0131 yetersiz olabilir. Diskinizde bo\u015f alan a\u00e7\u0131n.", "[DISK] You may be out of disk space. Free up some disk space.");
        L10n.r("crash.permission", "[IZIN] Dosya/klas\u00f6r eri\u015fim izni reddedildi. Launcher'\u0131 y\u00f6netici olarak \u00e7al\u0131\u015ft\u0131rmay\u0131 veya antivir\u00fcs\u00fc kontrol etmeyi deneyin.", "[PERMISSION] File/folder access denied. Try running the launcher as administrator or check your antivirus.");
        // --- Crash Analizi v2: en yuksek seviye tani ---
        L10n.r("crash.deep.header", "🔍 CRASH ANALİZİ", "🔍 CRASH ANALYSIS");
        L10n.r("crash.deep.root_cause", "Sebep: %s", "Root cause: %s");
        L10n.r("crash.deep.exception_line", "Hata satırı: %s", "Exception line: %s");
        L10n.r("crash.deep.mod_file", "Sorumlu mod dosyası: %s → bu dosyayı kaldırıp tekrar deneyin.", "Responsible mod file: %s → remove this file and try again.");
        L10n.r("crash.deep.suspected_mods", "Şüpheli modlar: %s", "Suspected mods: %s");
        L10n.r("crash.deep.no_mod_attribution", "Sorumlu mod tespit edilemedi.", "No mod could be attributed.");
        L10n.r("crash.deep.java_version", "Java: çalışan %s / oyunun istediği %s — JVM Profilleri menüsünden eşleştirin.", "Java: running %s / game wants %s — match it via the JVM Profiles menu.");
        L10n.r("crash.deep.java_ok", "Java sürümü uygun (%s).", "Java version is fine (%s).");
        L10n.r("crash.deep.entity", "Sorunlu varlık: %s — o bölgedeki bu varlıkları silmek crash'i çözebilir.", "Problematic entity: %s — removing these entities from that area may fix the crash.");
        L10n.r("crash.deep.shader_pack", "Shader paketi dosyası: %s — kaldırıp tekrar deneyin.", "Shader pack file: %s — remove it and try again.");
        L10n.r("crash.deep.world", "Dünya: %s", "World: %s");
        L10n.r("crash.deep.metaspace", "Metaspace dolu (classloader sızıntısı). -XX:MaxMetaspaceSize değerini artırın.", "Metaspace exhausted (classloader leak). Increase -XX:MaxMetaspaceSize.");
        L10n.r("crash.deep.native_oom", "Bellek sistem seviyesinde bitmiş. Diğer uygulamaları kapatın veya RAM ayırmasını düşürün.", "Memory exhausted at OS level. Close other apps or lower the RAM allocation.");
        L10n.r("crash.deep.fix_header", "Önerilen adımlar:", "Recommended steps:");
        L10n.r("crash.deep.fix_toggle", "Maks Performans'ı kapatın: yeni eklenen ~40 mod loader/sürüm uyumsuzluğu yaratabilir.", "Turn OFF Max Performance: the ~40 newly added mods can conflict on your loader/version.");
        L10n.r("crash.deep.fix_driver", "GPU sürücünü üreticiden (NVIDIA/AMD/Intel) güncel tam sürücü ile güncelleyin.", "Update your GPU driver with the latest full package from the vendor.");
        L10n.r("crash.deep.fix_optifine", "OptiFine'ı kaldırın — Sodium/Embeddium ailesiyle aynı anda çalışamaz.", "Remove OptiFine — it cannot run alongside the Sodium/Embeddium family.");
        L10n.r("crash.deep.fix_vulkan", "VulkanMod'u kaldırın (deneysel; Sodium/Iris ile çakışır).", "Remove VulkanMod (experimental; conflicts with Sodium/Iris).");
        L10n.r("crash.deep.fix_mod_delete", "'%s' dosyasını mods klasöründen çıkarıp oyunu tekrar başlatın.", "Move '%s' out of the mods folder and relaunch the game.");
        L10n.r("crash.deep.fix_deduplicate", "Aynı moda ait eski sürüm dosyalarını mods klasöründen silin.", "Delete the older duplicate version files of the same mod from the mods folder.");
        L10n.r("crash.deep.fix_world_backup", "Dünyanızın otomatik yedeğini geri yükleyin (saves klasörü).", "Restore your world's automatic backup (saves folder).");
        L10n.r("crash.network", "[AG] A\u011f ba\u011flant\u0131 hatas\u0131 (sunucuya ba\u011flan\u0131lamad\u0131 / zaman a\u015f\u0131m\u0131). Internet ba\u011flant\u0131n\u0131z\u0131 veya sunucu adresini kontrol edin.", "[NETWORK] Network connection error (could not connect to server / timeout). Check your internet connection or the server address.");
        L10n.r("crash.auth_error", "[HESAP] Kimlik do\u011frulama hatas\u0131. Microsoft hesab\u0131n\u0131zla tekrar giri\u015f yapmay\u0131 deneyin.", "[ACCOUNT] Authentication error. Try logging in with your Microsoft account again.");
        L10n.r("crash.duplicate_mod", "[MOD] Ayn\u0131 mod\u0131n birden fazla s\u00fcr\u00fcm\u00fc ayn\u0131 anda y\u00fckl\u00fc. Eski/duplike mod dosyalar\u0131n\u0131 kald\u0131r\u0131n.", "[MOD] Multiple versions of the same mod are installed at once. Remove the old/duplicate mod files.");
        L10n.r("crash.mod_version_mismatch", "[MOD] Bir mod\u0131n bu Minecraft/loader s\u00fcr\u00fcm\u00fcyle uyumlu olmayan bir s\u00fcr\u00fcm\u00fc y\u00fckl\u00fc. Modu do\u011fru s\u00fcr\u00fcmle de\u011fi\u015ftirin.", "[MOD] A mod's version is incompatible with this Minecraft/loader version. Replace it with the correct version.");
        L10n.r("crash.gpu_out_of_memory", "[GPU] Ekran kart\u0131 belle\u011fi (VRAM) yetersiz. G\u00f6r\u00fcnt\u00fc mesafesini/shader kalitesini d\u00fc\u015f\u00fcr\u00fcn.", "[GPU] Graphics card memory (VRAM) is insufficient. Lower your render distance/shader quality.");
        L10n.r("crash.null_pointer", "[MOD] Beklenmeyen bo\u015f de\u011fer hatas\u0131 (NullPointerException). Genelde bir mod\u0131n hatal\u0131 kodundan kaynaklan\u0131r - hangi mod oldu\u011funu g\u00f6rmek i\u00e7in tam log'a bak\u0131n.", "[MOD] Unexpected null value error (NullPointerException). Usually caused by a bug in a mod - check the full log to see which one.");
        L10n.r("crash.stackoverflow_worldgen", "[DUNYA URETIMI] D\u00fcnya \u00fcretimi (worldgen) s\u0131ras\u0131nda hata. Bir worldgen mod\u0131 (biome/structure) sorunlu olabilir.", "[WORLDGEN] Error during world generation. A worldgen mod (biome/structure) may be problematic.");
        L10n.r("crash.saveddata_error", "[KAYIT] Oyun verisi kaydedilirken/okunurken hata olu\u015ftu. NBT verisi bozulmu\u015f olabilir.", "[SAVE DATA] Error while saving/reading game data. NBT data may be corrupted.");
        L10n.r("crash.recipe_error", "[TARIF] Bir mod\u0131n crafting tarifi ge\u00e7ersiz/\u00e7ak\u0131\u015f\u0131yor. Mod g\u00fcncellemesi bekleyin veya \u00e7ak\u0131\u015fan modu kald\u0131r\u0131n.", "[RECIPE] A mod's crafting recipe is invalid/conflicting. Wait for a mod update or remove the conflicting mod.");
        L10n.r("crash.datapack_error", "[DATAPACK] Bir datapack/config dosyas\u0131 ge\u00e7ersiz JSON i\u00e7eriyor. Son eklenen datapack/config'i kontrol edin.", "[DATAPACK] A datapack/config file contains invalid JSON. Check the most recently added datapack/config.");
        L10n.r("crash.optifine_conflict", "[OPTIFINE] OptiFine di\u011fer performans modlar\u0131yla (Sodium/Iris/Lithium) \u00e7ak\u0131\u015f\u0131yor olabilir. Bunlar\u0131 birlikte kullanmay\u0131n.", "[OPTIFINE] OptiFine may be conflicting with other performance mods (Sodium/Iris/Lithium). Don't use them together.");
        L10n.r("playtime.total", "Toplam oyun s\u00fcresi", "Total playtime");
        L10n.r("playtime.last_played", "Son oynama", "Last played");
        L10n.r("playtime.never", "Hi\u00e7 oynanmad\u0131", "Never played");
        L10n.r("playtime.today", "Bug\u00fcn", "Today");
        L10n.r("playtime.session", "Bu oturum", "This session");
        L10n.r("skin.title", "Skin Y\u00fckle", "Upload Skin");
        L10n.r("skin.choose_file", "PNG Se\u00e7", "Choose PNG");
        L10n.r("skin.slim_model", "\u0130nce model (Alex)", "Slim model (Alex)");
        L10n.r("skin.apply", "Uygula", "Apply");
        L10n.r("skin.applied", "Skin uyguland\u0131.", "Skin applied.");
        L10n.r("skin.invalid_file", "Ge\u00e7ersiz dosya. PNG se\u00e7in.", "Invalid file. Please select a PNG.");
        L10n.r("skin.no_account", "\u00d6nce giri\u015f yap\u0131n.", "Please log in first.");
        L10n.r("skin.section", "Skin", "Skin");
        L10n.r("world.title", "D\u00fcnyalar", "Worlds");
        L10n.r("world.import", "D\u00fcnya \u0130\u00e7e Aktar", "Import World");
        L10n.r("world.export", "D\u00fcnyay\u0131 D\u0131\u015fa Aktar", "Export World");
        L10n.r("world.import_success", "D\u00fcnya i\u00e7e aktar\u0131ld\u0131: %s", "World imported: %s");
        L10n.r("world.export_success", "D\u00fcnya d\u0131\u015fa aktar\u0131ld\u0131: %s", "World exported: %s");
        L10n.r("world.import_failed", "D\u00fcnya i\u00e7e aktar\u0131lamad\u0131: %s", "Failed to import world: %s");
        L10n.r("world.export_failed", "D\u00fcnya d\u0131\u015fa aktar\u0131lamad\u0131: %s", "Failed to export world: %s");
        L10n.r("world.no_worlds", "Bu instance'ta d\u00fcnya bulunamad\u0131.", "No worlds found in this instance.");
        L10n.r("world.select", "Bir d\u00fcnya se\u00e7in:", "Select a world:");
        L10n.r("world.saves_missing", "saves/ klas\u00f6r\u00fc bulunamad\u0131.", "saves/ folder not found.");
        L10n.r("modpack.export_title", "Modpack D\u0131\u015fa Aktar", "Export Modpack");
        L10n.r("modpack.export_success", "Modpack d\u0131\u015fa aktar\u0131ld\u0131: %s", "Modpack exported: %s");
        L10n.r("modpack.export_failed", "D\u0131\u015fa aktarma hatas\u0131: %s", "Export failed: %s");
        L10n.r("modpack.import_title", "Modpack \u0130\u00e7e Aktar", "Import Modpack");
        L10n.r("modpack.import_success", "Modpack i\u00e7e aktar\u0131ld\u0131: %s", "Modpack imported: %s");
        L10n.r("modpack.import_failed", "\u0130\u00e7e aktarma hatas\u0131: %s", "Import failed: %s");
        L10n.r("mod.conflict_title", "Mod \u00c7ak\u0131\u015fmas\u0131", "Mod Conflict");
        L10n.r("mod.conflict_msg", "Ayn\u0131 mod ID'sinden birden fazla s\u00fcr\u00fcm bulundu:\n%s\nBirini silin.", "Multiple versions of the same mod found:\n%s\nPlease remove one.");
        L10n.r("mod.no_conflicts", "Mod \u00e7ak\u0131\u015fmas\u0131 yok.", "No mod conflicts found.");
        L10n.r("update.checking", "Launcher g\u00fcncellemesi kontrol ediliyor\u2026", "Checking for launcher update\u2026");
        L10n.r("update.available", "Yeni launcher s\u00fcr\u00fcm\u00fc mevcut: %s", "New launcher version available: %s");
        L10n.r("update.changelog", "De\u011fi\u015fiklikler: %s", "Changelog: %s");
        L10n.r("update.confirm", "S\u00fcr\u00fcm %s indirilsin mi?\n%s\nGitHub \u00fczerinden indirilecek.", "Download version %s?\n%s\nWill be downloaded from GitHub.");
        L10n.r("update.confirm_title", "G\u00fcncelleme \u0130ndir", "Download Update");
        L10n.r("update.complete", "G\u00fcncelleme tamamland\u0131: %s", "Update complete: %s");
        L10n.r("update.restart_confirm", "Launcher'\u0131 \u015fimdi yeniden ba\u015flat\u0131ls\u0131n m\u0131?", "Restart the launcher now?");
        L10n.r("update.restart_title", "G\u00fcncelleme Tamamland\u0131", "Update Complete");
        L10n.r("update.error", "G\u00fcncelleme kontrol\u00fc ba\u015far\u0131s\u0131z: %s", "Update check failed: %s");
        L10n.r("update.updating", "Launcher g\u00fcncelleniyor: %s\u2026", "Updating launcher: %s\u2026");
        L10n.r("update.update_error", "G\u00fcncelleme hatas\u0131: %s", "Update error: %s");

        // --- Cevrimdisi Giris diyalogu ---
        L10n.r("offline.title", "\u00c7evrimd\u0131\u015f\u0131 Giri\u015f", "Offline Login");
        L10n.r("offline.player_name", "Oyuncu Ad\u0131", "Player Name");
        L10n.r("offline.hint", "Microsoft hesab\u0131 gerekmez.<br>Yaln\u0131zca tek oyunculu ve offline sunucularda \u00e7al\u0131\u015f\u0131r.", "No Microsoft account required.<br>Works in singleplayer and on offline-mode servers only.");
        L10n.r("offline.placeholder", "Oyuncu ad\u0131n\u0131 girin\u2026", "Enter your player name\u2026");
        L10n.r("offline.login", "Giri\u015f Yap", "Log In");
        L10n.r("offline.cancel", "\u0130ptal", "Cancel");
        L10n.r("offline.err_title", "Hata", "Error");
        L10n.r("offline.err_empty", "L\u00fctfen bir oyuncu ad\u0131 girin.", "Please enter a player name.");
        L10n.r("offline.err_too_long", "Oyuncu ad\u0131 en fazla 16 karakter olabilir.", "Player name can be at most 16 characters.");

        // --- Microsoft Giris diyalogu ---
        L10n.r("msa.title", "Microsoft ile Giri\u015f", "Sign in with Microsoft");
        L10n.r("msa.heading", "Microsoft Hesab\u0131n\u0131zla Giri\u015f", "Sign in with Your Microsoft Account");
        L10n.r("msa.instructions", "Taray\u0131c\u0131da giri\u015f yap\u0131n, ard\u0131ndan y\u00f6nlendirilen adres \u00e7ubu\u011fundaki t\u00fcm URL'yi a\u015fa\u011f\u0131ya yap\u0131\u015ft\u0131r\u0131n.", "Sign in in your browser, then paste the full URL from the redirected address bar below.");
        L10n.r("msa.opening_browser", "Taray\u0131c\u0131 a\u00e7\u0131l\u0131yor\u2026", "Opening browser\u2026");
        L10n.r("msa.browser_opened", "Taray\u0131c\u0131 a\u00e7\u0131ld\u0131 \u2014 giri\u015f yap\u0131p URL'yi yap\u0131\u015ft\u0131r\u0131n.", "Browser opened \u2014 sign in and paste the URL.");
        L10n.r("msa.browser_failed", "Taray\u0131c\u0131 a\u00e7\u0131lamad\u0131. Manuel gidin:", "Could not open browser. Go manually:");
        L10n.r("msa.url_placeholder", "Y\u00f6nlendirme URL'sini buraya yap\u0131\u015ft\u0131r\u0131n\u2026", "Paste the redirect URL here\u2026");
        L10n.r("msa.continue", "Devam Et", "Continue");
        L10n.r("msa.reopen_browser", "Taray\u0131c\u0131y\u0131 Tekrar A\u00e7", "Reopen Browser");
        L10n.r("msa.cancel", "\u0130ptal", "Cancel");
        L10n.r("msa.completing", "Giri\u015f tamamlan\u0131yor\u2026", "Completing login\u2026");
        L10n.r("msa.warn_title", "Uyar\u0131", "Warning");
        L10n.r("msa.err_no_url", "L\u00fctfen y\u00f6nlendirme URL'sini yap\u0131\u015ft\u0131r\u0131n.", "Please paste the redirect URL.");
        L10n.r("msa.err_title", "Hata", "Error");
        L10n.r("msa.err_login_failed", "Giri\u015f ba\u015far\u0131s\u0131z: ", "Login failed: ");

        // --- Sekme adlari ---
        L10n.r("tab.instances", "\u0130nstance'lar", "Instances");
        L10n.r("tab.home", "Ana Sayfa", "Home");
        L10n.r("tab.resourcepacks", "Doku Paketleri", "Resource Packs");
        L10n.r("tab.my_servers", "Sunucu Listem", "My Servers");
        L10n.r("tab.accounts", "Hesaplar", "Accounts");
        L10n.r("tab.settings", "Ayarlar", "Settings");
        L10n.r("log.starting", "Complex Launcher ba\u015flat\u0131l\u0131yor...", "Starting Complex Launcher...");
        L10n.r("log.mods_check", "Modlar kontrol ediliyor...", "Checking mods...");
        L10n.r("log.versions_loading", "S\u00fcr\u00fcmler y\u00fckleniyor...", "Loading versions...");
        L10n.r("log.mc_preparing", "Minecraft haz\u0131rlan\u0131yor...", "Preparing Minecraft...");
        L10n.r("log.splash_tagline", "Surfing the waves of code...", "Surfing the waves of code...");
        L10n.r("log.mod_updates_checking", "Modlar g\u00fcncelleme kontrol\u00fc\u2026", "Checking mod updates\u2026");
        L10n.r("log.all_mods_up_to_date", "T\u00fcm modlar zaten g\u00fcncel.", "All mods are already up to date.");
        L10n.r("jvm.ram", "RAM (GB)", "RAM (GB)");
        L10n.r("jvm.recommend", "Sisteme g\u00f6re g\u00fcvenli max RAM se\u00e7", "Select safe max RAM based on system");
        L10n.r("jvm.recommend_btn", "\u00d6ner", "Suggest");
        L10n.r("server.connect", "Ba\u011flan", "Connect");
        L10n.r("server.add", "Sunucu Ekle", "Add Server");
        L10n.r("server.ping", "Ping", "Ping");
        L10n.r("server.players", "Oyuncular", "Players");
        L10n.r("server.status", "Durum", "Status");
        L10n.r("second_client.name_prompt", "\u0130kinci client i\u00e7in isim girin:", "Enter a name for the second client:");
        L10n.r("second_client.name_title", "\u0130kinci Client", "Second Client");
    }
}


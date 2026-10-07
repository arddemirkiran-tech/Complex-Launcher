/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.lubv.launcher.core.Paths;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

public class Settings {
    public String lastVersion = "";
    public String loader = "Vanilla";
    public int ramGB = 4;
    public String theme = "dark";
    public String backgroundColor = "#202020";
    public String javaPath = "";
    public String jvmArgs = "";
    public String activeInstance = "default";
    public String curseforgeApiKey = "$2a$10$9d8G2Q5rS.xB6MdD3X0NlefGcjZlt8eLfL6osBAQcsct3HfLglskq";
    public int windowWidth = 1280;
    public int windowHeight = 800;
    public String gpuSelection = "Otomatik";
    // "Maks Performans" tuşu: acildiginda launch sirasinda agresif JVM
    // GC/JIT flaglari otomatik eklenir ve en performansli mod paketi
    // (Sodium/Iris turevleri, entity culling, lithium, vb.) otomatik
    // olarak kurulmaya calisilir.
    public boolean maxPerformanceMode = false;
    // "Mod bilgisi oto acilsin": Mods sekmesinde bir mod secilince detay
    // panelinin otomatik acilip acilmayacagi. Ayarlar sekmesinden secilir.
    public boolean modInfoAutoOpen = false;
    // Tam ekran durumu: launcher kapatilirken tam ekrandaysa bir sonraki
    // acilista da tam ekran baslar (F11 ile degisir, burada saklanir).
    public boolean startFullscreen = false;
    // V34: son acik sekmeyi hatirla - launcher bir sonraki acilista ayni
    // sekmeden baslar (varsayilan 1 = Home).
    public int lastTab = 1;
    // V35 TASARIM: "modern" (animasyonlu arka plan efektleri) veya
    // "classic" (mevcut sade tasarim). Varsayilan: classic.
    public String designMode = "classic";
    // V36.1 KISISEL MARKA: kullanici kendi logosunu/fotografini secebilir.
    // Bos string = varsayilan Complex Launcher logasi kullanilir.
    public String customLogoPath = "";
    // Ana sayfa arka planina foto: bos = varsayilan animasyonlu efekt.
    public String homeBgPath = "";
    // V40: TUM PENCERE arka planina foto (butun sekmelerde görünür).
    public String appBgPath = "";
    // V41 FALLING PHOTOS: arka planda yavasca yagan fotolar (tum FX modlarinda).
    // Bos = kapali. Virgulle ayrilmis coklu yol desteklenir.
    public String fallingPhotoPaths = "";
    // V41: yagan foto yogunlugu (0..100). 0=kapali, 100=full.
    public int fallingPhotoDensity = 40;
    // Ana sayfa bas-harf avatarina foto: bos = bas harf cizilir.
    public String accountPhotoPath = "";
    // V39 EASTER EGG: kedi animasyonunun modu.
    // "cat" (varsayilan cizim), "chicken" (tavuk), "photo" (kullanici gorseli sprite olarak)
    public String eggMode = "cat";
    // eggMode == "photo" iken kullanilacak gorsel yolu; bos = kedi cizimi.
    public String eggPhotoPath = "";
    // V39.2: sprite buyuklugu yuzdesi (50..200). 100 = varsayilan kutu (140x92).
    public int eggPhotoScale = 100;
    // V39.4: sprite pin konumu (0..100, sahne yuzdesi; X=sol->sag, Y=ust->alt).
    // -1 = varsayilan yer (sol taraf, zeminin biraz ustu).
    public int eggPinX = -1;
    public int eggPinY = -1;
    public static final String DEFAULT_CURSEFORGE_API_KEY = "$2a$10$9d8G2Q5rS.xB6MdD3X0NlefGcjZlt8eLfL6osBAQcsct3HfLglskq";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static File settingsFile() {
        return new File(Paths.GAME_DIR, "launcher_settings.json");
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static Settings load() {
        File f = Settings.settingsFile();
        if (!f.exists()) return new Settings();
        try (FileReader reader = new FileReader(f);){
            Settings s = GSON.fromJson((Reader)reader, Settings.class);
            if (s == null) return new Settings();
            Settings settings = s;
            return settings;
        }
        catch (JsonSyntaxException | IOException exception) {
            // empty catch block
        }
        return new Settings();
    }

    public void save() {
        try {
            Paths.GAME_DIR.mkdirs();
            try (FileWriter writer = new FileWriter(Settings.settingsFile());){
                GSON.toJson((Object)this, (Appendable)writer);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}


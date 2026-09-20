/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.game.JavaRuntime;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Instance {
    public String name;
    public String lastVersion = "";
    public String loader = "Vanilla";
    public int ramGB = 4;
    public String jvmArgs = "";
    public String javaPath = "";
    public String preLaunchCmd = "";
    public long totalPlaySeconds = 0L;
    public long lastPlayedMs = 0L;
    public List<String> versionHistory = new ArrayList<String>();
    public boolean autoBackup = false;
    public String iconName = "default";
    // Bu instance'a ozel atanmis hesap (kullanici adi). Bos/null ise
    // instance baslatilirken global aktif hesap kullanilir. Bu sayede
    // her instance farkli bir isim/skin ile oynanabilir.
    public String assignedAccountUsername = "";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public File dir() {
        return new File(Paths.INSTANCES_DIR, this.name);
    }

    public File modsDir() {
        return new File(this.dir(), "mods");
    }

    public File shaderpacksDir() {
        return new File(this.dir(), "shaderpacks");
    }

    public File resourcepacksDir() {
        return new File(this.dir(), "resourcepacks");
    }

    private static File configFile(String name) {
        return new File(new File(Paths.INSTANCES_DIR, name), "instance.json");
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static Instance load(String name) {
        Instance inst = new Instance();
        inst.name = name;
        File f = Instance.configFile(name);
        if (!f.exists()) return inst;
        try (FileReader r = new FileReader(f);){
            Instance loaded = GSON.fromJson((Reader)r, Instance.class);
            if (loaded == null) return inst;
            loaded.name = name;
            Instance instance = loaded;
            return instance;
        }
        catch (Exception exception) {
            // empty catch block
        }
        return inst;
    }

    public int getJavaMajorVersion() {
        String exe;
        String path = this.javaPath != null && !this.javaPath.isBlank() ? this.javaPath : null;
        String string = exe = File.separatorChar == '\\' ? "java.exe" : "java";
        if (path != null && !path.isBlank()) {
            int major;
            int major2;
            File f = new File(path);
            if (f.isFile() && (major2 = JavaRuntime.detectMajor(path)) > 0) {
                return major2;
            }
            File bin = new File(new File(path, "bin"), exe);
            if (bin.isFile() && (major = JavaRuntime.detectMajor(bin.getAbsolutePath())) > 0) {
                return major;
            }
        }
        return JavaRuntime.detectMajor(new File(new File(System.getProperty("java.home"), "bin"), exe).getAbsolutePath());
    }

    public void save() {
        try {
            this.dir().mkdirs();
            this.modsDir().mkdirs();
            try (FileWriter w = new FileWriter(Instance.configFile(this.name));){
                GSON.toJson((Object)this, (Appendable)w);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Bu instance'in klasorunu (ve dolayisiyla ismini) diskte yeniden
     * adlandirir. Instance ismi klasor adiyla birebir eslendigi icin
     * gercek bir "rename" islemi klasoru tasimayi gerektirir.
     * Basarili olursa this.name guncellenir ve true doner.
     */
    public boolean renameTo(String newName) {
        if (newName == null) {
            return false;
        }
        String trimmed = newName.trim();
        if (trimmed.isEmpty() || trimmed.equals(this.name)) {
            return false;
        }
        // Dosya sistemi icin guvenli olmayan karakterleri temizle.
        String safeName = trimmed.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (safeName.isEmpty()) {
            return false;
        }
        File oldDir = this.dir();
        File newDir = new File(Paths.INSTANCES_DIR, safeName);
        if (newDir.exists()) {
            return false;
        }
        if (!oldDir.exists()) {
            this.name = safeName;
            this.save();
            return true;
        }
        if (!oldDir.renameTo(newDir)) {
            return false;
        }
        this.name = safeName;
        this.save();
        return true;
    }
}


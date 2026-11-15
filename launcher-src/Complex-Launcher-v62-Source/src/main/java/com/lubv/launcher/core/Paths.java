/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.core;

import java.io.File;
import java.net.URI;

public final class Paths {
    public static final File GAME_DIR = Paths.resolveGameDir();
    public static final File VERSIONS_DIR = new File(GAME_DIR, "versions");
    public static final File LIBRARIES_DIR = new File(GAME_DIR, "libraries");
    public static final File ASSETS_DIR = new File(GAME_DIR, "assets");
    public static final File ASSET_OBJECTS_DIR = new File(ASSETS_DIR, "objects");
    public static final File ASSET_INDEXES_DIR = new File(ASSETS_DIR, "indexes");
    public static final File NATIVES_ROOT_DIR = new File(GAME_DIR, "natives");
    public static final File RUNTIME_DIR = new File(GAME_DIR, "runtime");
    public static final File MODS_DIR = new File(GAME_DIR, "mods");
    public static final File INSTANCES_DIR = new File(GAME_DIR, "instances");
    public static final File RESOURCEPACKS_DIR = new File(GAME_DIR, "resourcepacks");
    public static final File SHADERPACKS_DIR = new File(GAME_DIR, "shaderpacks");
    public static final File SAVES_DIR = new File(GAME_DIR, "saves");
    public static final File AUTH_CACHE_FILE = new File(GAME_DIR, "auth_cache.json");
    public static final File LOGS_DIR = new File(GAME_DIR, "logs");
    public static final boolean PORTABLE = Paths.detectPortable();

    private Paths() {
    }

    private static File resolveGameDir() {
        File jarDir;
        if (PORTABLE && (jarDir = Paths.getLauncherDir()) != null) {
            File dataDir = new File(jarDir, "data");
            dataDir.mkdirs();
            return dataDir;
        }
        return Paths.resolveSystemDir();
    }

    public static File getLauncherDir() {
        try {
            String cp = System.getProperty("java.class.path");
            if (cp != null && !cp.isEmpty()) {
                String first = cp.split(File.pathSeparator)[0];
                File f = new File(first);
                if (f.isFile()) {
                    return f.getAbsoluteFile().getParentFile();
                }
                if (f.isDirectory()) {
                    return f.getAbsoluteFile();
                }
            }
        }
        catch (Exception cp) {
            // empty catch block
        }
        try {
            URI uri = Paths.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File f = new File(uri);
            if (f.isFile()) {
                return f.getAbsoluteFile().getParentFile();
            }
            if (f.isDirectory()) {
                return f.getAbsoluteFile();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new File(System.getProperty("user.dir")).getAbsoluteFile();
    }

    private static boolean detectPortable() {
        File dir = Paths.getLauncherDir();
        if (dir == null) {
            return false;
        }
        if (new File(dir, "runtime").isDirectory()) {
            return true;
        }
        if (new File(dir, "data").isDirectory()) {
            return true;
        }
        return "true".equalsIgnoreCase(System.getProperty("complex.portable"));
    }

    private static File resolveSystemDir() {
        File legacy;
        File base;
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            base = new File(appData != null ? appData : home, "ComplexLauncher");
            legacy = new File(appData != null ? appData : home, "LUBVLauncher");
        } else if (os.contains("mac")) {
            base = new File(home, "Library/Application Support/ComplexLauncher");
            legacy = new File(home, "Library/Application Support/LUBVLauncher");
        } else {
            base = new File(home, ".complexlauncher");
            legacy = new File(home, ".lubvlauncher");
        }
        try {
            if (legacy.isDirectory() && !base.exists()) {
                legacy.renameTo(base);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return base;
    }

    public static void ensureAll() {
        for (File d : new File[]{GAME_DIR, VERSIONS_DIR, LIBRARIES_DIR, ASSETS_DIR, ASSET_OBJECTS_DIR, ASSET_INDEXES_DIR, NATIVES_ROOT_DIR, RUNTIME_DIR, MODS_DIR, INSTANCES_DIR, RESOURCEPACKS_DIR, SHADERPACKS_DIR, SAVES_DIR, LOGS_DIR}) {
            d.mkdirs();
        }
    }
}


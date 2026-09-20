/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class OsRules {
    public static final Os CURRENT_OS = OsRules.detectOs();
    public static final String CURRENT_ARCH = OsRules.detectArch();

    private OsRules() {
    }

    private static Os detectOs() {
        String string = System.getProperty("os.name").toLowerCase();
        if (string.contains("win")) {
            return Os.WINDOWS;
        }
        if (string.contains("mac") || string.contains("darwin")) {
            return Os.MAC;
        }
        return Os.LINUX;
    }

    private static String detectArch() {
        String string = System.getProperty("os.arch").toLowerCase();
        if (string.contains("aarch64") || string.contains("arm64")) {
            return "arm64";
        }
        if (string.contains("arm")) {
            return "arm";
        }
        return "x64";
    }

    public static boolean isAllowed(JsonArray jsonArray) {
        if (jsonArray == null || jsonArray.size() == 0) {
            return true;
        }
        boolean bl = false;
        for (int i = 0; i < jsonArray.size(); ++i) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String string = jsonObject.get("action").getAsString();
            boolean bl2 = true;
            if (jsonObject.has("os")) {
                String string2;
                JsonObject jsonObject2 = jsonObject.getAsJsonObject("os");
                if (jsonObject2.has("name")) {
                    string2 = jsonObject2.get("name").getAsString();
                    Os parsed = string2.equals("windows") ? Os.WINDOWS : (string2.equals("osx") ? Os.MAC : Os.LINUX);
                    if (parsed != CURRENT_OS) {
                        bl2 = false;
                    }
                }
                if (jsonObject2.has("arch") && !(string2 = jsonObject2.get("arch").getAsString()).equals(CURRENT_ARCH)) {
                    bl2 = false;
                }
            }
            if (jsonObject.has("features")) {
                bl2 = false;
            }
            if (!bl2) continue;
            bl = string.equals("allow");
        }
        return bl;
    }

    public static String nativeClassifier() {
        switch (CURRENT_OS) {
            case WINDOWS: {
                return "arm64".equals(CURRENT_ARCH) ? "natives-windows-arm64" : "natives-windows";
            }
            case MAC: {
                return "arm64".equals(CURRENT_ARCH) ? "natives-macos-arm64" : "natives-macos";
            }
        }
        return "arm64".equals(CURRENT_ARCH) ? "natives-linux-arm64" : "natives-linux";
    }

    public static enum Os {
        WINDOWS,
        MAC,
        LINUX;

    }
}


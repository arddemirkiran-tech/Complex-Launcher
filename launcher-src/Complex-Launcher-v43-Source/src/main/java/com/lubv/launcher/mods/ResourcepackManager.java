/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.mods;

import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.mods.ModrinthApi;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ResourcepackManager {
    private ResourcepackManager() {
    }

    public static List<String> listInstalled(File dir) {
        if (!dir.isDirectory()) {
            return new ArrayList<String>();
        }
        File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".zip"));
        if (files == null) {
            return new ArrayList<String>();
        }
        ArrayList<String> names = new ArrayList<String>();
        for (File f : files) {
            names.add(f.getName());
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public static boolean isInstalled(File dir, String fileName) {
        return new File(dir, fileName).exists();
    }

    public static void install(File dir, ModrinthApi.ModVersion version) throws IOException {
        ResourcepackManager.install(dir, version, null);
    }

    public static void install(File dir, ModrinthApi.ModVersion version, HttpUtil.ByteProgress progress) throws IOException {
        dir.mkdirs();
        HttpUtil.downloadFile(version.downloadUrl, new File(dir, version.fileName), progress);
    }

    public static void remove(File dir, String fileName) {
        new File(dir, fileName).delete();
    }
}


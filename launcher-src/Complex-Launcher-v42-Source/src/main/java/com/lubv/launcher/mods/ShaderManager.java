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

public final class ShaderManager {
    private ShaderManager() {
    }

    public static List<String> listInstalled(File shaderDir) {
        if (!shaderDir.isDirectory()) {
            return new ArrayList<String>();
        }
        File[] files = shaderDir.listFiles((d, n) -> n.toLowerCase().endsWith(".zip"));
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

    public static boolean isInstalled(File shaderDir, String fileName) {
        return new File(shaderDir, fileName).exists();
    }

    public static void install(File shaderDir, ModrinthApi.ModVersion version) throws IOException {
        ShaderManager.install(shaderDir, version, null);
    }

    public static void install(File shaderDir, ModrinthApi.ModVersion version, HttpUtil.ByteProgress progress) throws IOException {
        shaderDir.mkdirs();
        HttpUtil.downloadFile(version.downloadUrl, new File(shaderDir, version.fileName), progress);
    }

    public static void remove(File shaderDir, String fileName) {
        new File(shaderDir, fileName).delete();
    }
}


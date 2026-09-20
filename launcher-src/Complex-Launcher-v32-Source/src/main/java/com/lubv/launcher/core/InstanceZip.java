/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.core;

import com.lubv.launcher.core.Instance;
import com.lubv.launcher.core.InstanceManager;
import com.lubv.launcher.core.ProgressListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class InstanceZip {
    private static final String[] INCLUDE_DIRS = new String[]{"mods", "resourcepacks", "shaderpacks", "saves", "config", "screenshots"};

    private InstanceZip() {
    }

    public static void export(Instance instance, File destZip, ProgressListener progress) throws IOException {
        File instanceDir = instance.dir();
        InstanceZip.report(progress, 0, "Export haz\u0131rlan\u0131yor...");
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destZip)));){
            File modsJson;
            zos.setLevel(1);
            File meta = new File(instanceDir, "instance.json");
            if (meta.exists()) {
                InstanceZip.addFile(zos, meta, "instance.json");
            }
            if ((modsJson = new File(new File(instanceDir, "mods"), "installed_mods.json")).exists()) {
                InstanceZip.addFile(zos, modsJson, "mods/installed_mods.json");
            }
            int total = INCLUDE_DIRS.length;
            for (int i = 0; i < total; ++i) {
                String dir = INCLUDE_DIRS[i];
                File sub = new File(instanceDir, dir);
                if (!sub.isDirectory()) continue;
                InstanceZip.report(progress, i * 80 / total, dir + "ekleniyor...");
                InstanceZip.addDir(zos, sub, dir + "/");
            }
        }
        InstanceZip.report(progress, 100, "Export tamamland\u0131.");
    }

    public static Instance importZip(File zipFile, String instanceName, ProgressListener progress) throws IOException {
        InstanceZip.report(progress, 0, "Import ba\u015flat\u0131l\u0131yor...");
        InstanceManager.create(instanceName);
        Instance inst = Instance.load(instanceName);
        File destDir = inst.dir();
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));){
            ZipEntry entry;
            int count = 0;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                File out = InstanceZip.safeResolve(destDir, name);
                if (entry.isDirectory()) {
                    out.mkdirs();
                } else {
                    Instance loaded;
                    out.getParentFile().mkdirs();
                    try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(out));){
                        int n;
                        byte[] buf = new byte[8192];
                        while ((n = zis.read(buf)) != -1) {
                            ((OutputStream)os).write(buf, 0, n);
                        }
                    }
                    if (name.equals("instance.json") && (loaded = Instance.load(instanceName)) != null) {
                        loaded.name = instanceName;
                        loaded.save();
                        inst.lastVersion = loaded.lastVersion;
                        inst.loader = loaded.loader;
                        inst.ramGB = loaded.ramGB;
                        inst.jvmArgs = loaded.jvmArgs;
                        inst.javaPath = loaded.javaPath;
                    }
                }
                if (++count % 10 == 0) {
                    InstanceZip.report(progress, Math.min(90, count / 2), "Dosyalar \u00e7\u0131kar\u0131l\u0131yor...");
                }
                zis.closeEntry();
            }
        }
        inst.name = instanceName;
        inst.save();
        InstanceZip.report(progress, 100, "Import tamamland\u0131: " + instanceName);
        return inst;
    }

    private static void addDir(ZipOutputStream zos, File dir, String prefix) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            String entryName = prefix + f.getName();
            if (f.isDirectory()) {
                InstanceZip.addDir(zos, f, entryName + "/");
                continue;
            }
            InstanceZip.addFile(zos, f, entryName);
        }
    }

    private static void addFile(ZipOutputStream zos, File f, String name) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        entry.setLastModifiedTime(FileTime.fromMillis(f.lastModified()));
        zos.putNextEntry(entry);
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));){
            int n;
            byte[] buf = new byte[8192];
            while ((n = ((InputStream)in).read(buf)) != -1) {
                zos.write(buf, 0, n);
            }
        }
        zos.closeEntry();
    }

    private static File safeResolve(File base, String name) {
        File f = new File(base, name).getAbsoluteFile();
        if (!f.getPath().startsWith(base.getAbsolutePath())) {
            throw new IllegalArgumentException("Ge\u00e7ersiz ZIP yolu: " + name);
        }
        return f;
    }

    private static void report(ProgressListener p, int pct, String msg) {
        if (p != null) {
            p.onProgress(pct, msg);
        }
    }

    public static void exportSavesDir(File savesDir, File destZip) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destZip)));){
            zos.setLevel(1);
            InstanceZip.addDir(zos, savesDir, "saves/");
        }
    }
}


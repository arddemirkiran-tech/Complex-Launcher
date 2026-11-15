/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.mods;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.Instance;
import com.lubv.launcher.core.InstanceManager;
import com.lubv.launcher.core.ProgressListener;
import com.lubv.launcher.mods.CurseForgeApi;
import com.lubv.launcher.mods.ModrinthApi;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ModpackManager {
    private ModpackManager() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static ModpackInfo install(ModrinthApi.ModResult modpack, ModrinthApi.ModVersion version, Consumer<String> log, ProgressListener progress) throws Exception {
        File tmpDir = Files.createTempDirectory("mrpack-", new FileAttribute[0]).toFile();
        try {
            File archive = new File(tmpDir, version.fileName);
            log.accept("Modpack indiriliyor: " + modpack.title + " (" + version.versionNumber + ")");
            ModpackManager.report(progress, 0, "Modpack indiriliyor");
            HttpUtil.downloadFile(version.downloadUrl, archive, (done, total) -> ModpackManager.report(progress, total > 0L ? (int)(done * 8L / total) : -1, "Modpack indiriliyor"));
            ModpackManager.report(progress, 8, "Modpack a\u00e7\u0131l\u0131yor");
            File extracted = new File(tmpDir, "extracted");
            ModpackManager.unzip(archive, extracted);
            File indexFile = new File(extracted, "modrinth.index.json");
            if (!indexFile.isFile()) {
                throw new IOException("modrinth.index.json bulunamad\u0131 - dosya bir modpack olmayabilir");
            }
            JsonObject index = JsonParser.parseString(new String(Files.readAllBytes(indexFile.toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
            String mcVersion = index.get("versionId").getAsString();
            String name = index.has("name") && !index.get("name").isJsonNull() ? index.get("name").getAsString() : modpack.title;
            String loader = ModpackManager.mapLoader(index.has("dependencies") ? index.getAsJsonObject("dependencies") : null);
            String instanceName = ModpackManager.uniqueName(name);
            Instance inst = InstanceManager.create(instanceName);
            inst.lastVersion = mcVersion;
            inst.loader = loader;
            inst.save();
            File destDir = inst.dir();
            log.accept("\u00d6rnek olu\u015fturuldu: " + instanceName + " (MC " + mcVersion + ", " + loader + ")");
            JsonArray files = index.has("files") ? index.getAsJsonArray("files") : new JsonArray();
            int count = 0;
            for (int i = 0; i < files.size(); ++i) {
                String sha1;
                String path;
                File dest;
                JsonObject f = files.get(i).getAsJsonObject();
                if (!ModpackManager.isClientSide(f) || HttpUtil.sha1Matches(dest = ModpackManager.safeResolve(destDir, path = f.get("path").getAsString()), sha1 = ModpackManager.sha1(f))) continue;
                String url = ModpackManager.pickDownloadUrl(f.getAsJsonArray("downloads"));
                log.accept("  [" + (i + 1) + "/" + files.size() + "] " + path);
                int fileIndex = i;
                int totalFiles = files.size();
                HttpUtil.downloadFile(url, dest, (done, total) -> {
                    double fileFrac = total > 0L ? (double)done / (double)total : 0.0;
                    int pct = (int)Math.min(95.0, 8.0 + ((double)fileIndex + fileFrac) * 87.0 / (double)Math.max(totalFiles, 1));
                    ModpackManager.report(progress, pct, "\u0130ndiriliyor: " + path);
                });
                ++count;
            }
            ModpackManager.report(progress, 97, "Yap\u0131land\u0131rma kopyalan\u0131yor");
            File overrides = new File(extracted, "overrides");
            if (overrides.isDirectory()) {
                ModpackManager.copyRecursive(overrides, destDir);
            }
            ModpackInfo info = new ModpackInfo();
            info.instanceName = instanceName;
            info.mcVersion = mcVersion;
            info.loader = loader;
            info.fileCount = count;
            ModpackManager.report(progress, 100, "Kurulum tamamland\u0131");
            ModpackInfo modpackInfo = info;
            return modpackInfo;
        }
        finally {
            ModpackManager.deleteRecursive(tmpDir);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static ModpackInfo installCurseforge(String apiKey, CurseForgeApi.ModResult modpack, CurseForgeApi.FileResult file, Consumer<String> log, ProgressListener progress) throws Exception {
        String downloadUrl = CurseForgeApi.getDownloadUrl(apiKey, modpack.id, file.id);
        if (downloadUrl == null) {
            throw new IOException("\u0130ndirme adresi al\u0131namad\u0131");
        }
        File tmpDir = Files.createTempDirectory("cfpack-", new FileAttribute[0]).toFile();
        try {
            File archive = new File(tmpDir, file.fileName);
            log.accept("Modpack indiriliyor: " + modpack.name);
            ModpackManager.report(progress, 0, "Modpack indiriliyor");
            HttpUtil.downloadFile(downloadUrl, archive, (done, total) -> ModpackManager.report(progress, total > 0L ? (int)(done * 8L / total) : -1, "Modpack indiriliyor"));
            ModpackManager.report(progress, 8, "Modpack a\u00e7\u0131l\u0131yor");
            File extracted = new File(tmpDir, "extracted");
            ModpackManager.unzip(archive, extracted);
            File manifestFile = new File(extracted, "manifest.json");
            if (!manifestFile.isFile()) {
                throw new IOException("manifest.json bulunamad\u0131");
            }
            JsonObject manifest = JsonParser.parseString(new String(Files.readAllBytes(manifestFile.toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject mc = manifest.getAsJsonObject("minecraft");
            String mcVersion = mc.get("version").getAsString();
            String loader = ModpackManager.mapCfLoader(mc);
            String name = manifest.has("name") && !manifest.get("name").isJsonNull() ? manifest.get("name").getAsString() : modpack.name;
            String instanceName = ModpackManager.uniqueName(name);
            Instance inst = InstanceManager.create(instanceName);
            inst.lastVersion = mcVersion;
            inst.loader = loader;
            inst.save();
            File destDir = inst.dir();
            File modsDir = inst.modsDir();
            log.accept("\u00d6rnek olu\u015fturuldu: " + instanceName + " (MC " + mcVersion + ", " + loader + ")");
            JsonArray files = manifest.getAsJsonArray("files");
            int count = 0;
            int i = 0;
            while (i < files.size()) {
                JsonObject f = files.get(i).getAsJsonObject();
                int projectID = f.get("projectID").getAsInt();
                int fileID = f.get("fileID").getAsInt();
                String url = CurseForgeApi.getDownloadUrl(apiKey, projectID, fileID);
                String fileName = ModpackManager.fileNameFromUrl(url);
                File dest = new File(modsDir, fileName);
                log.accept("  [" + (i + 1) + "/" + files.size() + "] " + fileName);
                int fileIndex = i++;
                int totalFiles = files.size();
                HttpUtil.downloadFile(url, dest, (done, total) -> {
                    double fileFrac = total > 0L ? (double)done / (double)total : 0.0;
                    int pct = (int)Math.min(95.0, 8.0 + ((double)fileIndex + fileFrac) * 87.0 / (double)Math.max(totalFiles, 1));
                    ModpackManager.report(progress, pct, "\u0130ndiriliyor: " + fileName);
                });
                ++count;
            }
            ModpackManager.report(progress, 97, "Yap\u0131land\u0131rma kopyalan\u0131yor");
            File overrides = new File(extracted, "overrides");
            if (overrides.isDirectory()) {
                ModpackManager.copyRecursive(overrides, destDir);
            }
            ModpackInfo info = new ModpackInfo();
            info.instanceName = instanceName;
            info.mcVersion = mcVersion;
            info.loader = loader;
            info.fileCount = count;
            ModpackManager.report(progress, 100, "Kurulum tamamland\u0131");
            ModpackInfo modpackInfo = info;
            return modpackInfo;
        }
        finally {
            ModpackManager.deleteRecursive(tmpDir);
        }
    }

    private static String mapCfLoader(JsonObject mc) {
        if (mc.has("modLoaders") && mc.get("modLoaders").isJsonArray()) {
            JsonArray loaders = mc.getAsJsonArray("modLoaders");
            for (int i = 0; i < loaders.size(); ++i) {
                String id;
                JsonObject l = loaders.get(i).getAsJsonObject();
                String string = id = l.has("id") ? l.get("id").getAsString() : "";
                if (id.startsWith("fabric-") || id.startsWith("quilt-")) {
                    return "Fabric";
                }
                if (id.startsWith("neoforge-")) {
                    return "NeoForge";
                }
                if (!id.startsWith("forge-")) continue;
                return "Forge";
            }
        }
        return "Vanilla";
    }

    private static String fileNameFromUrl(String url) {
        try {
            String path = URI.create(url).getPath();
            String name = path.substring(path.lastIndexOf(47) + 1);
            return URLDecoder.decode(name, StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            return "mod-" + System.currentTimeMillis() + ".jar";
        }
    }

    private static void report(ProgressListener p, int percent, String stage) {
        if (p != null) {
            p.onProgress(percent, stage);
        }
    }

    private static boolean isClientSide(JsonObject f) {
        if (!f.has("env") || !f.get("env").isJsonObject()) {
            return true;
        }
        JsonObject env = f.getAsJsonObject("env");
        return !env.has("server") || !"unsupported".equals(env.get("server").getAsString());
    }

    private static String sha1(JsonObject f) {
        JsonObject h;
        if (f.has("hashes") && f.get("hashes").isJsonObject() && (h = f.getAsJsonObject("hashes")).has("sha1") && !h.get("sha1").isJsonNull()) {
            return h.get("sha1").getAsString();
        }
        return null;
    }

    private static String pickDownloadUrl(JsonArray downloads) {
        for (int i = 0; i < downloads.size(); ++i) {
            String u = downloads.get(i).getAsString();
            if (!u.startsWith("https://")) continue;
            return u;
        }
        return downloads.get(0).getAsString();
    }

    private static String mapLoader(JsonObject deps) {
        if (deps == null) {
            return "Vanilla";
        }
        if (deps.has("fabric-loader") || deps.has("quilt-loader")) {
            return "Fabric";
        }
        if (deps.has("neoforge")) {
            return "NeoForge";
        }
        if (deps.has("forge")) {
            return "Forge";
        }
        return "Vanilla";
    }

    private static String uniqueName(String base) {
        String clean = base.replaceAll("[^\\w\\- .]", "").trim();
        if (clean.isEmpty()) {
            clean = "Modpack";
        }
        String candidate = clean;
        int i = 2;
        while (InstanceManager.listNames().contains(candidate)) {
            candidate = clean + " (" + i + ")";
            ++i;
        }
        return candidate;
    }

    private static void unzip(File zipFile, File destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));){
            ZipEntry entry;
            byte[] buf = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                File out = ModpackManager.safeResolve(destDir, entry.getName());
                if (entry.isDirectory()) {
                    out.mkdirs();
                    continue;
                }
                out.getParentFile().mkdirs();
                try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(out));){
                    int n;
                    while ((n = zis.read(buf)) != -1) {
                        ((OutputStream)os).write(buf, 0, n);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private static void copyRecursive(File src, File dest) throws IOException {
        File[] children = src.listFiles();
        if (children == null) {
            return;
        }
        for (File c : children) {
            File target = new File(dest, c.getName());
            if (c.isDirectory()) {
                target.mkdirs();
                ModpackManager.copyRecursive(c, target);
                continue;
            }
            target.getParentFile().mkdirs();
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(c));
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target));){
                int n;
                byte[] buf = new byte[8192];
                while ((n = ((InputStream)in).read(buf)) != -1) {
                    ((OutputStream)out).write(buf, 0, n);
                }
            }
        }
    }

    private static void deleteRecursive(File f) {
        File[] children = f.listFiles();
        if (children != null) {
            for (File c : children) {
                ModpackManager.deleteRecursive(c);
            }
        }
        f.delete();
    }

    private static File safeResolve(File base, String name) {
        File f = new File(base, name).getAbsoluteFile();
        String basePath = base.getAbsolutePath();
        if (!f.getPath().startsWith(basePath)) {
            throw new IllegalArgumentException("Ge\u00e7ersiz ar\u015fiv yolu: " + name);
        }
        return f;
    }

    public static class ModpackInfo {
        public String instanceName;
        public String mcVersion;
        public String loader;
        public int fileCount;
    }
}


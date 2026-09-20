/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.game.GameVersion;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ForgeInstaller {
    private static final String MAVEN_BASE = "https://maven.minecraftforge.net/net/minecraftforge/forge/";
    private static final String PROMOTIONS_URL = "https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json";

    public static String getRecommendedForgeVersion(String string) throws IOException {
        JsonObject jsonObject;
        // V29 INTERNETSIZ CALISMA: Forge promotions JSON'u da surum bilgisi.
        try {
            jsonObject = HttpUtil.getJson(PROMOTIONS_URL);
            com.lubv.launcher.core.LocalCache.putJson("forge:promotions", jsonObject.toString());
        }
        catch (IOException offline) {
            String cached = com.lubv.launcher.core.LocalCache.getJson("forge:promotions");
            if (cached == null) {
                throw offline;
            }
            jsonObject = com.google.gson.JsonParser.parseString(cached).getAsJsonObject();
        }
        JsonObject jsonObject2 = jsonObject.getAsJsonObject("promos");
        String string2 = string + "-recommended";
        String string3 = string + "-latest";
        if (jsonObject2.has(string2)) {
            return jsonObject2.get(string2).getAsString();
        }
        if (jsonObject2.has(string3)) {
            return jsonObject2.get(string3).getAsString();
        }
        throw new IOException("Bu Minecraft s\u00fcr\u00fcm\u00fc i\u00e7in Forge bulunamad\u0131: " + string);
    }

    public static GameVersion installForge(String string, String string2, String string3, Consumer<String> consumer) throws IOException {
        String string4;
        File file;
        String string5 = string + "-" + string2;
        String string6 = MAVEN_BASE + string5 + "/forge-" + string5 + "-installer.jar";
        String string7 = "forge-" + string5;
        String string8 = string + "-forge-" + string2;
        File file2 = new File(Paths.GAME_DIR, "forge_installers/forge-" + string5 + "-installer.jar");
        file2.getParentFile().mkdirs();
        if (!file2.exists()) {
            consumer.accept("Forge installer indiriliyor (" + string5 + ")...");
            HttpUtil.downloadFile(string6, file2);
        }
        if (!(file = new File(Paths.VERSIONS_DIR, (string4 = ForgeInstaller.detectForgeProfileId(file2, string7, string8, consumer)) + "/" + string4 + ".json")).exists()) {
            Object object;
            File file3 = new File(Paths.GAME_DIR, "launcher_profiles.json");
            if (!file3.exists()) {
                object = new FileWriter(file3);
                try {
                    ((Writer)object).write("{\"profiles\":{},\"selectedProfile\":\"\",\"clientToken\":\"complex-launcher\",\"authenticationDatabase\":{}}");
                }
                finally {
                    ((OutputStreamWriter)object).close();
                }
            }
            consumer.accept("Forge resmi installer \u00e7al\u0131\u015ft\u0131r\u0131l\u0131yor (ilk kurulum 1-3 dakika s\u00fcrebilir)...");
            consumer.accept("Not: Forge binary patch i\u015flemi yap\u0131yor \u2014 l\u00fctfen bekleyin.");
            try {
                object = new ProcessBuilder(string3, "-jar", file2.getAbsolutePath(), "--installClient", Paths.GAME_DIR.getAbsolutePath());
                ((ProcessBuilder)object).directory(Paths.GAME_DIR);
                ((ProcessBuilder)object).redirectErrorStream(true);
                Process process = ((ProcessBuilder)object).start();
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                    String string9;
                    while ((string9 = bufferedReader.readLine()) != null) {
                        String string10 = string9.trim();
                        if (string10.isEmpty() || !string10.startsWith("Processor:") && !string10.startsWith("Installing") && !string10.startsWith("Successfully") && !string10.contains("error") && !string10.contains("Error") && !string10.contains("There was") && !string10.startsWith("Extracting") && !string10.startsWith("Downloading")) continue;
                        consumer.accept("  " + string10);
                    }
                }
                int n = process.waitFor();
                if (n != 0) {
                    throw new IOException("Forge installer ba\u015far\u0131s\u0131z (\u00e7\u0131k\u0131\u015f kodu " + n + "). Java " + string + " ile uyumlu mu kontrol edin. Forge 1.12.2 Java 8, 1.16.5 Java 8/11, 1.17+ Java 17 gerektirir.");
                }
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new IOException("Forge installer kesintiye u\u011frad\u0131.", interruptedException);
            }
            if (!file.exists()) {
                file = ForgeInstaller.findForgeVersionJson(string4, string, string2, consumer);
            }
            if (file == null || !file.exists()) {
                throw new IOException("Forge installer version profilini \u00fcretemedi. versions/" + string4 + "/" + string4 + ".json bulunamad\u0131.");
            }
        } else {
            consumer.accept("Forge zaten kurulu (" + string5 + ").");
        }
        return ForgeInstaller.parseProfile(string4, file, consumer);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static String detectForgeProfileId(File file, String string, String string2, Consumer<String> consumer) {
        try (ZipFile zipFile = new ZipFile(file);){
            ZipEntry zipEntry = zipFile.getEntry("version.json");
            if (zipEntry == null) {
                String string3 = string;
                return string3;
            }
            try (InputStream inputStream = zipFile.getInputStream(zipEntry);){
                JsonObject jsonObject = JsonParser.parseString(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                if (!jsonObject.has("id")) return string;
                String string4 = jsonObject.get("id").getAsString();
                consumer.accept("Forge profil id: " + string4);
                String string5 = string4;
                return string5;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return string;
    }

    private static File findForgeVersionJson(String string, String string2, String string3, Consumer<String> consumer) {
        File file = Paths.VERSIONS_DIR;
        if (!file.isDirectory()) {
            return null;
        }
        String[] stringArray = new String[]{string, string2 + "-forge-" + string3, string2 + "-forge" + string3, "forge-" + string2 + "-" + string3};
        for (String string4 : stringArray) {
            File profileJson = new File(file, string4 + "/" + string4 + ".json");
            if (!profileJson.exists()) continue;
            consumer.accept("Forge profil bulundu: " + string4);
            return profileJson;
        }
        File[] versionDirs = file.listFiles(File::isDirectory);
        if (versionDirs != null) {
            File bestProfile = null;
            long bestTime = 0L;
            for (File dirCandidate : versionDirs) {
                String dirName = dirCandidate.getName().toLowerCase();
                File profileJson;
                if (!dirName.contains("forge") || !dirCandidate.getName().contains(string2) || !(profileJson = new File(dirCandidate, dirCandidate.getName() + ".json")).exists() || dirCandidate.lastModified() <= bestTime) continue;
                bestProfile = profileJson;
                bestTime = dirCandidate.lastModified();
            }
            if (bestProfile != null) {
                consumer.accept("Forge profil (fallback) bulundu: " + bestProfile.getParentFile().getName());
                return bestProfile;
            }
        }
        return null;
    }

    private static GameVersion parseProfile(String string, File file, Consumer<String> consumer) throws IOException {
        JsonElement jsonElement;
        JsonObject jsonObject = JsonParser.parseString(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
        GameVersion gameVersion = new GameVersion();
        gameVersion.id = string;
        gameVersion.raw = jsonObject;
        String string2 = gameVersion.mainClass = jsonObject.has("mainClass") ? jsonObject.get("mainClass").getAsString() : null;
        if (jsonObject.has("arguments")) {
            jsonElement = jsonObject.getAsJsonObject("arguments");
            gameVersion.gameArgumentsNew = ((JsonObject)jsonElement).has("game") ? ((JsonObject)jsonElement).getAsJsonArray("game") : new JsonArray();
            gameVersion.jvmArgumentsNew = ((JsonObject)jsonElement).has("jvm") ? ((JsonObject)jsonElement).getAsJsonArray("jvm") : new JsonArray();
        } else if (jsonObject.has("minecraftArguments")) {
            gameVersion.minecraftArgumentsLegacy = jsonObject.get("minecraftArguments").getAsString();
        }
        if (jsonObject.has("libraries")) {
            jsonElement = jsonObject.getAsJsonArray("libraries");
            for (int i = 0; i < ((JsonArray)jsonElement).size(); ++i) {
                JsonObject libObj = ((JsonArray)jsonElement).get(i).getAsJsonObject();
                GameVersion.LibraryEntry libraryEntry = new GameVersion.LibraryEntry();
                libraryEntry.name = libObj.get("name").getAsString();
                JsonObject downloads = libObj.has("downloads") ? libObj.getAsJsonObject("downloads") : null;
                if (downloads != null && downloads.has("artifact")) {
                    JsonObject artifact = downloads.getAsJsonObject("artifact");
                    libraryEntry.url = artifact.has("url") ? artifact.get("url").getAsString() : null;
                    libraryEntry.path = artifact.has("path") ? artifact.get("path").getAsString() : null;
                    libraryEntry.sha1 = artifact.has("sha1") ? artifact.get("sha1").getAsString() : null;
                    if (libraryEntry.url != null && libraryEntry.url.isBlank()) {
                        libraryEntry.url = null;
                    }
                } else {
                    String[] parts = libraryEntry.name.split(":");
                    if (parts.length >= 3) {
                        String groupPath = parts[0].replace('.', '/');
                        String libName = parts[1];
                        String libVersion = parts[2];
                        libraryEntry.path = groupPath + "/" + libName + "/" + libVersion + "/" + libName + "-" + libVersion + ".jar";
                        String repoUrl = libObj.has("url") ? libObj.get("url").getAsString() : "https://maven.minecraftforge.net/";
                        if (repoUrl == null || repoUrl.isBlank()) {
                            repoUrl = "https://maven.minecraftforge.net/";
                        }
                        if (!repoUrl.endsWith("/")) {
                            repoUrl = repoUrl + "/";
                        }
                        libraryEntry.url = repoUrl + libraryEntry.path;
                    }
                }
                gameVersion.libraries.add(libraryEntry);
            }
        }
        consumer.accept("\u2713 Forge profili haz\u0131r (" + gameVersion.libraries.size() + " k\u00fct\u00fcphane, mainClass=" + gameVersion.mainClass + ")");
        return gameVersion;
    }
}


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
import com.lubv.launcher.game.OsRules;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeoForgeInstaller {
    private static final String MAVEN_BASE = "https://maven.neoforged.net/releases/net/neoforged/neoforge/";
    private static final String METADATA_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml";
    private static final Pattern VERSION_TAG = Pattern.compile("<version>([^<]+)</version>");

    public static String getLatestNeoForgeVersion(String string) throws IOException {
        String string2;
        String string3;
        // V29 INTERNETSIZ CALISMA: NeoForge metadata (surum listesi) cache.
        try {
            string3 = HttpUtil.getText(METADATA_URL);
            com.lubv.launcher.core.LocalCache.putJson("neoforge:metadata", string3);
        }
        catch (IOException offline) {
            String cached = com.lubv.launcher.core.LocalCache.getJson("neoforge:metadata");
            if (cached == null) {
                throw offline;
            }
            string3 = cached;
        }
        String string4 = NeoForgeInstaller.neoForgePrefix(string);
        if (string4 == null) {
            throw new IOException("NeoForge bu Minecraft s\u00fcr\u00fcm\u00fcn\u00fc desteklemiyor: " + string);
        }
        Matcher matcher = VERSION_TAG.matcher(string3);
        String string5 = null;
        String string6 = null;
        while (matcher.find()) {
            string2 = matcher.group(1);
            if (!string2.startsWith(string4)) continue;
            String string7 = string6 = NeoForgeInstaller.compareVersions(string2, string6) > 0 ? string2 : string6;
            if (string2.contains("-beta") || string2.contains("-preview") || string2.contains("-rc")) continue;
            string5 = NeoForgeInstaller.compareVersions(string2, string5) > 0 ? string2 : string5;
        }
        String string8 = string2 = string5 != null ? string5 : string6;
        if (string2 == null) {
            throw new IOException("Bu Minecraft s\u00fcr\u00fcm\u00fc i\u00e7in NeoForge bulunamad\u0131: " + string + " (NeoForge 1.20.2 ve \u00fczeri s\u00fcr\u00fcmleri destekler)");
        }
        return string2;
    }

    private static String neoForgePrefix(String string) {
        String[] stringArray = string.split("\\.");
        if (stringArray.length < 2) {
            return null;
        }
        String string2 = stringArray[1];
        String string3 = stringArray.length >= 3 ? stringArray[2] : "0";
        return string2 + "." + string3 + ".";
    }

    private static int compareVersions(String string, String string2) {
        if (string == null) {
            return -1;
        }
        if (string2 == null) {
            return 1;
        }
        String[] stringArray = string.split("\\.");
        String[] stringArray2 = string2.split("\\.");
        int n = Math.max(stringArray.length, stringArray2.length);
        for (int i = 0; i < n; ++i) {
            int n2;
            int n3 = i < stringArray.length ? NeoForgeInstaller.safeInt(stringArray[i]) : 0;
            int n4 = n2 = i < stringArray2.length ? NeoForgeInstaller.safeInt(stringArray2[i]) : 0;
            if (n3 == n2) continue;
            return Integer.compare(n3, n2);
        }
        return 0;
    }

    private static int safeInt(String string) {
        try {
            return Integer.parseInt(string.replaceAll("[^0-9]", ""));
        }
        catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    public static GameVersion installNeoForge(String string, String string2, String string3, Consumer<String> consumer) throws IOException {
        String string4 = "neoforge-" + string3;
        File file = new File(Paths.GAME_DIR, "neoforge_installers/neoforge-" + string3 + "-installer.jar");
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            consumer.accept("NeoForge installer indiriliyor (" + string3 + ")...");
            HttpUtil.downloadFile(MAVEN_BASE + string3 + "/neoforge-" + string3 + "-installer.jar", file);
        }
        File file2 = new File(Paths.VERSIONS_DIR, string4 + "/" + string4 + ".json");
        File file3 = new File(Paths.LIBRARIES_DIR, "net/neoforged/neoforge/" + string3 + "/" + string4 + "-client.jar");
        if (!file2.exists() || !file3.exists()) {
            Object object;
            File file4 = new File(Paths.GAME_DIR, "launcher_profiles.json");
            if (!file4.exists()) {
                object = new FileWriter(file4);
                try {
                    ((Writer)object).write("{\"profiles\":{},\"selectedProfile\":\"\",\"clientToken\":\"complex-launcher\",\"authenticationDatabase\":{}}");
                }
                finally {
                    ((OutputStreamWriter)object).close();
                }
            }
            consumer.accept("NeoForge resmi installer \u00e7al\u0131\u015ft\u0131r\u0131l\u0131yor (ilk kurulum birka\u00e7 dakika s\u00fcrebilir)...");
            try {
                object = new ProcessBuilder(string2, "-jar", file.getAbsolutePath(), "--install-client", Paths.GAME_DIR.getAbsolutePath());
                ((ProcessBuilder)object).directory(Paths.GAME_DIR);
                ((ProcessBuilder)object).redirectErrorStream(true);
                Process process = ((ProcessBuilder)object).start();
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                    String string5;
                    while ((string5 = bufferedReader.readLine()) != null) {
                        String string6 = string5.trim();
                        if (!string6.contains("Processor:") && !string6.contains("Successfully") && !string6.contains("error") && !string6.contains("Error") && !string6.contains("There was") && !string6.contains("Installing")) continue;
                        consumer.accept("  " + string6);
                    }
                }
                int n = process.waitFor();
                if (n != 0) {
                    throw new IOException("NeoForge installer ba\u015far\u0131s\u0131z oldu (\u00e7\u0131k\u0131\u015f kodu " + n + ").");
                }
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new IOException("NeoForge installer kesintiye u\u011frad\u0131.", interruptedException);
            }
            if (!file2.exists()) {
                throw new IOException("NeoForge installer version profilini \u00fcretemedi.");
            }
        } else {
            consumer.accept("NeoForge zaten kurulu (" + string3 + ").");
        }
        return NeoForgeInstaller.parseProfile(string4, file2);
    }

    private static GameVersion parseProfile(String string, File file) throws IOException {
        JsonElement jsonElement;
        JsonObject jsonObject = JsonParser.parseString(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
        GameVersion gameVersion = new GameVersion();
        gameVersion.id = string;
        gameVersion.raw = jsonObject;
        gameVersion.mainClass = jsonObject.get("mainClass").getAsString();
        if (jsonObject.has("arguments")) {
            jsonElement = jsonObject.getAsJsonObject("arguments");
            gameVersion.gameArgumentsNew = ((JsonObject)jsonElement).has("game") ? ((JsonObject)jsonElement).getAsJsonArray("game") : new JsonArray();
            gameVersion.jvmArgumentsNew = ((JsonObject)jsonElement).has("jvm") ? ((JsonObject)jsonElement).getAsJsonArray("jvm") : new JsonArray();
        }
        jsonElement = jsonObject.has("libraries") ? jsonObject.getAsJsonArray("libraries") : new JsonArray();
            for (int i = 0; i < ((JsonArray)jsonElement).size(); ++i) {
                JsonObject libObj = ((JsonArray)jsonElement).get(i).getAsJsonObject();
                if (libObj.has("rules") && !OsRules.isAllowed(libObj.getAsJsonArray("rules"))) continue;
                GameVersion.LibraryEntry libraryEntry = new GameVersion.LibraryEntry();
                libraryEntry.name = libObj.get("name").getAsString();
                JsonObject downloads = libObj.has("downloads") ? libObj.getAsJsonObject("downloads") : null;
                if (downloads != null && downloads.has("artifact")) {
                    JsonObject artifact = downloads.getAsJsonObject("artifact");
                    libraryEntry.url = artifact.has("url") && !artifact.get("url").isJsonNull() ? artifact.get("url").getAsString() : null;
                    libraryEntry.path = artifact.has("path") && !artifact.get("path").isJsonNull() ? artifact.get("path").getAsString() : null;
                    libraryEntry.sha1 = artifact.has("sha1") && !artifact.get("sha1").isJsonNull() ? artifact.get("sha1").getAsString() : null;
                    if (libraryEntry.path == null && libraryEntry.name.split(":").length >= 3) {
                        String[] parts = libraryEntry.name.split(":");
                        String groupPath = parts[0].replace('.', '/');
                        String libName = parts[1];
                        String libVersion = parts[2];
                        libraryEntry.path = groupPath + "/" + libName + "/" + libVersion + "/" + libName + "-" + libVersion + ".jar";
                    }
                } else {
                    String[] parts = libraryEntry.name.split(":");
                    if (parts.length < 3) continue;
                    String groupPath = parts[0].replace('.', '/');
                    String libName = parts[1];
                    String libVersion = parts[2];
                    libraryEntry.path = groupPath + "/" + libName + "/" + libVersion + "/" + libName + "-" + libVersion + ".jar";
                    String repoUrl = libObj.has("url") ? libObj.get("url").getAsString() : "https://repo1.maven.org/maven2/";
                    if (repoUrl == null || repoUrl.isBlank()) {
                        repoUrl = "https://repo1.maven.org/maven2/";
                    }
                    if (!repoUrl.endsWith("/")) {
                        repoUrl = repoUrl + "/";
                    }
                    libraryEntry.url = repoUrl + libraryEntry.path;
                }
                if (libraryEntry.path == null) continue;
                gameVersion.libraries.add(libraryEntry);
            }
        return gameVersion;
    }
}


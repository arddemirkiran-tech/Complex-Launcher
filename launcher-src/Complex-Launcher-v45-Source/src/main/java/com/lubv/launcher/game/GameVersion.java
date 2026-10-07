/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.game.OsRules;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameVersion {
    public String id;
    public String mainClass;
    public String assetIndexId;
    public String assetIndexUrl;
    public String clientJarUrl;
    public String clientJarSha1;
    public String minecraftArgumentsLegacy;
    public JsonArray gameArgumentsNew;
    public JsonArray jvmArgumentsNew;
    public int javaMajorVersion = 8;
    public String nativesLibraryPath = "";
    public JsonObject raw;
    public List<LibraryEntry> libraries = new ArrayList<LibraryEntry>();

    /**
     * V34.8 BASLANGIC HIZI: surum JSON'unu arkada cekip diske yazar
     * (LocalCache key'i parse() ile AYNI). Kullanici oyuna basladiginda
     * parse() internet yerine sicak disk cache'inden okur - surum JSON'u
     * icin ag beklemesi sifirlanir. Basarisizlik sessizce yutulur.
     */
    public static void prefetchVersionJsonAsync(String versionUrl, String versionId) {
        if (versionUrl == null || versionUrl.isBlank() || versionId == null || versionId.isBlank()) {
            return;
        }
        Thread t = new Thread(() -> {
            try {
                String cacheKey = "mc:version:" + versionId;
                if (com.lubv.launcher.core.LocalCache.getJson(cacheKey) != null) {
                    return; // zaten cache'te
                }
                String body = HttpUtil.getText(versionUrl);
                if (body != null && !body.isEmpty()) {
                    com.lubv.launcher.core.LocalCache.putJson(cacheKey, body);
                }
            }
            catch (Exception ignored) {
                // one-yukleme basarisizligi onemli degil
            }
        }, "version-json-prefetch");
        t.setDaemon(true);
        t.start();
    }

    public static GameVersion parse(String string, String string2) throws IOException {
        JsonElement jsonElement;
        JsonObject jsonObject;
        GameVersion gameVersion = new GameVersion();
        gameVersion.id = string;
        // V29 INTERNETSIZ CALISMA + PERFORMANS:
        //  - Her basarili cekim disk'e ASYNC yazilir (bloklamaz). Ayni surum
        //    icin ikinci acilista bile ag olmadan ayni JSON yerelden gelir.
        //  - Internet yoksa son cekilen JSON kullanilir; hic cache yoksa
        //    (o surum hicbaslmamissa) gercek hata firlatilir.
        String cacheKey = "mc:version:" + string;
        try {
            gameVersion.raw = HttpUtil.getJson(string2);
            com.lubv.launcher.core.LocalCache.putJson(cacheKey, gameVersion.raw.toString());
        }
        catch (IOException offline) {
            String cached = com.lubv.launcher.core.LocalCache.getJson(cacheKey);
            if (cached == null) {
                throw offline;
            }
            gameVersion.raw = com.google.gson.JsonParser.parseString(cached).getAsJsonObject();
        }
        gameVersion.mainClass = gameVersion.raw.get("mainClass").getAsString();
        if (gameVersion.raw.has("javaVersion") && (jsonObject = gameVersion.raw.getAsJsonObject("javaVersion")).has("majorVersion")) {
            gameVersion.javaMajorVersion = jsonObject.get("majorVersion").getAsInt();
        }
        jsonObject = gameVersion.raw.getAsJsonObject("assetIndex");
        gameVersion.assetIndexId = jsonObject.get("id").getAsString();
        gameVersion.assetIndexUrl = jsonObject.get("url").getAsString();
        JsonObject jsonObject2 = gameVersion.raw.getAsJsonObject("downloads");
        JsonObject jsonObject3 = jsonObject2.getAsJsonObject("client");
        gameVersion.clientJarUrl = jsonObject3.get("url").getAsString();
        String string3 = gameVersion.clientJarSha1 = jsonObject3.has("sha1") ? jsonObject3.get("sha1").getAsString() : null;
        if (gameVersion.raw.has("minecraftArguments")) {
            gameVersion.minecraftArgumentsLegacy = gameVersion.raw.get("minecraftArguments").getAsString();
        }
        if (gameVersion.raw.has("arguments")) {
            jsonElement = gameVersion.raw.getAsJsonObject("arguments");
            gameVersion.gameArgumentsNew = ((JsonObject)jsonElement).has("game") ? ((JsonObject)jsonElement).getAsJsonArray("game") : new JsonArray();
            gameVersion.jvmArgumentsNew = ((JsonObject)jsonElement).has("jvm") ? ((JsonObject)jsonElement).getAsJsonArray("jvm") : new JsonArray();
        }
        gameVersion.nativesLibraryPath = GameVersion.detectNativesSubDir(gameVersion.jvmArgumentsNew);
        jsonElement = gameVersion.raw.getAsJsonArray("libraries");
        for (int i = 0; i < ((JsonArray)jsonElement).size(); ++i) {
            JsonObject jsonObject5 = ((JsonArray)jsonElement).get(i).getAsJsonObject();
            if (jsonObject5.has("rules") && !OsRules.isAllowed(jsonObject5.getAsJsonArray("rules"))) continue;
            LibraryEntry libraryEntry = new LibraryEntry();
            libraryEntry.name = jsonObject5.get("name").getAsString();
            JsonObject jsonObject4 = jsonObject5.has("downloads") ? jsonObject5.getAsJsonObject("downloads") : null;
            if (jsonObject4 != null && jsonObject4.has("artifact")) {
                JsonObject artifact = jsonObject4.getAsJsonObject("artifact");
                libraryEntry.url = artifact.get("url").getAsString();
                libraryEntry.path = artifact.get("path").getAsString();
                libraryEntry.sha1 = artifact.has("sha1") ? artifact.get("sha1").getAsString() : null;
            } else if (jsonObject4 == null) {
                String[] parts = libraryEntry.name.split(":");
                if (parts.length < 3) continue;
                String groupPath = parts[0].replace('.', '/');
                String libName = parts[1];
                String libVersion = parts[2];
                String classifier = parts.length > 3 ? parts[3] : null;
                String jarName = classifier != null ? libName + "-" + libVersion + "-" + classifier + ".jar" : libName + "-" + libVersion + ".jar";
                libraryEntry.path = groupPath + "/" + libName + "/" + libVersion + "/" + jarName;
                String repoUrl = jsonObject5.has("url") ? jsonObject5.get("url").getAsString() : "https://repo1.maven.org/maven2/";
                if (repoUrl == null || repoUrl.isBlank()) {
                    repoUrl = "https://repo1.maven.org/maven2/";
                }
                if (!repoUrl.endsWith("/")) {
                    repoUrl = repoUrl + "/";
                }
                libraryEntry.url = repoUrl + libraryEntry.path;
            }
            if (jsonObject4 != null && jsonObject4.has("classifiers") && jsonObject5.has("natives")) {
                JsonObject natives = jsonObject5.getAsJsonObject("natives");
                String osKey = OsRules.CURRENT_OS == OsRules.Os.WINDOWS ? "windows" : (OsRules.CURRENT_OS == OsRules.Os.MAC ? "osx" : "linux");
                if (natives.has(osKey)) {
                    String nativeKey = natives.get(osKey).getAsString().replace("${arch}", "64");
                    JsonObject classifiers = jsonObject4.getAsJsonObject("classifiers");
                    if (classifiers.has(nativeKey)) {
                        JsonObject nativeArtifact = classifiers.getAsJsonObject(nativeKey);
                        libraryEntry.isNative = true;
                        libraryEntry.nativeUrl = nativeArtifact.get("url").getAsString();
                        libraryEntry.nativePath = nativeArtifact.get("path").getAsString();
                        libraryEntry.nativeSha1 = nativeArtifact.has("sha1") ? nativeArtifact.get("sha1").getAsString() : null;
                    }
                }
            }
            String[] nameParts = libraryEntry.name.split(":");
            String nameClassifier = nameParts.length > 3 ? nameParts[3] : null;
            if (nameClassifier != null && nameClassifier.startsWith("natives-")) {
                if (!nameClassifier.equals(OsRules.nativeClassifier())) continue;
                libraryEntry.isNative = true;
                libraryEntry.nativeUrl = libraryEntry.url;
                libraryEntry.nativePath = libraryEntry.path;
                libraryEntry.nativeSha1 = libraryEntry.sha1;
            } else if (libraryEntry.name.contains("natives-")) continue;
            gameVersion.libraries.add(libraryEntry);
        }
        return gameVersion;
    }

    public File clientJarFile() {
        return new File(Paths.VERSIONS_DIR, this.id + "/" + this.id + ".jar");
    }

    private static String detectNativesSubDir(JsonArray jsonArray) {
        if (jsonArray == null) {
            return "";
        }
        for (int i = 0; i < jsonArray.size(); ++i) {
            char c;
            String string = jsonArray.get(i).toString();
            int n = string.indexOf("${natives_directory}/");
            if (n < 0) continue;
            int n2 = n + "${natives_directory}/".length();
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = n2; j < string.length() && Character.isLetter(c = string.charAt(j)); ++j) {
                stringBuilder.append(c);
            }
            return stringBuilder.toString();
        }
        return "";
    }

    public static class LibraryEntry {
        public String name;
        public String url;
        public String path;
        public String sha1;
        public boolean isNative;
        public String nativeUrl;
        public String nativePath;
        public String nativeSha1;
    }
}


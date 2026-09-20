/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.game;

import com.google.gson.JsonArray;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.game.GameVersion;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptiFineInstaller {
    private static final Map<String, String> VERSION_CACHE = new ConcurrentHashMap<String, String>();
    private static final String OPTIFINE_DOWNLOADS_URL = "https://optifine.net/downloads";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final Pattern DOWNLOAD_LINK_PATTERN = Pattern.compile("downloadx\\?f=([^\"'&]+)&x=([a-fA-F0-9]+)");

    public static String getLatestOptiFineVersion(String string) throws IOException {
        if (VERSION_CACHE.containsKey(string)) {
            return VERSION_CACHE.get(string);
        }
        String string2 = OptiFineInstaller.fetchHtml(OPTIFINE_DOWNLOADS_URL);
        Pattern pattern = Pattern.compile("OptiFine_(" + Pattern.quote(string) + "_HD_U_[A-Z0-9_]+)\\.jar");
        Matcher matcher = pattern.matcher(string2);
        if (matcher.find()) {
            String string3 = matcher.group(1);
            String string4 = string3.replace(string + "_", "");
            VERSION_CACHE.put(string, string4);
            return string4;
        }
        throw new IOException("Bu Minecraft surumu icin OptiFine bulunamadi: " + string);
    }

    public static GameVersion installOptiFine(String string, String string2, String string3, Consumer<String> consumer) throws Exception {
        Object object;
        consumer.accept("OptiFine hazirlaniyor (" + string + " - " + string2 + ")...");
        File file = new File(Paths.LIBRARIES_DIR, "optifine/Optifine/" + string + "_" + string2);
        file.mkdirs();
        File file2 = new File(file, "OptiFine-" + string + "_" + string2 + ".jar");
        if (!OptiFineInstaller.isValidJar(file2)) {
            object = "OptiFine_" + string + "_" + string2 + ".jar";
            consumer.accept("Indiriliyor: " + file2.getName());
            OptiFineInstaller.downloadOptiFineJar((String)object, file2, consumer);
        } else {
            consumer.accept("OptiFine JAR zaten mevcut (" + file2.length() / 1024L + " KB)");
        }
        object = new GameVersion();
        ((GameVersion)object).id = "OptiFine_" + string + "_" + string2;
        ((GameVersion)object).mainClass = "net.minecraft.launchwrapper.Launch";
        ((GameVersion)object).libraries = new ArrayList<GameVersion.LibraryEntry>();
        ((GameVersion)object).gameArgumentsNew = new JsonArray();
        ((GameVersion)object).gameArgumentsNew.add("--tweakClass");
        ((GameVersion)object).gameArgumentsNew.add("optifine.OptiFineTweaker");
        GameVersion.LibraryEntry libraryEntry = new GameVersion.LibraryEntry();
        libraryEntry.name = "net.minecraft:launchwrapper:1.12";
        libraryEntry.url = "https://libraries.minecraft.net/";
        libraryEntry.path = "net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar";
        ((GameVersion)object).libraries.add(libraryEntry);
        GameVersion.LibraryEntry libraryEntry2 = new GameVersion.LibraryEntry();
        libraryEntry2.name = "optifine:Optifine:" + string + "_" + string2;
        libraryEntry2.path = "optifine/Optifine/" + string + "_" + string2 + "/OptiFine-" + string + "_" + string2 + ".jar";
        libraryEntry2.url = null;
        ((GameVersion)object).libraries.add(libraryEntry2);
        consumer.accept("OptiFine hazir (" + ((GameVersion)object).libraries.size() + " kutuphane)");
        return (GameVersion)object;
    }

    public static File installOptiFineAsMod(String string, String string3, File file2, Consumer<String> consumer) throws IOException {
        file2.mkdirs();
        String string4 = "OptiFine_" + string + "_" + string3 + ".jar";
        File file3 = new File(file2, string4);
        if (!OptiFineInstaller.isValidJar(file3)) {
            consumer.accept("OptiFine (mod) indiriliyor: " + string4);
            OptiFineInstaller.downloadOptiFineJar(string4, file3, consumer);
        } else {
            consumer.accept("OptiFine mod JAR zaten mevcut (" + file3.length() / 1024L + " KB)");
        }
        File[] fileArray = file2.listFiles((file, string2) -> string2.toLowerCase().startsWith("optifine_") && string2.toLowerCase().endsWith(".jar") && !string2.equals(string4));
        if (fileArray != null) {
            for (File file4 : fileArray) {
                if (!file4.delete()) continue;
                consumer.accept("Eski OptiFine mod jar'i temizlendi: " + file4.getName());
            }
        }
        return file3;
    }

    private static void downloadOptiFineJar(String string, File file, Consumer<String> consumer) throws IOException {
        String string2 = "https://optifine.net/adloadx?f=" + string;
        String string3 = OptiFineInstaller.fetchHtml(string2);
        Matcher matcher = DOWNLOAD_LINK_PATTERN.matcher(string3);
        if (!matcher.find()) {
            throw new IOException("OptiFine indirme linki (x= token) bulunamadi \u2014 optifine.net sayfa yapisi degismis olabilir.");
        }
        String string4 = matcher.group(1);
        String string5 = matcher.group(2);
        String string6 = "https://optifine.net/downloadx?f=" + string4 + "&x=" + string5;
        OptiFineInstaller.downloadFile(string6, file, consumer);
        if (!OptiFineInstaller.isValidJar(file)) {
            long l = file.exists() ? file.length() : 0L;
            file.delete();
            throw new IOException("OptiFine indirmesi gecersiz (boyut=" + l + " byte) \u2014 optifine.net gecici olarak erisilemez ya da token suresi dolmus olabilir. Tekrar deneyin.");
        }
        consumer.accept("Indirildi: " + file.getName() + " (" + file.length() / 1024L + " KB)");
    }

    private static boolean isValidJar(File file) {
        boolean bl;
        if (!file.isFile() || file.length() < 10000L) {
            return false;
        }
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] byArray = new byte[2];
            int n = ((InputStream)fileInputStream).read(byArray);
            bl = n == 2 && byArray[0] == 80 && byArray[1] == 75;
        }
        catch (Throwable throwable) {
            return false;
        }
        return bl;
    }

    private static String fetchHtml(String string) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(string).openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setInstanceFollowRedirects(true);
        httpURLConnection.setConnectTimeout(10000);
        httpURLConnection.setReadTimeout(10000);
        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
        int n = httpURLConnection.getResponseCode();
        if (n != 200) {
            httpURLConnection.disconnect();
            throw new IOException("HTTP " + n + " \u2014 " + string);
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8));){
            String string2;
            while ((string2 = bufferedReader.readLine()) != null) {
                stringBuilder.append(string2).append('\n');
            }
        }
        httpURLConnection.disconnect();
        return stringBuilder.toString();
    }

    private static void downloadFile(String string, File file, Consumer<String> consumer) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(string).openConnection();
        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
        httpURLConnection.setConnectTimeout(15000);
        httpURLConnection.setReadTimeout(30000);
        httpURLConnection.setInstanceFollowRedirects(true);
        int n = httpURLConnection.getResponseCode();
        if (n != 200) {
            httpURLConnection.disconnect();
            throw new IOException("OptiFine sunucusu yanit vermedi (HTTP " + n + ")");
        }
        file.getParentFile().mkdirs();
        try (InputStream inputStream = httpURLConnection.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(file);){
            int n2;
            byte[] byArray = new byte[8192];
            while ((n2 = inputStream.read(byArray)) != -1) {
                ((OutputStream)fileOutputStream).write(byArray, 0, n2);
            }
        }
        httpURLConnection.disconnect();
    }
}


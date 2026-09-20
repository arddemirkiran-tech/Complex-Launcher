/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.game.GameVersion;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class AssetDownloader {
    private static final String RESOURCES_BASE = "https://resources.download.minecraft.net/";

    /**
     * V34.8 BASLANGIC HIZI: asset index JSON'unu (500KB civari) launch
     * boyunca ARKA PLANDA ceker ve diske yazar. Kaynaklar indirme asamasina
     * gelindiginde index zaten diskte olur - download() ag beklemeksizin
     * dogrudan parcayla devam eder. Indirme basarisiz olursa sessizce
     * yutulur: download() kendi yolunda index'i yine kendisi ceker.
     */
    public static void prefetchIndexAsync(GameVersion gameVersion) {
        if (gameVersion == null || gameVersion.assetIndexUrl == null || gameVersion.assetIndexId == null) {
            return;
        }
        Thread t = new Thread(() -> {
            try {
                File idx = new File(com.lubv.launcher.core.Paths.ASSET_INDEXES_DIR, gameVersion.assetIndexId + ".json");
                if (idx.isFile() && idx.length() > 0) {
                    return; // zaten var
                }
                String body = HttpUtil.getText(gameVersion.assetIndexUrl);
                com.lubv.launcher.core.Paths.ASSET_INDEXES_DIR.mkdirs();
                File tmp = new File(idx.getParentFile(), idx.getName() + ".tmp");
                java.nio.file.Files.writeString(tmp.toPath(), body, java.nio.charset.StandardCharsets.UTF_8);
                try {
                    java.nio.file.Files.move(tmp.toPath(), idx.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
                }
                catch (IOException atomicFailed) {
                    java.nio.file.Files.move(tmp.toPath(), idx.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                // Index yeni indi ise kaynagi da one tuslemeye gec.
                AssetDownloader.startWarmWorkers(gameVersion);
            }
            catch (Exception ignored) {
                // on-yukleme basarisizligi hicbir seyi bozmaz
            }
        }, "asset-index-prefetch");
        t.setDaemon(true);
        t.start();
    }

    /**
     * V35.1 ACILIS HIZI: kutuphaneler inerken kaynagi da PARALEL indirir.
     * Index diskte hazirsa hemen baslar; degilse prefetchIndexAsync index'i
     * indirir indirmez baslar. Kaynagi download() cagrildiginda sha1
     * kontroluyle ATLAR - yani dogruluk korunur, sadece ag beklemesi
     * kutuphanelerle cakisir.
     */
    public static void warmAssetsAsync(GameVersion gameVersion) {
        if (gameVersion == null || gameVersion.assetIndexId == null) {
            return;
        }
        File idx = new File(com.lubv.launcher.core.Paths.ASSET_INDEXES_DIR, gameVersion.assetIndexId + ".json");
        if (idx.isFile() && idx.length() > 0) {
            AssetDownloader.startWarmWorkers(gameVersion);
        }
        // index yoksa prefetchIndexAsync tamamlaninca kendisi baslatir.
    }

    private static void startWarmWorkers(GameVersion gameVersion) {
        Thread t = new Thread(() -> {
            try {
                File idx = new File(com.lubv.launcher.core.Paths.ASSET_INDEXES_DIR, gameVersion.assetIndexId + ".json");
                if (!idx.isFile()) {
                    return;
                }
                String body = java.nio.file.Files.readString(idx.toPath(), java.nio.charset.StandardCharsets.UTF_8);
                com.google.gson.JsonObject objects = com.google.gson.JsonParser.parseString(body)
                    .getAsJsonObject().getAsJsonObject("objects");
                java.util.concurrent.Semaphore gate = new java.util.concurrent.Semaphore(10);
                for (java.util.Map.Entry<String, com.google.gson.JsonElement> e : objects.entrySet()) {
                    com.google.gson.JsonObject o = e.getValue().getAsJsonObject();
                    String hash = o.get("hash").getAsString();
                    String sub = hash.substring(0, 2);
                    File dest = new File(com.lubv.launcher.core.Paths.ASSET_OBJECTS_DIR, sub + "/" + hash);
                    Runnable job = () -> {
                        try {
                            gate.acquire();
                            try {
                                if (!HttpUtil.sha1Matches(dest, hash)) {
                                    HttpUtil.downloadFile(RESOURCES_BASE + sub + "/" + hash, dest);
                                }
                            }
                            finally {
                                gate.release();
                            }
                        }
                        catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        catch (IOException ignored) {
                            // download() kendi yolu tekrar dener
                        }
                    };
                    Thread w = new Thread(job, "asset-warm");
                    w.setDaemon(true);
                    w.start();
                }
            }
            catch (Exception ignored) {
                // isitma basarisizligi kaynagi bozmaz; download() tamamlar
            }
        }, "asset-warmup");
        t.setDaemon(true);
        t.start();
    }

    public static void download(GameVersion gameVersion, BiConsumer<Integer, Integer> biConsumer) throws Exception {
        Object object;
        String string;
        File file = new File(Paths.ASSET_INDEXES_DIR, gameVersion.assetIndexId + ".json");
        if (file.exists()) {
            string = Files.readString(file.toPath());
        } else {
            string = HttpUtil.getText(gameVersion.assetIndexUrl);
            Paths.ASSET_INDEXES_DIR.mkdirs();
            object = new FileWriter(file);
            try {
                ((Writer)object).write(string);
            }
            finally {
                ((OutputStreamWriter)object).close();
            }
        }
        object = JsonParser.parseString(string).getAsJsonObject();
        JsonObject jsonObject = ((JsonObject)object).getAsJsonObject("objects");
        int n = jsonObject.size();
        AtomicInteger atomicInteger = new AtomicInteger(0);
        // V29 PERFORMANS: 8 -> 16 is parcacigi. Asset'ler kucuk dosyalardir
        // (çoğu <50KB); performans darbogazi bant genisligi degil istek
        // gecikmesidir, bu yuzden daha yuksek eszamanlilik dogrudan daha
        // hizli ilk acilis demektir.
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        ArrayList<Future<?>> arrayList = new ArrayList<Future<?>>();
        for (Map.Entry<String, JsonElement> object2 : jsonObject.entrySet()) {
            JsonObject jsonObject2 = object2.getValue().getAsJsonObject();
            String string2 = jsonObject2.get("hash").getAsString();
            String string3 = string2.substring(0, 2);
            File file2 = new File(Paths.ASSET_OBJECTS_DIR, string3 + "/" + string2);
            arrayList.add(executorService.submit(() -> {
                try {
                    if (!HttpUtil.sha1Matches(file2, string2)) {
                        HttpUtil.downloadFile(RESOURCES_BASE + string3 + "/" + string2, file2);
                    }
                }
                catch (IOException iOException) {
                }
                finally {
                    biConsumer.accept(atomicInteger.incrementAndGet(), n);
                }
            }));
        }
        for (Future<?> future : arrayList) {
            try {
                future.get();
            }
            catch (Exception exception) {}
        }
        executorService.shutdown();
    }
}


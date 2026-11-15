/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.core.LocalCache;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VersionManifest {
    private static final String MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String CACHE_KEY = "mc:version_manifest_v2";
    private static final String CACHE_KEY_LATEST = "mc:latest_release";
    public JsonObject raw;
    public List<Entry> versions = new ArrayList<Entry>();
    public String latestRelease;
    public String latestSnapshot;
    public boolean fromCache = false;

    public static VersionManifest fetch() throws IOException {
        VersionManifest versionManifest = new VersionManifest();
        try {
            // Taze fetch + ASYNC disk cache: internet varken son manifest
            // kaydedilir, internet yoksa ayni key'den geri okunur.
            versionManifest.raw = HttpUtil.getJson(MANIFEST_URL);
            LocalCache.putJson(CACHE_KEY, versionManifest.raw.toString());
        }
        catch (IOException offline) {
            // INTERNETSIZ CALISMA: son cekilen manifest yerelden gelir.
            String cached = LocalCache.getJson(CACHE_KEY);
            if (cached == null) {
                throw offline; // hic cache yoksa gercekten hata -> cagiran bilir
            }
            versionManifest.raw = JsonParser.parseString(cached).getAsJsonObject();
            versionManifest.fromCache = true;
        }
        JsonObject jsonObject = versionManifest.raw.getAsJsonObject("latest");
        versionManifest.latestRelease = jsonObject.get("release").getAsString();
        versionManifest.latestSnapshot = jsonObject.get("snapshot").getAsString();
        JsonArray jsonArray = versionManifest.raw.getAsJsonArray("versions");
        for (int i = 0; i < jsonArray.size(); ++i) {
            JsonObject jsonObject2 = jsonArray.get(i).getAsJsonObject();
            Entry entry = new Entry();
            entry.id = jsonObject2.get("id").getAsString();
            entry.type = jsonObject2.get("type").getAsString();
            entry.url = jsonObject2.get("url").getAsString();
            versionManifest.versions.add(entry);
        }
        // Son release'i ayrica sakla: hic cache olmasa bile (orn. ilk acilis
        // offline) surum listesi bosken en azindan son bilinen release ile
        // oyuna baslanabilir; internet gelince normal liste geri gelir.
        if (!versionManifest.fromCache) {
            LocalCache.putJson(CACHE_KEY_LATEST, versionManifest.latestRelease);
        }
        return versionManifest;
    }

    /**
     * Hafif surum listesi: internet yoksa ve manifest cache'i de yoksa
     * en azindan son bilinen release'i tek girdilik liste olarak doner.
     * (Ilk acilis offline olan kullanici "hic surum yok" duvarina carpmasin.)
     */
    public static String lastKnownRelease() {
        return LocalCache.getJson(CACHE_KEY_LATEST);
    }

    public Entry find(String string) {
        for (Entry entry : this.versions) {
            if (!entry.id.equals(string)) continue;
            return entry;
        }
        return null;
    }

    public List<Entry> releasesOnly() {
        ArrayList<Entry> arrayList = new ArrayList<Entry>();
        for (Entry entry : this.versions) {
            if (!entry.type.equals("release")) continue;
            arrayList.add(entry);
        }
        return arrayList;
    }

    public static class Entry {
        public String id;
        public String type;
        public String url;
    }
}

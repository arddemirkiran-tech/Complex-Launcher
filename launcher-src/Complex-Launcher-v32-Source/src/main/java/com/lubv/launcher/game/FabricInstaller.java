/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.HttpUtil;
import com.lubv.launcher.game.GameVersion;
import java.io.IOException;
import java.util.function.Consumer;

public class FabricInstaller {
    private static final String META = "https://meta.fabricmc.net/v2";

    public static String getLatestLoaderVersion(String string) throws IOException {
        String string2;
        // V29 INTERNETSIZ CALISMA: loader surum listesi de surum bilgisi.
        String cacheKey = "fabric:loaders:" + string;
        try {
            string2 = HttpUtil.getText("https://meta.fabricmc.net/v2/versions/loader/" + string);
            com.lubv.launcher.core.LocalCache.putJson(cacheKey, string2);
        }
        catch (IOException offline) {
            String cached = com.lubv.launcher.core.LocalCache.getJson(cacheKey);
            if (cached == null) {
                throw offline;
            }
            string2 = cached;
        }
        JsonArray jsonArray = JsonParser.parseString(string2).getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); ++i) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            JsonObject jsonObject2 = jsonObject.getAsJsonObject("loader");
            if (!jsonObject2.get("stable").getAsBoolean()) continue;
            return jsonObject2.get("version").getAsString();
        }
        if (jsonArray.size() > 0) {
            return jsonArray.get(0).getAsJsonObject().getAsJsonObject("loader").get("version").getAsString();
        }
        throw new IOException("Bu Minecraft s\u00fcr\u00fcm\u00fc i\u00e7in Fabric loader bulunamad\u0131: " + string);
    }

    public static GameVersion installFabric(String string, String string2, Consumer<String> consumer) throws IOException {
        consumer.accept("Fabric profili indiriliyor (MC " + string + " + Loader " + string2 + ")...");
        String string3 = "https://meta.fabricmc.net/v2/versions/loader/" + string + "/" + string2 + "/profile/json";
        GameVersion gameVersion = new GameVersion();
        // V29 INTERNETSIZ CALISMA: daha once bu (MC, loader) ikilisiyle
        // profil cekildiyse JSON disk'te; internet yoksa yerelden gelir.
        String cacheKey = "fabric:profile:" + string + ":" + string2;
        try {
            gameVersion.raw = HttpUtil.getJson(string3);
            com.lubv.launcher.core.LocalCache.putJson(cacheKey, gameVersion.raw.toString());
        }
        catch (IOException offline) {
            String cached = com.lubv.launcher.core.LocalCache.getJson(cacheKey);
            if (cached == null) {
                throw offline;
            }
            gameVersion.raw = com.google.gson.JsonParser.parseString(cached).getAsJsonObject();
        }
        gameVersion.id = string + "-fabric-" + string2;
        gameVersion.mainClass = gameVersion.raw.get("mainClass").getAsString();
        JsonArray jsonArray = gameVersion.raw.getAsJsonArray("libraries");
        for (int i = 0; i < jsonArray.size(); ++i) {
            Object object;
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            GameVersion.LibraryEntry libraryEntry = new GameVersion.LibraryEntry();
            libraryEntry.name = jsonObject.get("name").getAsString();
            String[] stringArray = libraryEntry.name.split(":");
            if (stringArray.length < 3) continue;
            String string4 = stringArray[0].replace('.', '/');
            String string5 = stringArray[1];
            String string6 = stringArray[2];
            String string7 = stringArray.length > 3 ? stringArray[3] : null;
            String string8 = string7 != null ? string5 + "-" + string6 + "-" + string7 + ".jar" : string5 + "-" + string6 + ".jar";
            libraryEntry.path = string4 + "/" + string5 + "/" + string6 + "/" + string8;
            Object object2 = object = jsonObject.has("url") ? jsonObject.get("url").getAsString() : "";
            if (object == null || ((String)object).isBlank()) {
                object = "https://maven.fabricmc.net/";
            }
            if (!((String)object).endsWith("/")) {
                object = (String)object + "/";
            }
            libraryEntry.url = (String)object + libraryEntry.path;
            if (string7 != null && string7.startsWith("natives-")) {
                libraryEntry.isNative = true;
                libraryEntry.nativeUrl = libraryEntry.url;
                libraryEntry.nativePath = libraryEntry.path;
            }
            gameVersion.libraries.add(libraryEntry);
        }
        consumer.accept("\u2713 Fabric profili haz\u0131r (" + gameVersion.libraries.size() + " ek k\u00fct\u00fcphane)");
        return gameVersion;
    }
}


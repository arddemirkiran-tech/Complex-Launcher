/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.auth;

import com.google.gson.Gson;
import com.lubv.launcher.core.Paths;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MinecraftSession {
    public String username;
    public String uuid;
    public String accessToken;
    public String msRefreshToken;
    public String userType = "msa";
    public String skinUrl = "";
    public String skinFile = "";
    private static final Gson GSON = new Gson();

    public static MinecraftSession offline(String string) {
        MinecraftSession minecraftSession = new MinecraftSession();
        minecraftSession.username = string;
        minecraftSession.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + string).getBytes(StandardCharsets.UTF_8)).toString();
        minecraftSession.accessToken = UUID.randomUUID().toString();
        minecraftSession.userType = "mojang";
        return minecraftSession;
    }

    public void saveToDisk() {
        try {
            Paths.GAME_DIR.mkdirs();
            try (FileWriter fileWriter = new FileWriter(Paths.AUTH_CACHE_FILE);){
                GSON.toJson((Object)this, (Appendable)fileWriter);
            }
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    public static MinecraftSession loadFromDisk() {
        if (!Paths.AUTH_CACHE_FILE.exists()) {
            return null;
        }
        try (FileReader fileReader = new FileReader(Paths.AUTH_CACHE_FILE)) {
            return GSON.fromJson((Reader)fileReader, MinecraftSession.class);
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static void clearDisk() {
        Paths.AUTH_CACHE_FILE.delete();
    }
}


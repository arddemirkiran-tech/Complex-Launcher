/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lubv.launcher.auth.MinecraftSession;
import com.lubv.launcher.core.Paths;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public final class AccountManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private AccountManager() {
    }

    private static File file() {
        return new File(Paths.GAME_DIR, "accounts.json");
    }

    public static synchronized Store load() {
        File file = AccountManager.file();
        Store store = new Store();
        if (file.exists()) {
            try (FileReader fileReader = new FileReader(file);){
                Store store2 = GSON.fromJson((Reader)fileReader, Store.class);
                if (store2 != null) {
                    store = store2;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return store;
    }

    public static synchronized void save(Store store) {
        Paths.GAME_DIR.mkdirs();
        try (FileWriter fileWriter = new FileWriter(AccountManager.file());){
            GSON.toJson((Object)store, (Appendable)fileWriter);
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    public static synchronized List<MinecraftSession> list() {
        return AccountManager.load().accounts;
    }

    public static synchronized String activeUsername() {
        Store store = AccountManager.load();
        if ((store.active == null || store.active.isEmpty()) && !store.accounts.isEmpty()) {
            store.active = store.accounts.get((int)0).username;
        }
        return store.active;
    }

    public static synchronized MinecraftSession getActive() {
        Store store = AccountManager.load();
        for (MinecraftSession minecraftSession : store.accounts) {
            if (minecraftSession.username == null || !minecraftSession.username.equals(store.active)) continue;
            return minecraftSession;
        }
        return store.accounts.isEmpty() ? null : store.accounts.get(0);
    }

    public static synchronized void add(MinecraftSession minecraftSession) {
        Store store = AccountManager.load();
        store.accounts.removeIf(minecraftSession2 -> minecraftSession2.uuid != null && minecraftSession2.uuid.equals(minecraftSession.uuid) || minecraftSession2.username != null && minecraftSession2.username.equalsIgnoreCase(minecraftSession.username));
        store.accounts.add(minecraftSession);
        store.active = minecraftSession.username;
        AccountManager.save(store);
    }

    public static synchronized void remove(String string) {
        Store store = AccountManager.load();
        boolean bl = string != null && string.equals(store.active);
        store.accounts.removeIf(minecraftSession -> string != null && string.equals(minecraftSession.username));
        if (bl) {
            store.active = store.accounts.isEmpty() ? "" : store.accounts.get((int)0).username;
        }
        AccountManager.save(store);
    }

    public static synchronized void setActive(String string) {
        Store store = AccountManager.load();
        for (MinecraftSession minecraftSession : store.accounts) {
            if (string == null || !string.equals(minecraftSession.username)) continue;
            store.active = string;
            AccountManager.save(store);
            return;
        }
    }

    public static synchronized void migrateLegacy() {
        if (AccountManager.file().exists()) {
            return;
        }
        MinecraftSession minecraftSession = MinecraftSession.loadFromDisk();
        if (minecraftSession != null) {
            AccountManager.add(minecraftSession);
        }
    }

    public static class Store {
        public String active = "";
        public List<MinecraftSession> accounts = new ArrayList<MinecraftSession>();
    }
}


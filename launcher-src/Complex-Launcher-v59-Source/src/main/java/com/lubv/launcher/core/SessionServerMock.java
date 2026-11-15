/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public final class SessionServerMock {
    private static volatile HttpServer server;
    private static final Map<String, SkinData> SKINS;
    private static final Map<String, SkinData> TOKENS;

    private SessionServerMock() {
    }

    public static void setSkin(String string, SkinData skinData) {
        SKINS.put(string.replace("-", ""), skinData);
    }

    public static void registerToken(String string, SkinData skinData) {
        if (string != null && !string.isBlank()) {
            TOKENS.put(string, skinData);
        }
    }

    public static synchronized int start() {
        if (server != null) {
            return server.getAddress().getPort();
        }
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            httpServer.createContext("/session/minecraft/profile/", httpExchange -> SessionServerMock.handleProfile(httpExchange));
            httpServer.createContext("/session/minecraft/hasJoined", httpExchange -> SessionServerMock.handleHasJoined(httpExchange));
            httpServer.createContext("/minecraft/profile", httpExchange -> SessionServerMock.handleServicesProfile(httpExchange));
            httpServer.createContext("/minecraft/profile/", httpExchange -> SessionServerMock.handleProfile(httpExchange));
            httpServer.createContext("/player/attributes", httpExchange -> SessionServerMock.handlePrivileges(httpExchange));
            httpServer.createContext("/authserver/", httpExchange -> SessionServerMock.handleAuth(httpExchange));
            httpServer.createContext("/auth/", httpExchange -> SessionServerMock.handleAuth(httpExchange));
            httpServer.createContext("/game/", httpExchange -> {
                SessionServerMock.sendJson(httpExchange, 200, "OK");
                httpExchange.close();
            });
            httpServer.createContext("/server/", httpExchange -> SessionServerMock.handleServerPing(httpExchange));
            httpServer.createContext("/privileges", httpExchange -> SessionServerMock.handlePrivileges(httpExchange));
            httpServer.createContext("/privacy/blocklist", httpExchange -> {
                SessionServerMock.sendJson(httpExchange, 200, "[]");
                httpExchange.close();
            });
            httpServer.createContext("/", httpExchange -> {
                SessionServerMock.sendJson(httpExchange, 200, "{}");
                httpExchange.close();
            });
            httpServer.setExecutor(Executors.newCachedThreadPool(runnable -> {
                Thread thread = new Thread(runnable, "session-mock");
                thread.setDaemon(true);
                return thread;
            }));
            httpServer.start();
            server = httpServer;
            return httpServer.getAddress().getPort();
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return -1;
        }
    }

    public static int port() {
        return server != null ? server.getAddress().getPort() : -1;
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        SKINS.clear();
        TOKENS.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void handleProfile(HttpExchange httpExchange) throws IOException {
        try {
            String string;
            String string2 = httpExchange.getRequestURI().getPath();
            String string3 = "/session/minecraft/profile/";
            int n = string2.indexOf(string3);
            if (n >= 0) {
                string = string2.substring(n + string3.length()).trim();
            } else {
                string = string2.replace("/minecraft/profile/", "").trim();
                if (string.contains("/")) {
                    string = string.split("/")[0];
                }
            }
            String string4 = string.replace("-", "");
            SkinData skinData = SKINS.get(string4);
            if (skinData != null) {
                String string5 = SessionServerMock.buildProfileJson(skinData);
                SessionServerMock.sendJson(httpExchange, 200, string5);
            } else {
                SessionServerMock.sendJson(httpExchange, 200, "{}");
            }
        }
        catch (Exception exception) {
            SessionServerMock.sendJson(httpExchange, 200, "{}");
        }
        finally {
            httpExchange.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void handleHasJoined(HttpExchange httpExchange) throws IOException {
        try {
            Object object;
            String string = httpExchange.getRequestURI().getQuery();
            String string2 = "";
            String string3 = "";
            if (string != null) {
                for (String string4 : string.split("&")) {
                    String[] stringArray = string4.split("=", 2);
                    if (stringArray.length != 2) continue;
                    if ("uuid".equals(stringArray[0])) {
                        string2 = stringArray[1].replace("-", "");
                    }
                    if (!"serverId".equals(stringArray[0])) continue;
                    string3 = stringArray[1];
                }
            }
            if ((object = SKINS.get(string2)) != null) {
                String string5 = SessionServerMock.buildProfileJson((SkinData)object);
                SessionServerMock.sendJson(httpExchange, 200, string5);
            } else {
                SessionServerMock.sendJson(httpExchange, 200, "{}");
            }
        }
        catch (Exception exception) {
            SessionServerMock.sendJson(httpExchange, 200, "{}");
        }
        finally {
            httpExchange.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void handleServicesProfile(HttpExchange httpExchange) throws IOException {
        try {
            String string = httpExchange.getRequestURI().getPath();
            if (string.contains("/names")) {
                String string2 = string.replace("/minecraft/profile/", "").split("/")[0].replace("-", "");
                SkinData skinData = SKINS.get(string2);
                if (skinData != null) {
                    JsonArray jsonArray = new JsonArray();
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("name", skinData.username);
                    jsonObject.addProperty("changedToAt", 0);
                    jsonArray.add(jsonObject);
                    SessionServerMock.sendJson(httpExchange, 200, jsonArray.toString());
                } else {
                    SessionServerMock.sendJson(httpExchange, 200, "[]");
                }
            } else if (!SKINS.isEmpty()) {
                SkinData skinData = SKINS.values().iterator().next();
                String string3 = SessionServerMock.buildServicesProfileJson(skinData);
                SessionServerMock.sendJson(httpExchange, 200, string3);
            } else {
                SessionServerMock.sendJson(httpExchange, 200, "{}");
            }
        }
        catch (Exception exception) {
            SessionServerMock.sendJson(httpExchange, 200, "{}");
        }
        finally {
            httpExchange.close();
        }
    }

    private static void handleGameJoin(HttpExchange httpExchange) throws IOException {
        try {
            SessionServerMock.sendJson(httpExchange, 200, "OK");
        }
        catch (Exception exception) {
            SessionServerMock.sendJson(httpExchange, 200, "OK");
        }
        finally {
            httpExchange.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void handleServerPing(HttpExchange httpExchange) throws IOException {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("description", "Mock Server");
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("name", "1.8.9");
            jsonObject2.addProperty("protocol", 47);
            jsonObject.add("version", jsonObject2);
            JsonObject jsonObject3 = new JsonObject();
            jsonObject3.addProperty("max", 20);
            jsonObject3.addProperty("online", 0);
            jsonObject.add("players", jsonObject3);
            SessionServerMock.sendJson(httpExchange, 200, jsonObject.toString());
        }
        catch (Exception exception) {
            SessionServerMock.sendJson(httpExchange, 200, "{}");
        }
        finally {
            httpExchange.close();
        }
    }

    private static void handleAuth(HttpExchange httpExchange) throws IOException {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("accessToken", "mock-token");
            jsonObject.addProperty("clientToken", "mock-client");
            SessionServerMock.sendJson(httpExchange, 200, jsonObject.toString());
        }
        catch (Exception exception) {
            SessionServerMock.sendJson(httpExchange, 200, "{}");
        }
        finally {
            httpExchange.close();
        }
    }

    private static void handlePrivileges(HttpExchange httpExchange) throws IOException {
        try {
            SessionServerMock.sendJson(httpExchange, 200, SessionServerMock.buildFullPrivilegesJson());
        }
        catch (Exception exception) {
            SessionServerMock.sendJson(httpExchange, 200, SessionServerMock.buildFullPrivilegesJson());
        }
        finally {
            httpExchange.close();
        }
    }

    private static String buildFullPrivilegesJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("enabled", true);
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.add("onlineChat", jsonObject);
        JsonObject jsonObject3 = new JsonObject();
        jsonObject3.addProperty("enabled", true);
        jsonObject2.add("multiplayerServer", jsonObject3);
        JsonObject jsonObject4 = new JsonObject();
        jsonObject4.addProperty("enabled", true);
        jsonObject2.add("multiplayerRealms", jsonObject4);
        JsonObject jsonObject5 = new JsonObject();
        jsonObject5.addProperty("enabled", true);
        jsonObject2.add("telemetry", jsonObject5);
        JsonObject jsonObject6 = new JsonObject();
        jsonObject6.addProperty("profanityFilterOn", false);
        JsonObject jsonObject7 = new JsonObject();
        jsonObject7.add("bannedScopes", new JsonObject());
        JsonObject jsonObject8 = new JsonObject();
        jsonObject8.add("privileges", jsonObject2);
        jsonObject8.add("profanityFilterPreferences", jsonObject6);
        jsonObject8.add("banStatus", jsonObject7);
        return jsonObject8.toString();
    }

    private static String buildProfileJson(SkinData skinData) {
        String string = SessionServerMock.buildTex(skinData);
        String string2 = Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "textures");
        jsonObject.addProperty("value", string2);
        jsonArray.add(jsonObject);
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("id", skinData.uuid.replace("-", ""));
        jsonObject2.addProperty("name", skinData.username);
        jsonObject2.add("properties", jsonArray);
        return jsonObject2.toString();
    }

    private static String buildServicesProfileJson(SkinData skinData) {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "0");
        jsonObject.addProperty("state", "ACTIVE");
        jsonObject.addProperty("url", skinData.skinUrl);
        jsonObject.addProperty("variant", "CLASSIC");
        jsonArray.add(jsonObject);
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("id", skinData.uuid.replace("-", ""));
        jsonObject2.addProperty("name", skinData.username);
        jsonObject2.add("skins", jsonArray);
        jsonObject2.add("capes", new JsonArray());
        return jsonObject2.toString();
    }

    private static String buildTex(SkinData skinData) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("timestamp", System.currentTimeMillis());
        jsonObject.addProperty("profileId", skinData.uuid.replace("-", ""));
        jsonObject.addProperty("profileName", skinData.username);
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("url", skinData.skinUrl);
        JsonObject jsonObject3 = new JsonObject();
        jsonObject3.add("SKIN", jsonObject2);
        jsonObject.add("textures", jsonObject3);
        return jsonObject.toString();
    }

    private static void sendJson(HttpExchange httpExchange, int n, String string) throws IOException {
        byte[] byArray = string.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(n, byArray.length);
        try (OutputStream outputStream = httpExchange.getResponseBody();){
            outputStream.write(byArray);
        }
    }

    static {
        SKINS = new ConcurrentHashMap<String, SkinData>();
        TOKENS = new ConcurrentHashMap<String, SkinData>();
    }

    public static class SkinData {
        public final String uuid;
        public final String username;
        public final String skinUrl;

        public SkinData(String string, String string2, String string3) {
            this.uuid = string;
            this.username = string2;
            this.skinUrl = string3 != null ? string3 : "";
        }
    }
}


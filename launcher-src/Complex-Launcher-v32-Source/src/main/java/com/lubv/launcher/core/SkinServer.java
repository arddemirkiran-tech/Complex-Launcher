/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.core;

import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.Executors;

public final class SkinServer {
    private static volatile HttpServer server;
    private static volatile File skinsDir;

    private SkinServer() {
    }

    public static synchronized int start(File dir) {
        if (server != null) {
            return server.getAddress().getPort();
        }
        skinsDir = dir;
        try {
            skinsDir.mkdirs();
            HttpServer s = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            s.createContext("/skins", exchange -> {
                String path = exchange.getRequestURI().getPath();
                String fileName = path.substring(path.lastIndexOf(47) + 1);
                File f = new File(skinsDir, fileName);
                if (f.isFile()) {
                    byte[] bytes = Files.readAllBytes(f.toPath());
                    exchange.getResponseHeaders().set("Content-Type", "image/png");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody();){
                        os.write(bytes);
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1L);
                }
                exchange.close();
            });
            s.setExecutor(Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "skin-server");
                t.setDaemon(true);
                return t;
            }));
            s.start();
            server = s;
            return s.getAddress().getPort();
        }
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int port() {
        return server != null ? server.getAddress().getPort() : -1;
    }

    public static String skinUrl(String fileName) {
        int p = SkinServer.port();
        if (p <= 0) {
            return null;
        }
        return "http://127.0.0.1:" + p + "/skins/" + fileName;
    }
}


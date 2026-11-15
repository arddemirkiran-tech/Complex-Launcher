package com.lubv.agent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class IpcClient {
    private volatile int port = -1;
    private volatile String currentName = null;
    private volatile boolean connected = false;
    private final AtomicInteger cmdId = new AtomicInteger(0);
    private volatile Runnable onNameChanged;

    public void setOnNameChanged(Runnable runnable) {
        this.onNameChanged = runnable;
    }

    public void connect() {
        for (int i = 0; i < 30; ++i) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException interruptedException) {
                return;
            }
            if (!this.tryConnect()) continue;
            System.out.println("[ComplexAgent] Launcher baglantisi kuruldu (port " + this.port + ")");
            this.startKeepAlive();
            return;
        }
        System.out.println("[ComplexAgent] Launcher bulunamadi (standalone modda calisiliyor)");
    }

    private boolean tryConnect() {
        try {
            for (Path path : IpcClient.configPaths()) {
                if (!Files.exists(path, new LinkOption[0])) continue;
                String config = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
                if (config.isEmpty()) continue;
                int candidate = IpcClient.extractInt(config, "port");
                if (candidate <= 0) continue;
                String pong = this.sendRaw(candidate, "PING");
                if (!"PONG".equals(pong)) continue;
                this.port = candidate;
                this.connected = true;
                this.currentName = this.sendRaw(this.port, "GETNAME");
                return true;
            }
        } catch (Exception exception) {
            // ignore, try next round
        }
        return false;
    }

    private void startKeepAlive() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(10000L);
                        if (IpcClient.this.port <= 0) continue;
                        String pong = IpcClient.this.sendRaw(IpcClient.this.port, "PING");
                        IpcClient.this.connected = "PONG".equals(pong);
                    }
                } catch (Exception exception) {
                    IpcClient.this.connected = false;
                }
            }
        }, "complex-keepalive");
        thread.setDaemon(true);
        thread.start();
    }

    public boolean isConnected() {
        return this.connected && this.port > 0;
    }

    public String getCurrentName() {
        return this.currentName;
    }

    public boolean setName(String string) {
        try {
            String response = this.sendRaw(this.port, "SETNAME:" + string);
            if (response != null && response.startsWith("OK:")) {
                this.currentName = string;
                Runnable runnable = this.onNameChanged;
                if (runnable != null) {
                    runnable.run();
                }
                return true;
            }
        } catch (Exception exception) {
            // ignore
        }
        return false;
    }

    public boolean requestSecondClient(String string) {
        try {
            String command = string != null && !string.trim().isEmpty() ? "SECONDCLIENT:" + string : "SECONDCLIENT";
            String response = this.sendRaw(this.port, command);
            return response != null && response.startsWith("OK:");
        } catch (Exception exception) {
            return false;
        }
    }

    private String sendRaw(int portNumber, String message) {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", portNumber);
            socket.setSoTimeout(2000);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer.println(message);
            String response = reader.readLine();
            socket.close();
            return response;
        } catch (Exception exception) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ignore) {
                }
            }
            return null;
        }
    }

    private static int extractInt(String json, String key) {
        try {
            int idx = json.indexOf("\"" + key + "\"");
            if (idx < 0) {
                return -1;
            }
            int colon = json.indexOf(58, idx);
            int end = json.indexOf(44, colon);
            if (end < 0) {
                end = json.indexOf(125, colon);
            }
            return Integer.parseInt(json.substring(colon + 1, end).trim());
        } catch (Exception exception) {
            return -1;
        }
    }

    private static Path[] configPaths() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home");
        String appdata = System.getenv("APPDATA");
        String userDir = System.getProperty("user.dir", home);
        if (os.contains("win")) {
            return new Path[]{
                Paths.get(appdata != null ? appdata : home, "ComplexLauncher", "complextools.json"),
                Paths.get(userDir, "complextools.json"),
                Paths.get(appdata != null ? appdata : home, ".minecraft", "complextools.json")
            };
        }
        return new Path[]{
            Paths.get(home, ".ComplexLauncher", "complextools.json"),
            Paths.get(userDir, "complextools.json"),
            Paths.get(home, ".minecraft", "complextools.json")
        };
    }
}

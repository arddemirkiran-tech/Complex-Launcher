/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.core;

import com.lubv.launcher.auth.MinecraftSession;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.game.GameLauncher;
import com.lubv.launcher.game.VersionManifest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class NameChangeServer {
    public static final int DEFAULT_PORT = 25797;
    private ServerSocket serverSocket;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private int boundPort;
    private MinecraftSession session;
    private Runnable onSecondClientRequest;
    private Consumer<String> logger;
    private VersionManifest manifest;
    private String versionId;
    private String loader;
    private int ramGB;
    private String jvmArgs;
    private String javaPath;
    private String gpuSelection;
    private File gameDir;

    public NameChangeServer(MinecraftSession session, Consumer<String> logger) {
        this.session = session;
        this.logger = logger;
    }

    public void setLaunchContext(VersionManifest manifest, String versionId, String loader, int ramGB, String jvmArgs, String javaPath, String gpuSelection, File gameDir) {
        this.manifest = manifest;
        this.versionId = versionId;
        this.loader = loader;
        this.ramGB = ramGB;
        this.jvmArgs = jvmArgs;
        this.javaPath = javaPath;
        this.gpuSelection = gpuSelection;
        this.gameDir = gameDir;
    }

    public void setOnSecondClient(Runnable cb) {
        this.onSecondClientRequest = cb;
    }

    public void updateSession(MinecraftSession s) {
        this.session = s;
    }

    public int getBoundPort() {
        return this.boundPort;
    }

    public void start() throws IOException {
        for (int p = 25797; p <= 25820; ++p) {
            try {
                this.serverSocket = new ServerSocket(p, 5, InetAddress.getByName("127.0.0.1"));
                this.boundPort = p;
                break;
            }
            catch (IOException iOException) {
                continue;
            }
        }
        if (this.serverSocket == null) {
            throw new IOException("25797-25820 aras\u0131 port bulunamad\u0131");
        }
        this.running.set(true);
        this.writeConfig();
        this.log("NameChangeServer ba\u015flat\u0131ld\u0131  localhost:" + this.boundPort);
        Thread t = new Thread(this::acceptLoop, "name-change-server");
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        this.running.set(false);
        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.deleteConfig();
        this.log("NameChangeServer durduruldu.");
    }

    private void acceptLoop() {
        while (this.running.get()) {
            try {
                Socket client = this.serverSocket.accept();
                Thread h = new Thread(() -> this.handle(client), "ncs-handler");
                h.setDaemon(true);
                h.start();
            }
            catch (IOException e) {
                if (!this.running.get()) continue;
                this.log("Ba\u011flant\u0131 hatas\u0131: " + e.getMessage());
            }
        }
    }

    private void handle(Socket client) {
        try (Socket socket = client;
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter((Writer)new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);){
            String line = in.readLine();
            if (line == null) {
                return;
            }
            if ("PING".equals(line = line.trim())) {
                out.println("PONG");
            } else if ("GETNAME".equals(line)) {
                out.println(this.session != null ? this.session.username : "unknown");
            } else if (line.startsWith("SETNAME:")) {
                String newName = line.substring(8).trim();
                if (newName.isEmpty() || newName.length() > 16 || !newName.matches("[a-zA-Z0-9_]+")) {
                    out.println("ERR:INVALID_NAME");
                    return;
                }
                if (this.session != null) {
                    this.session.username = newName;
                    this.session.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + newName).getBytes(StandardCharsets.UTF_8)).toString();
                    this.session.saveToDisk();
                    this.log("\u0130sim de\u011fi\u015ftirildi  " + newName);
                    out.println("OK:" + newName);
                } else {
                    out.println("ERR:NO_SESSION");
                }
            } else if (line.equals("SECONDCLIENT") || line.startsWith("SECONDCLIENT:")) {
                String customName2 = line.contains(":") ? line.substring(line.indexOf(58) + 1).trim() : null;
                out.println("OK:LAUNCHING");
                this.launchSecondClient(customName2);
            } else {
                out.println("ERR:UNKNOWN");
            }
        }
        catch (IOException e) {
            this.log("Handler hatas\u0131: " + e.getMessage());
        }
    }

    public void launchSecondClient(String customName) {
        if (this.manifest == null || this.session == null) {
            this.log("\u0130kinci istemci: eksik ba\u011flam.");
            return;
        }
        String user2 = customName != null && !customName.isBlank() ? customName : (this.session.username.length() <= 13 ? this.session.username : this.session.username.substring(0, 13)) + "_2";
        MinecraftSession session2 = MinecraftSession.offline(user2);
        this.log("\u0130kinci istemci ba\u015flat\u0131l\u0131yor: " + user2);
        new Thread(() -> {
            try {
                GameLauncher.launch(this.manifest, this.versionId, this.loader, session2, this.ramGB, this.jvmArgs, this.javaPath, this.gpuSelection, "", this.gameDir, new GameLauncher.ProgressCallback(){

                    @Override
                    public void onLog(String msg) {
                        NameChangeServer.this.log("[2.\u0130stemci] " + msg);
                    }

                    @Override
                    public void onProgress(int pct, String stage) {
                    }
                });
            }
            catch (Exception e) {
                this.log("\u0130kinci istemci hatas\u0131: " + e.getMessage());
            }
        }, "second-client").start();
    }

    private void launchSecondClientInternal() {
        if (this.manifest == null || this.session == null) {
            this.log("\u0130kinci istemci: eksik ba\u011flam.");
            return;
        }
        String base = this.session.username;
        String user2 = (base.length() <= 13 ? base : base.substring(0, 13)) + "_2";
        MinecraftSession session2 = MinecraftSession.offline(user2);
        this.log("\u0130kinci istemci ba\u015flat\u0131l\u0131yor: " + user2);
        new Thread(() -> {
            try {
                GameLauncher.launch(this.manifest, this.versionId, this.loader, session2, this.ramGB, this.jvmArgs, this.javaPath, this.gpuSelection, "", this.gameDir, new GameLauncher.ProgressCallback(){

                    @Override
                    public void onLog(String msg) {
                        NameChangeServer.this.log("[2.\u0130stemci] " + msg);
                    }

                    @Override
                    public void onProgress(int pct, String stage) {
                    }
                });
            }
            catch (Exception e) {
                this.log("\u0130kinci istemci hatas\u0131: " + e.getMessage());
            }
        }, "second-client").start();
    }

    private File configFile() {
        return new File(Paths.GAME_DIR, "complextools.json");
    }

    private void writeConfig() {
        try {
            String json = "{\"port\":" + this.boundPort + ",\"version\":\"" + (this.versionId != null ? this.versionId : "") + "\",\"loader\":\"" + (this.loader != null ? this.loader : "Vanilla") + "\"}";
            Files.writeString(this.configFile().toPath(), (CharSequence)json, StandardCharsets.UTF_8, new OpenOption[0]);
            if (this.gameDir != null && this.gameDir.exists()) {
                try {
                    Files.writeString(new File(this.gameDir, "complextools.json").toPath(), (CharSequence)json, StandardCharsets.UTF_8, new OpenOption[0]);
                }
                catch (Exception exception) {}
            }
        }
        catch (Exception e) {
            this.log("Config yaz\u0131lamad\u0131: " + e.getMessage());
        }
    }

    private void deleteConfig() {
        try {
            this.configFile().delete();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void log(String msg) {
        if (this.logger != null) {
            this.logger.accept(msg);
        }
    }
}


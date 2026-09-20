/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.ui;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.invoke.CallSite;
import java.lang.management.ManagementFactory;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class DiscordRpc {
    private static final String CLIENT_ID = "1543571969176248320";
    private static final AtomicBoolean running = new AtomicBoolean(false);
    private static final AtomicReference<ActivityInfo> pending = new AtomicReference();
    private static Thread workerThread;
    private static volatile String instanceName;
    private static volatile String loader;
    private static volatile String mcVersion;
    private static volatile int modCount;
    private static volatile boolean inGame;
    private static volatile long gameStartMs;

    private DiscordRpc() {
    }

    public static void start(String string) {
        String string2 = instanceName = string != null && !string.isEmpty() ? string : "Complex Launcher";
        if (running.get()) {
            return;
        }
        running.set(true);
        workerThread = new Thread(DiscordRpc::rpcLoop, "discord-rpc");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public static void stop() {
        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    public static void updateInstance(String string) {
        instanceName = string != null && !string.isEmpty() ? string : "Complex Launcher";
        loader = "";
        mcVersion = "";
        modCount = 0;
        inGame = false;
        pending.set(new ActivityInfo(instanceName, loader, mcVersion, modCount, false, 0L));
    }

    public static void onGameStart(String string, String string2, String string3, int n) {
        instanceName = string != null ? string : instanceName;
        loader = string2 != null ? string2 : "";
        mcVersion = string3 != null ? string3 : "";
        modCount = n;
        inGame = true;
        gameStartMs = System.currentTimeMillis();
        pending.set(new ActivityInfo(instanceName, loader, mcVersion, modCount, true, gameStartMs));
    }

    public static void onGameStop() {
        inGame = false;
        gameStartMs = 0L;
        pending.set(new ActivityInfo(instanceName, loader, mcVersion, modCount, false, 0L));
    }

    public static void updateModCount(int n) {
        modCount = n;
        pending.set(new ActivityInfo(instanceName, loader, mcVersion, modCount, inGame, gameStartMs));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void rpcLoop() {
        while (running.get()) {
            try {
                Connection connection = DiscordRpc.connect();
                if (connection == null) {
                    Thread.sleep(10000L);
                    continue;
                }
                try {
                    DiscordRpc.sendFrame(connection, 0, "{\"v\":1,\"client_id\":\"1543571969176248320\"}");
                    DiscordRpc.readFrame(connection);
                    DiscordRpc.sendActivity(connection, new ActivityInfo(instanceName, loader, mcVersion, modCount, inGame, gameStartMs));
                    long l = System.currentTimeMillis();
                    while (running.get()) {
                        ActivityInfo activityInfo = pending.getAndSet(null);
                        if (activityInfo != null) {
                            DiscordRpc.sendActivity(connection, activityInfo);
                        }
                        if (System.currentTimeMillis() - l > 15000L) {
                            DiscordRpc.sendFrame(connection, 3, "{}");
                            try {
                                DiscordRpc.readFrame(connection);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            l = System.currentTimeMillis();
                        }
                        Thread.sleep(500L);
                    }
                }
                finally {
                    connection.close();
                }
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                break;
            }
            catch (Exception exception) {
                try {
                    Thread.sleep(5000L);
                }
                catch (InterruptedException interruptedException) {
                    break;
                }
            }
        }
    }

    private static void sendActivity(Connection connection, ActivityInfo activityInfo) throws IOException {
        String string;
        String string2;
        String string3;
        CharSequence charSequence;
        Object object;
        if (activityInfo.inGame) {
            String string4;
            object = "\ud83c\udfae Oynuyor" + (String)(activityInfo.mcVersion.isEmpty() ? "" : " \u2014 MC " + activityInfo.mcVersion);
            charSequence = activityInfo.loader.isEmpty() ? "" : activityInfo.loader;
            String string5 = string4 = activityInfo.modCount > 0 ? activityInfo.modCount + " mod" : "";
            string3 = ((String)charSequence).isEmpty() && string4.isEmpty() ? activityInfo.instanceName : activityInfo.instanceName + (String)(((String)charSequence).isEmpty() ? "" : " \u00b7 " + (String)charSequence) + (String)(string4.isEmpty() ? "" : " \u00b7 " + string4);
            string2 = "logo_1024";
            string = activityInfo.loader.isEmpty() ? "Vanilla" : activityInfo.loader;
        } else {
            object = "Complex Launcher v35";
            string3 = activityInfo.instanceName.isEmpty() || activityInfo.instanceName.equals("Complex Launcher") ? "Instance se\u00e7iliyor..." : "\ud83d\udcc1 " + activityInfo.instanceName;
            string2 = null;
            string = null;
        }
        charSequence = new StringBuilder();
        ((StringBuilder)charSequence).append("{\"cmd\":\"SET_ACTIVITY\",\"args\":{");
        ((StringBuilder)charSequence).append("\"pid\":").append(ProcessHandle.current().pid()).append(",");
        ((StringBuilder)charSequence).append("\"activity\":{");
        ((StringBuilder)charSequence).append("\"details\":\"").append(DiscordRpc.esc((String)object)).append("\",");
        ((StringBuilder)charSequence).append("\"state\":\"").append(DiscordRpc.esc(string3)).append("\",");
        long l = activityInfo.inGame && activityInfo.startMs > 0L ? activityInfo.startMs / 1000L : ManagementFactory.getRuntimeMXBean().getStartTime() / 1000L;
        ((StringBuilder)charSequence).append("\"timestamps\":{\"start\":").append(l).append("},");
        ((StringBuilder)charSequence).append("\"assets\":{");
        ((StringBuilder)charSequence).append("\"large_image\":\"logo_1024\",");
        ((StringBuilder)charSequence).append("\"large_text\":\"Complex Launcher v35\"");
        if (string2 != null) {
            ((StringBuilder)charSequence).append(",\"small_image\":\"").append(DiscordRpc.esc(string2)).append("\"");
            ((StringBuilder)charSequence).append(",\"small_text\":\"").append(DiscordRpc.esc(string)).append("\"");
        }
        ((StringBuilder)charSequence).append("}");
        ((StringBuilder)charSequence).append("}}");
        ((StringBuilder)charSequence).append(",\"nonce\":\"").append(UUID.randomUUID()).append("\"");
        ((StringBuilder)charSequence).append("}");
        DiscordRpc.sendFrame(connection, 1, ((StringBuilder)charSequence).toString());
        connection.flush();
        try {
            DiscordRpc.readFrame(connection);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void sendFrame(Connection connection, int n, String string) throws IOException {
        byte[] byArray = string.getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(8 + byArray.length).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(n);
        byteBuffer.putInt(byArray.length);
        byteBuffer.put(byArray);
        connection.write(byteBuffer.array());
        connection.flush();
    }

    private static String readFrame(Connection connection) throws IOException {
        byte[] byArray = connection.readExact(8);
        if (byArray == null) {
            throw new IOException("Connection closed");
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(byArray).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.getInt();
        int n = byteBuffer.getInt();
        if (n <= 0 || n > 65536) {
            return "";
        }
        byte[] byArray2 = connection.readExact(n);
        return byArray2 != null ? new String(byArray2, StandardCharsets.UTF_8) : "";
    }

    private static Connection connect() {
        String string = System.getProperty("os.name", "").toLowerCase();
        try {
            if (string.contains("win")) {
                for (int i = 0; i < 10; ++i) {
                    try {
                        RandomAccessFile randomAccessFile = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
                        return new PipeConnection(randomAccessFile);
                    }
                    catch (Exception exception) {
                        continue;
                    }
                }
            } else {
                String string2 = System.getenv("XDG_RUNTIME_DIR");
                String string3 = System.getenv("XDG_RUNTIME_DIR");
                for (int i = 0; i < 10; ++i) {
                    ArrayList<String> arrayList = new ArrayList<String>();
                    if (string2 != null) {
                        arrayList.add(string2 + "/discord-ipc-" + i);
                        arrayList.add(string2 + "/app/com.discordapp.Discord/discord-ipc-" + i);
                    }
                    arrayList.add("/tmp/discord-ipc-" + i);
                    arrayList.add(System.getProperty("user.home") + "/.discord-ipc-" + i);
                    for (String string4 : arrayList) {
                        try {
                            File file = new File(string4);
                            if (!file.exists()) continue;
                            UnixDomainSocketAddress unixDomainSocketAddress = UnixDomainSocketAddress.of(file.toPath());
                            SocketChannel socketChannel = SocketChannel.open(StandardProtocolFamily.UNIX);
                            socketChannel.configureBlocking(true);
                            socketChannel.connect(unixDomainSocketAddress);
                            return new SocketConnection(socketChannel);
                        }
                        catch (Exception exception) {
                        }
                    }
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private static String esc(String string) {
        if (string == null) {
            return "";
        }
        return string.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    static {
        instanceName = "Complex Launcher";
        loader = "";
        mcVersion = "";
        modCount = 0;
        inGame = false;
        gameStartMs = 0L;
    }

    static class ActivityInfo {
        final String instanceName;
        final String loader;
        final String mcVersion;
        final int modCount;
        final boolean inGame;
        final long startMs;

        ActivityInfo(String string, String string2, String string3, int n, boolean bl, long l) {
            this.instanceName = string;
            this.loader = string2;
            this.mcVersion = string3;
            this.modCount = n;
            this.inGame = bl;
            this.startMs = l;
        }
    }

    static interface Connection {
        public void write(byte[] var1) throws IOException;

        public void flush() throws IOException;

        public byte[] readExact(int var1) throws IOException;

        public void close();
    }

    static class PipeConnection
    implements Connection {
        private final RandomAccessFile pipe;

        PipeConnection(RandomAccessFile randomAccessFile) {
            this.pipe = randomAccessFile;
        }

        @Override
        public void write(byte[] byArray) throws IOException {
            this.pipe.write(byArray);
        }

        @Override
        public void flush() {
        }

        @Override
        public byte[] readExact(int n) throws IOException {
            byte[] byArray = new byte[n];
            this.pipe.readFully(byArray);
            return byArray;
        }

        @Override
        public void close() {
            try {
                this.pipe.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    static class SocketConnection
    implements Connection {
        private final SocketChannel ch;

        SocketConnection(SocketChannel socketChannel) {
            this.ch = socketChannel;
        }

        @Override
        public void write(byte[] byArray) throws IOException {
            this.ch.write(ByteBuffer.wrap(byArray));
        }

        @Override
        public void flush() {
        }

        @Override
        public byte[] readExact(int n) throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.allocate(n);
            while (byteBuffer.hasRemaining()) {
                int n2 = this.ch.read(byteBuffer);
                if (n2 >= 0) continue;
                return null;
            }
            return byteBuffer.array();
        }

        @Override
        public void close() {
            try {
                this.ch.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}


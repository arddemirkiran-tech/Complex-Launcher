/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class RconClient
implements Closeable {
    private static final int SERVERDATA_AUTH = 3;
    private static final int SERVERDATA_EXECCOMMAND = 2;
    private static final int SERVERDATA_RESPONSE = 0;
    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;
    private final AtomicInteger reqId = new AtomicInteger(1);

    public RconClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.socket.setSoTimeout(5000);
        this.out = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
        this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
    }

    public void authenticate(String password) throws IOException {
        int id = this.reqId.getAndIncrement();
        this.sendPacket(id, 3, password);
        Packet resp = this.readPacket();
        if (resp.id == -1) {
            throw new IllegalArgumentException("RCON kimlik dogrulama basarisiz - sifre yanlis?");
        }
    }

    public String sendCommand(String command) throws IOException {
        int id = this.reqId.getAndIncrement();
        this.sendPacket(id, 2, command);
        Packet resp = this.readPacket();
        return resp.payload;
    }

    private void sendPacket(int id, int type, String payload) throws IOException {
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        int len = 8 + payloadBytes.length + 2;
        ByteBuffer buf = ByteBuffer.allocate(4 + len).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(len);
        buf.putInt(id);
        buf.putInt(type);
        buf.put(payloadBytes);
        buf.put((byte)0);
        buf.put((byte)0);
        this.out.write(buf.array());
        this.out.flush();
    }

    private Packet readPacket() throws IOException {
        int len = this.readInt();
        int id = this.readInt();
        int type = this.readInt();
        byte[] payload = new byte[len - 10];
        this.in.readFully(payload);
        this.in.read();
        this.in.read();
        return new Packet(id, type, new String(payload, StandardCharsets.UTF_8));
    }

    private int readInt() throws IOException {
        byte[] b = new byte[4];
        this.in.readFully(b);
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    @Override
    public void close() {
        try {
            this.socket.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private record Packet(int id, int type, String payload) {
    }
}


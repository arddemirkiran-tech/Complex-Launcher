package com.lubv.launcher.core;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Minecraft'in "Server List Ping" protokolunu (modern, 1.7+) uygulayan
 * hafif bir istemci. Bir sunucuya baglanip onun motd/oyuncu sayisi/
 * versiyon ve en onemlisi FAVICON'unu (64x64 base64 PNG, sunucunun
 * server-icon.png dosyasindan gelir) ceker.
 *
 * Bu, "sunucu eklerken kapak fotografini otomatik gostersin" ozelligi
 * icin gerekli - onceden launcher'da bu protokol hic uygulanmiyordu,
 * sadece basit bir TCP connect/disconnect ile ping suresi olculuyordu.
 */
public final class ServerPing {
    private ServerPing() {
    }

    public static class PingResult {
        public String motd = "";
        public String versionName = "";
        public int onlinePlayers = 0;
        public int maxPlayers = 0;
        public String faviconBase64 = null; // "data:image/png;base64,...." on-eki olmadan, ham base64
        public long latencyMs = -1;
    }

    /** Verilen host:port adresine baglanip status bilgisini ve favicon'u ceker. Basarisiz olursa null doner. */
    public static PingResult ping(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            long start = System.currentTimeMillis();
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            socket.setSoTimeout(timeoutMs);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // --- Handshake paketi (packet id 0x00) ---
            ByteArrayOutputStream handshake = new ByteArrayOutputStream();
            writeVarInt(handshake, 0x00);           // packet id
            writeVarInt(handshake, 763);             // protocol version (yaklasik guncel - sunucu genelde yine de status doner)
            writeVarIntPrefixedString(handshake, host);
            handshake.write((port >> 8) & 0xFF);
            handshake.write(port & 0xFF);
            writeVarInt(handshake, 1);               // next state: status
            writePacket(out, handshake.toByteArray());

            // --- Status request paketi (packet id 0x00, bos govde) ---
            ByteArrayOutputStream statusReq = new ByteArrayOutputStream();
            writeVarInt(statusReq, 0x00);
            writePacket(out, statusReq.toByteArray());

            // --- Status response okuma ---
            readVarInt(in); // toplam paket uzunlugu
            int packetId = readVarInt(in);
            if (packetId != 0x00) {
                return null;
            }
            int jsonLength = readVarInt(in);
            byte[] jsonBytes = new byte[jsonLength];
            in.readFully(jsonBytes);
            String json = new String(jsonBytes, StandardCharsets.UTF_8);

            long latency = System.currentTimeMillis() - start;

            PingResult result = new PingResult();
            result.latencyMs = latency;
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("description")) {
                Object descObj = root.get("description");
                if (root.get("description").isJsonObject() && root.getAsJsonObject("description").has("text")) {
                    result.motd = root.getAsJsonObject("description").get("text").getAsString();
                } else if (root.get("description").isJsonPrimitive()) {
                    result.motd = root.get("description").getAsString();
                }
            }
            if (root.has("version") && root.get("version").isJsonObject()) {
                JsonObject v = root.getAsJsonObject("version");
                if (v.has("name")) result.versionName = v.get("name").getAsString();
            }
            if (root.has("players") && root.get("players").isJsonObject()) {
                JsonObject p = root.getAsJsonObject("players");
                if (p.has("online")) result.onlinePlayers = p.get("online").getAsInt();
                if (p.has("max")) result.maxPlayers = p.get("max").getAsInt();
            }
            if (root.has("favicon") && !root.get("favicon").isJsonNull()) {
                String favicon = root.get("favicon").getAsString();
                // Format genelde "data:image/png;base64,AAAA..." seklindedir.
                int commaIdx = favicon.indexOf(',');
                result.faviconBase64 = commaIdx >= 0 ? favicon.substring(commaIdx + 1) : favicon;
            }
            return result;
        }
        catch (Exception e) {
            return null;
        }
    }

    private static void writePacket(DataOutputStream out, byte[] data) throws IOException {
        ByteArrayOutputStream lengthPrefixed = new ByteArrayOutputStream();
        writeVarInt(lengthPrefixed, data.length);
        out.write(lengthPrefixed.toByteArray());
        out.write(data);
        out.flush();
    }

    private static void writeVarInt(ByteArrayOutputStream out, int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.write(value);
                return;
            }
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }

    private static void writeVarIntPrefixedString(ByteArrayOutputStream out, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes, 0, bytes.length);
    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int value = 0;
        int position = 0;
        byte currentByte;
        while (true) {
            currentByte = in.readByte();
            value |= (currentByte & 0x7F) << position;
            if ((currentByte & 0x80) == 0) break;
            position += 7;
            if (position >= 32) throw new IOException("VarInt too big");
        }
        return value;
    }
}

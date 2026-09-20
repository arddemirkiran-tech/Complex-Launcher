package com.lubv.launcher.mods;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kurulu shader/resourcepack dosyalari ile Modrinth projeleri arasindaki
 * bagi kalici olarak saklar: <klasor>/installed_meta.json
 *
 * Bu sayede bilgi paneli dosya adindan "tahmin" etmek yerine dosyanin
 * HANGI Modrinth projesine ait oldugunu TAM olarak bilir - ModsPanel'deki
 * gibi dogru detay acilir.
 */
public final class InstallMeta {
    private InstallMeta() {
    }

    public static class Entry {
        public String slug;      // Modrinth slug (detay/link icin)
        public String title;     // Görünen baslik
        public String iconUrl;   // Ikon (opsiyonel)
        public String source;    // "modrinth" / "curseforge" / "local"
    }

    private static final Map<File, Map<String, Entry>> CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private static File metaFile(File dir) {
        return new File(dir, "installed_meta.json");
    }

    public static synchronized Map<String, Entry> load(File dir) {
        if (dir == null) {
            return new LinkedHashMap<>();
        }
        Map<String, Entry> cached = CACHE.get(dir);
        if (cached != null) {
            return cached;
        }
        Map<String, Entry> out = new LinkedHashMap<>();
        File f = metaFile(dir);
        if (f.isFile()) {
            try {
                String json = new String(java.nio.file.Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                for (Map.Entry<String, JsonElement> e : root.entrySet()) {
                    if (!e.getValue().isJsonObject()) continue;
                    JsonObject o = e.getValue().getAsJsonObject();
                    Entry en = new Entry();
                    en.slug = o.has("slug") && !o.get("slug").isJsonNull() ? o.get("slug").getAsString() : null;
                    en.title = o.has("title") && !o.get("title").isJsonNull() ? o.get("title").getAsString() : null;
                    en.iconUrl = o.has("iconUrl") && !o.get("iconUrl").isJsonNull() ? o.get("iconUrl").getAsString() : null;
                    en.source = o.has("source") && !o.get("source").isJsonNull() ? o.get("source").getAsString() : "modrinth";
                    out.put(e.getKey(), en);
                }
            } catch (Exception ignored) {
            }
        }
        CACHE.put(dir, out);
        return out;
    }

    public static synchronized void put(File dir, String fileName, Entry entry) {
        if (dir == null || fileName == null || entry == null) {
            return;
        }
        Map<String, Entry> map = load(dir);
        map.put(fileName, entry);
        save(dir, map);
    }

    public static synchronized Entry get(File dir, String fileName) {
        Map<String, Entry> map = load(dir);
        Entry e = map.get(fileName);
        if (e != null) {
            return e;
        }
        // Uzanti haric deneme (orn. "BSL Shaders.zip" -> "BSL Shaders")
        String base = fileName != null ? fileName.replaceAll("\\.(zip|jar)$", "") : null;
        return base != null ? map.get(base) : null;
    }

    public static synchronized void remove(File dir, String fileName) {
        Map<String, Entry> map = load(dir);
        if (map.remove(fileName) != null) {
            save(dir, map);
        }
    }

    private static void save(File dir, Map<String, Entry> map) {
        try {
            dir.mkdirs();
            JsonObject root = new JsonObject();
            for (Map.Entry<String, Entry> e : map.entrySet()) {
                Entry en = e.getValue();
                JsonObject o = new JsonObject();
                if (en.slug != null) o.addProperty("slug", en.slug);
                if (en.title != null) o.addProperty("title", en.title);
                if (en.iconUrl != null) o.addProperty("iconUrl", en.iconUrl);
                o.addProperty("source", en.source != null ? en.source : "modrinth");
                root.add(e.getKey(), o);
            }
            java.nio.file.Files.write(metaFile(dir).toPath(),
                root.toString().getBytes(StandardCharsets.UTF_8));
            CACHE.put(dir, map);
        } catch (IOException ignored) {
        }
    }

    public static Iterable<String> keys(File dir) {
        return Collections.unmodifiableSet(load(dir).keySet());
    }
}

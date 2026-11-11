package com.lubv.launcher.mods;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * v51: pre-launch compatibility check based on what the mods THEMSELVES declare.
 *
 * <p>Maks Performans used to install a fixed list of slugs and trust Modrinth's
 * per-project version filter. That is not enough: a jar can perfectly support
 * the game version and still refuse to load because of another mod. The real
 * crash this fixes (1.21.1 Fabric instance):
 *
 * <pre>
 *  More Culling 1.0.11 requires cloth-config &gt;= 15.0.0 (missing)
 *  More Culling 1.0.11 breaks Sodium &lt;= 0.6.13 (0.6.13 installed)
 *  Particle Core 0.3.3 requires fzzy_config &gt;= 0.7.5 (missing)
 * </pre>
 *
 * <p>Reads {@code fabric.mod.json} straight out of each jar ({@code depends} and
 * {@code breaks}), compares the declared version ranges against the jars that
 * are actually in {@code mods/}, and reports/removes the jars that Fabric would
 * reject. Forge/NeoForge jars are skipped (their metadata is a TOML file with a
 * different shape) - they are not silently "checked", they are simply not
 * verified here.
 */
public final class ModCompatibility {
    /** One detected incompatibility. */
    public static final class Problem {
        public final String fileName;
        public final String modId;
        public final String message;

        Problem(String fileName, String modId, String message) {
            this.fileName = fileName;
            this.modId = modId;
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    /** Ids provided by the game/loader itself; never "missing". */
    private static final java.util.Set<String> ENVIRONMENT_IDS = new java.util.HashSet<>(java.util.Arrays.asList(
        "minecraft", "java", "fabricloader", "fabric-loader", "fabric", "fabric-api", "quilt_loader", "quilt-loader"
    ));

    private ModCompatibility() {
    }

    /** Metadata read from one jar. */
    private static final class ModMeta {
        String fileName;
        String id = "";
        String version = "";
        /** "fabric", "forge", "neoforge" or "" when unknown. */
        String format = "";
        final Map<String, String[]> depends = new LinkedHashMap<>();
        final Map<String, String[]> breaks = new LinkedHashMap<>();
        /** Ids this jar PROVIDES for other mods (Fabric API ships dozens of them). */
        final Map<String, String> provides = new LinkedHashMap<>();
        boolean readable;
    }

    /** Checks every readable jar and returns the problems Fabric would raise. */
    public static List<Problem> check(File modsDir) {
        return check(modsDir, null);
    }

    /**
     * Same check, but also rejects jars built for a DIFFERENT loader than the
     * instance. A Fabric instance that received a NeoForge jar does not simply
     * "ignore" it: the jar's metadata is invisible to Fabric while other mods
     * still list it as a dependency, which is exactly how
     * "cloth-config ... which is missing" happens with cloth-config on disk.
     */
    public static List<Problem> check(File modsDir, String loader) {
        List<Problem> problems = new ArrayList<>();
        if (modsDir == null || !modsDir.isDirectory()) {
            return problems;
        }
        File[] jars = modsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            return problems;
        }        List<ModMeta> metas = new ArrayList<>();
        Map<String, ModMeta> byId = new LinkedHashMap<>();
        for (File jar : jars) {
            ModMeta meta = readMeta(jar);
            if (meta == null) {
                continue;
            }
            metas.add(meta);
            if (!meta.id.isEmpty()) {
                byId.putIfAbsent(meta.id, meta);
            }
        }
        // fabric-api gibi "bundle" jar'lar onlarca modul id'si saglar
        // (fabric-renderer-api-v1, fabric-resource-loader-v0 ...). Bunlari
        // saglayici olarak kaydetmezsek sodium gibi modlar icin olmayan
        // bir bagimlilik hatasi uydururuz - gercek Fabric ise yukler.
        Map<String, String> providedVersions = new LinkedHashMap<>();
        for (ModMeta meta : metas) {
            for (Map.Entry<String, String> p : meta.provides.entrySet()) {
                providedVersions.putIfAbsent(p.getKey(), p.getValue().isEmpty() ? meta.version : p.getValue());
            }
        }

        String want = loader == null ? "" : loader.toLowerCase();
        boolean instanceFabric = want.contains("fabric") || want.contains("quilt");
        boolean instanceForge = want.contains("neoforge") || want.contains("forge");
        for (ModMeta meta : metas) {
            if (!meta.format.isEmpty()) {
                boolean jarFabric = "fabric".equals(meta.format);
                if ((instanceFabric && !jarFabric) || (instanceForge && jarFabric)) {
                    problems.add(new Problem(meta.fileName, meta.id,
                        meta.fileName + " is a " + meta.format + " jar but the instance runs "
                            + loader + " - the loader cannot see it, so its dependencies look missing"));
                    continue;
                }
            }
            for (Map.Entry<String, String[]> dep : meta.depends.entrySet()) {
                String depId = dep.getKey();
                if (ENVIRONMENT_IDS.contains(depId) || depId.equals(meta.id)) {
                    continue;
                }
                ModMeta installed = byId.get(depId);
                if (installed == null) {
                    if (providedVersions.containsKey(depId)) {
                        continue; // baska bir jar bu id'yi sagliyor (Fabric API vb.)
                    }
                    problems.add(new Problem(meta.fileName, meta.id,
                        meta.fileName + " (" + meta.id + ") requires " + depId + " "
                            + joinRanges(dep.getValue()) + ", which is missing"));
                    continue;
                }
                if (!matchesAny(installed.version, dep.getValue())) {
                    problems.add(new Problem(meta.fileName, meta.id,
                        meta.fileName + " (" + meta.id + ") requires " + depId + " "
                            + joinRanges(dep.getValue()) + ", but " + installed.version + " is installed"));
                }
            }
            for (Map.Entry<String, String[]> bad : meta.breaks.entrySet()) {
                String badId = bad.getKey();
                if (ENVIRONMENT_IDS.contains(badId) || badId.equals(meta.id)) {
                    continue;
                }
                ModMeta installed = byId.get(badId);
                if (installed != null && matchesAny(installed.version, bad.getValue())) {
                    problems.add(new Problem(meta.fileName, meta.id,
                        meta.fileName + " (" + meta.id + ") is incompatible with " + badId + " "
                            + joinRanges(bad.getValue()) + ", but " + installed.version + " is installed"));
                }
            }
        }
        return problems;
    }

    /**
     * Removes the jars that declare an unsatisfiable requirement/incompatibility
     * (never the dependency they complain about), so the set Fabric receives is
     * one that actually loads.
     *
     * @return number of removed jars
     */
    public static int prune(File modsDir, Consumer<String> log) {
        return prune(modsDir, null, log);
    }

    /** Loader-aware prune: also removes jars built for the wrong loader. */
    public static int prune(File modsDir, String loader, Consumer<String> log) {
        List<Problem> problems = check(modsDir, loader);
        if (problems.isEmpty()) {
            if (log != null) {
                log.accept("[Uyumluluk] Mod seti temiz - cakisma yok.");
            }
            return 0;
        }
        java.util.LinkedHashSet<String> remove = new java.util.LinkedHashSet<>();
        for (Problem p : problems) {
            if (log != null) {
                log.accept("[Uyumluluk] " + p.message);
            }
            remove.add(p.fileName);
        }
        // Dosya SILINMEZ: jar .disabled olarak yeniden adlandirilir. Boylece
        // oyun acilir, kullanici isterse Modlar sekmesinden geri acar; launcher
        // geri alinamaz bir silme yapmaz.
        int removed = 0;
        List<ModManager.InstalledMod> registry = ModManager.loadRegistry(modsDir);
        for (String name : remove) {
            File jar = new File(modsDir, name);
            File disabled = new File(modsDir, name + ".disabled");
            if (disabled.isFile()) {
                disabled.delete();
            }
            boolean moved = jar.isFile() && jar.renameTo(disabled);
            if (moved) {
                removed++;
                if (log != null) {
                    log.accept("[Uyumluluk] Devre disi: " + name);
                }
            }
            for (ModManager.InstalledMod m : registry) {
                if (name.equals(m.fileName)) {
                    m.enabled = false;
                }
            }
        }
        if (removed > 0) {
            ModManager.saveRegistry(modsDir, registry);
            if (log != null) {
                log.accept("[Uyumluluk] " + removed + " mod devre disi birakildi - oyun artik yuklenebilir."
                    + " (Geri acmak icin Modlar sekmesi; dosyalar .disabled olarak duruyor.)");
            }
        }
        return removed;
    }

    /** Reads fabric.mod.json out of a jar; null when the jar has none (Forge/NeoForge). */
    private static ModMeta readMeta(File jar) {
        ZipFile zip = null;
        try {
            zip = new ZipFile(jar);
            ZipEntry entry = zip.getEntry("fabric.mod.json");
            if (entry == null) {
                // Fabric degil: yalnizca formati kaydet (loader uyusmazligi icin).
                if (zip.getEntry("META-INF/neoforge.mods.toml") != null) {
                    ModMeta forge = new ModMeta();
                    forge.fileName = jar.getName();
                    forge.format = "neoforge";
                    return forge;
                }
                if (zip.getEntry("META-INF/mods.toml") != null) {
                    ModMeta forge = new ModMeta();
                    forge.fileName = jar.getName();
                    forge.format = "forge";
                    return forge;
                }
                return null;
            }
            InputStream in = zip.getInputStream(entry);
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            in.close();

            ModMeta meta = new ModMeta();
            meta.fileName = jar.getName();
            meta.format = "fabric";
            meta.id = optString(root, "id");
            meta.version = optString(root, "version");
            readMap(root, "depends", meta.depends);
            readMap(root, "breaks", meta.breaks);
            readProvides(root, meta.provides);
            // fabric-api bir BUNDLE: 49 alt modul jar'i META-INF/jars/ altinda.
            // fabric-renderer-api-v1 gibi id'leri saglayanlar onlar; okunmazsa
            // sodium icin "olmayan bagimlilik" hatasi uydurulurdu.
            readNestedProvides(zip, meta.provides);
            meta.readable = !meta.id.isEmpty();
            return meta.readable ? meta : null;
        } catch (Exception e) {
            // Bozuk/eski jar: yukleme kararini etkilemeyiz, sadece atlariz.
            return null;
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (Exception ignored) {
                    // kapanis hatasi onemsiz
                }
            }
        }
    }

    private static String optString(JsonObject o, String key) {
        JsonElement e = o.get(key);
        if (e == null || e.isJsonNull() || !e.isJsonPrimitive()) {
            return "";
        }
        return e.getAsString();
    }

    private static void readMap(JsonObject root, String key, Map<String, String[]> out) {
        JsonElement e = root.get(key);
        if (e == null || !e.isJsonObject()) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : e.getAsJsonObject().entrySet()) {
            JsonElement v = entry.getValue();
            if (v.isJsonArray()) {
                JsonArray arr = v.getAsJsonArray();
                List<String> ranges = new ArrayList<>();
                for (JsonElement item : arr) {
                    if (item.isJsonPrimitive()) {
                        ranges.add(item.getAsString());
                    }
                }
                out.put(entry.getKey(), ranges.toArray(new String[0]));
            } else if (v.isJsonPrimitive()) {
                out.put(entry.getKey(), new String[]{v.getAsString()});
            }
        }
    }

    /** Reads META-INF/jars/*.jar (Fabric API bundle) and records their ids as provided. */
    private static void readNestedProvides(ZipFile outer, Map<String, String> out) {
        java.util.Enumeration<? extends ZipEntry> entries = outer.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!name.startsWith("META-INF/jars/") || !name.endsWith(".jar")) {
                continue;
            }
            java.io.InputStream raw = null;
            java.util.zip.ZipInputStream nested = null;
            try {
                raw = outer.getInputStream(entry);
                nested = new java.util.zip.ZipInputStream(raw);
                ZipEntry inner;
                while ((inner = nested.getNextEntry()) != null) {
                    if (!"fabric.mod.json".equals(inner.getName())) {
                        continue;
                    }
                    JsonObject root = JsonParser.parseReader(
                        new InputStreamReader(nested, StandardCharsets.UTF_8)).getAsJsonObject();
                    String id = optString(root, "id");
                    if (!id.isEmpty()) {
                        out.putIfAbsent(id, optString(root, "version"));
                    }
                    break;
                }
            } catch (Exception ignored) {
                // bozuk alt jar: atla
            } finally {
                if (nested != null) {
                    try {
                        nested.close();
                    } catch (Exception ignored) {
                        // onemsiz
                    }
                }
            }
        }
    }

    /**
     * "provides" entries are objects with an id and an optional version
     * ({@code "provides": [{"id": "fabric-api-base"}, ...]}).
     */
    private static void readProvides(JsonObject root, Map<String, String> out) {
        JsonElement e = root.get("provides");
        if (e == null || !e.isJsonArray()) {
            return;
        }
        for (JsonElement item : e.getAsJsonArray()) {
            if (item.isJsonPrimitive()) {
                out.put(item.getAsString(), "");
            } else if (item.isJsonObject()) {
                JsonObject o = item.getAsJsonObject();
                String id = optString(o, "id");
                if (!id.isEmpty()) {
                    out.put(id, optString(o, "version"));
                }
            }
        }
    }

    private static String joinRanges(String[] ranges) {
        return ranges == null || ranges.length == 0 ? "*" : String.join(" | ", ranges);
    }

    private static boolean matchesAny(String installedVersion, String[] ranges) {
        if (ranges == null || ranges.length == 0) {
            return true;
        }
        for (String range : ranges) {
            if (rangeMatches(installedVersion, range)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluates a Fabric version predicate: space-separated tokens are ANDed,
     * tokens may carry {@code >= > <= < = ~ ^} and {@code x}/{@code *} wildcards.
     */
    static boolean rangeMatches(String installedVersion, String range) {
        if (range == null) {
            return true;
        }
        String r = range.trim();
        if (r.isEmpty() || "*".equals(r)) {
            return true;
        }
        int[] installed = versionParts(installedVersion);
        for (String token : r.split("\\s+")) {
            String t = token.trim();
            if (t.isEmpty()) {
                continue;
            }
            String op = "";
            for (String candidate : new String[]{">=", "<=", ">", "<", "=", "~", "^"}) {
                if (t.startsWith(candidate)) {
                    op = candidate;
                    t = t.substring(candidate.length()).trim();
                    break;
                }
            }
            if (t.isEmpty()) {
                continue;
            }
            if (t.contains("x") || t.contains("X") || t.contains("*")) {
                // Prefixed match: 1.2.x matches any 1.2.*
                String prefix = t.replaceAll("(?i)[x*].*$", "").replaceAll("\\.$", "");
                int[] want = versionParts(prefix);
                int[] got = installed;
                int n = Math.min(want.length, got.length);
                for (int i = 0; i < n; i++) {
                    if (want[i] != got[i]) {
                        return false;
                    }
                }
                continue;
            }
            int[] want = versionParts(t);
            int cmp = compare(got(want, installed), want);
            switch (op) {
                case ">=":
                    if (cmp < 0) return false;
                    break;
                case "<=":
                    if (cmp > 0) return false;
                    break;
                case ">":
                    if (cmp <= 0) return false;
                    break;
                case "<":
                    if (cmp >= 0) return false;
                    break;
                case "~":
                    // same major (and minor for >= 3 components)
                    if (installed.length == 0 || want.length == 0 || installed[0] != want[0]) return false;
                    if (want.length > 1 && (installed.length < 2 || installed[1] != want[1])) return false;
                    break;
                case "^":
                    if (installed.length == 0 || want.length == 0 || installed[0] != want[0]) return false;
                    break;
                default:
                    if (cmp != 0) return false;
                    break;
            }
        }
        return true;
    }

    /** Pads the installed version to the range's length for comparison. */
    private static int[] got(int[] want, int[] installed) {
        if (installed.length >= want.length) {
            return installed;
        }
        int[] padded = new int[want.length];
        System.arraycopy(installed, 0, padded, 0, installed.length);
        return padded;
    }

    private static int compare(int[] a, int[] b) {
        int n = Math.max(a.length, b.length);
        for (int i = 0; i < n; i++) {
            int x = i < a.length ? a[i] : 0;
            int y = i < b.length ? b[i] : 0;
            if (x != y) {
                return Integer.compare(x, y);
            }
        }
        return 0;
    }

    /** Numeric components of a version, ignoring build metadata and pre-release tags. */
    static int[] versionParts(String version) {
        if (version == null) {
            return new int[0];
        }
        String v = version.trim();
        int cut = v.length();
        int plus = v.indexOf('+');
        if (plus >= 0) {
            cut = plus;
        }
        int dash = v.indexOf('-');
        if (dash >= 0 && dash < cut) {
            cut = dash;
        }
        v = v.substring(0, cut);
        String[] pieces = v.split("\\.");
        List<Integer> nums = new ArrayList<>();
        for (String piece : pieces) {
            String digits = piece.replaceAll("\\D", "");
            if (digits.isEmpty()) {
                continue;
            }
            try {
                nums.add(Integer.parseInt(digits));
            } catch (NumberFormatException e) {
                nums.add(0);
            }
        }
        int[] out = new int[nums.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = nums.get(i);
        }
        return out;
    }

    /** Convenience for callers that only want the count of problems. */
    public static int count(File modsDir) {
        return check(modsDir).size();
    }
}

package com.lubv.launcher.game;

import com.lubv.launcher.core.L10n;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Crash Analizi v2 - en yuksek seviye tani motoru.
 *
 * Crash raporunu / oyun logunu katman katman inceleyerek:
 *  - Gercek kok nedeni (Description veya son "Caused by" satiri)
 *  - Sorumlu MOD DOSYASINI (Forge "Mod File:" satiri, exception
 *    cevresindeki .jar yollari, mixin config adlari) tespit eder
 *  - Sorumlu mod bulunamazsa cagri zincirindeki framework DISI
 *    siniflardan supheli mod adaylarini cikarir
 *  - Java surumu uyumsuzlugunu (calisan vs istenen major) sayisal
 *    olarak dogrular (class file version tablosuyla)
 *  - Sorunlu varligi (ticking entity), shader paketini, dunya adini,
 *    metaspace/native-OOM durumlarini ayiklar
 *  - Bulgulara gore somut, uygulanabilir adimlar listeler
 *
 * Eski hizli tarama (analyzeCrash'in kisa kategori satirlari) aynen
 * korunur; bu motorun ciktisi en ustte gosterilir.
 */
public final class CrashAnalyzer {
    private CrashAnalyzer() {
    }

    /** Framework/ozgun paketler: bunlar "mod" olarak supheli sayilmaz. */
    private static final String[] FRAMEWORK_PACKAGES = {
        "net.minecraft", "com.mojang", "java.", "javax.", "jdk.", "sun.", "com.sun",
        "org.lwjgl", "io.netty", "com.google", "org.apache", "it.unimi",
        "net.minecraftforge", "net.neoforged", "net.fabricmc", "org.spongepowered",
        "org.objectweb", "org.quiltmc", "com.mumfrey", "org.slf4j", "org.junit",
        "oshi.", "com.ibm.icu", "org.joml", "mcp.", "org.yaml"
    };

    /** class file version -> Java major surumu (UnsupportedClassVersionError tani). */
    private static int javaMajorFromClassVersion(int v) {
        if (v >= 61) return 17 + (v - 61);
        if (v >= 52) return 8 + (v - 52);
        if (v >= 51) return 7;
        return 8;
    }

    private static int javaMajor(String version) {
        try {
            String v = version.trim();
            if (v.startsWith("1.")) {
                String[] parts = v.split("[._]");
                return Integer.parseInt(parts[1]);
            }
            return Integer.parseInt(v.split("[._]")[0]);
        } catch (Exception e) {
            return -1;
        }
    }

    /** Tam metin + eski hizli tarama sonucunu derin analize cevirir. */
    public static String enhance(String fullLog, String legacyAnalysis) {
        if (fullLog == null || fullLog.isBlank()) {
            return legacyAnalysis == null ? "" : legacyAnalysis;
        }
        String text = fullLog;
        String lower = text.toLowerCase();

        LinkedHashSet<String> out = new LinkedHashSet<>();
        out.add(L10n.get("crash.deep.header"));

        // ---------- 0) Native (hs_err) sinyal satirlari ----------
        if (lower.contains("problematic frame") || lower.contains("sigsegv") || lower.contains("sigfpe")
            || lower.contains("exception_") || lower.contains("fatal error")) {
            String sig = firstGroup(text, "(?m)^#\\s*((?:EXCEPTION_[\\w_]+|SIG[A-Z]+)[^\\n]*)");
            if (sig != null) {
                out.add(L10n.fmt("crash.deep.exception_line", clip(sig, 120)));
            }
            String frame = firstGroup(text, "(?m)^#\\s*[CVJ]\\s+\\[([^\\]]+)\\]");
            if (frame != null) {
                out.add(L10n.fmt("crash.deep.exception_line", "Problematic frame: " + clip(frame, 90)));
            }
        }

        // ---------- 1) Kok neden ----------
        String rootCause = null;
        String exceptionLine = null;

        Matcher desc = Pattern.compile("(?im)^Description:\\s*(.+)$").matcher(text);
        if (desc.find()) {
            rootCause = desc.group(1).trim();
        }
        // "Caused by" zincirindeki SON satir genelde gercek kok neden.
        Matcher caused = Pattern.compile("(?im)^\\s*Caused by:\\s*(.+)$").matcher(text);
        String lastCaused = null;
        while (caused.find()) {
            lastCaused = caused.group(1).trim();
        }
        if (lastCaused != null) {
            rootCause = lastCaused;
        }
        if (exceptionLine == null) {
            Matcher ex = Pattern.compile("(?m)^[ \\t]*([\\w$.]+(?:Exception|Error))[^\\n]*$").matcher(text);
            if (ex.find()) {
                exceptionLine = ex.group(0).trim();
            }
        }
        if (rootCause == null && exceptionLine != null) {
            rootCause = exceptionLine;
        }
        if (rootCause != null) {
            out.add(L10n.fmt("crash.deep.root_cause", clip(rootCause, 160)));
        }
        if (exceptionLine != null && !exceptionLine.equals(rootCause)) {
            out.add(L10n.fmt("crash.deep.exception_line", clip(exceptionLine, 160)));
        }

        // ---------- 2) Sorumlu mod dosyasi ----------
        String modFile = null;
        Matcher mf = Pattern.compile("(?im)^\\s*(?:Mod File|File):\\s*(.+?\\.jar)\\s*$").matcher(text);
        if (mf.find()) {
            modFile = mf.group(1).trim();
        }
        List<String> suspected = new ArrayList<>();
        if (modFile == null) {
            // Exception satiri + iki satir civarindaki .jar yollari
            List<String> jarsNear = findJarsNearException(text);
            if (!jarsNear.isEmpty()) {
                modFile = jarsNear.get(0);
                for (int i = 1; i < jarsNear.size() && i < 3; i++) {
                    suspected.add(jarsNear.get(i));
                }
            }
        }
        // Mixin config -> mod adayi (orn. sodium.mixins.json -> sodium)
        if (modFile == null) {
            Matcher mx = Pattern.compile("(?i)([\\w\\-]+)\\.mixins\\.json").matcher(text);
            LinkedHashSet<String> mixinMods = new LinkedHashSet<>();
            while (mx.find() && mixinMods.size() < 3) {
                mixinMods.add(mx.group(1));
            }
            suspected.addAll(mixinMods);
        }
        // Cagri zincirindeki framework-disi siniflar -> supheli mod adaylari
        if (suspected.isEmpty()) {
            LinkedHashSet<String> frames = findNonFrameworkFrames(text);
            suspected.addAll(frames);
        }
        final String attributedFile = modFile;
        if (attributedFile != null) {
            suspected.removeIf(s -> s.equalsIgnoreCase(attributedFile));
        }
        if (modFile != null) {
            out.add(L10n.fmt("crash.deep.mod_file", clip(modFile, 100)));
        } else if (!suspected.isEmpty()) {
            out.add(L10n.fmt("crash.deep.suspected_mods", clip(join(suspected, 3), 140)));
        } else {
            out.add(L10n.get("crash.deep.no_mod_attribution"));
        }

        // ---------- 3) Java surumu ----------
        String running = firstGroup(text, "(?im)^Java Version:\\s*([\\d._]+)");
        if (running == null) running = System.getProperty("java.version", "");
        String wanted = null;
        Matcher req = Pattern.compile("(?i)(?:requires|needs)\\s+(?:java\\s+|jdk\\s+|jre\\s+)?(\\d{2,3}(?:\\.\\d+)?)").matcher(text);
        if (req.find()) {
            wanted = req.group(1);
        }
        if (wanted == null) {
            Matcher ucv = Pattern.compile("(?i)class file version (\\d+)").matcher(text);
            if (ucv.find()) {
                wanted = String.valueOf(javaMajorFromClassVersion(Integer.parseInt(ucv.group(1))));
            }
        }
        if (wanted != null) {
            int runM = javaMajor(running), wantM = javaMajor(wanted);
            if (runM > 0 && wantM > 0 && runM != wantM) {
                out.add(L10n.fmt("crash.deep.java_version", running, wanted));
            } else {
                out.add(L10n.fmt("crash.deep.java_ok", running));
            }
        }

        // ---------- 4) Varlik / shader / dunya ----------
        // DIKKAT: "Ticking entity" den sonra gelen satir her zaman varlik
        // tipi degildir (orn. exception satiri) - colon ister veya bilinen
        // Entity's Type alanini ara; yoksa "java" gibi yanlis yakalamalar
        // oluyordu.
        String entity = firstGroup(text, "(?i)ticking entity:\\s*(?:minecraft:)?([\\w_]+)");
        if (entity == null) entity = firstGroup(text, "(?i)entity(?:'s)?\\s+type[\\\"]:\\s*(?:minecraft:)?([\\w_]+)");
        if (entity == null) entity = firstGroup(text, "(?i)entity\\s+type\\s+['\"]?(minecraft:[\\w_]+)");
        if (entity != null) {
            out.add(L10n.fmt("crash.deep.entity", entity));
        }
        String shader = firstGroup(text, "(?i)(?:shader|iris)[^\\n]{0,80}?([\\w\\-.]+\\.(?:zip|vsh|fsh|glsl))");
        if (shader != null) {
            out.add(L10n.fmt("crash.deep.shader_pack", shader));
        }
        String world = firstGroup(text, "(?i)saves[/\\\\]+([\\w\\- ]+)");
        if (world == null) world = firstGroup(text, "(?i)dimension:\\s*(minecraft:[\\w_]+|overworld|the_nether|the_end)");
        if (world != null) {
            out.add(L10n.fmt("crash.deep.world", clip(world, 60)));
        }

        // ---------- 5) Bellek alt turleri ----------
        if (lower.contains("metaspace")) {
            out.add(L10n.get("crash.deep.metaspace"));
        }
        if (lower.contains("could not reserve enough space") || lower.contains("out of memory") && lower.contains("native")) {
            out.add(L10n.get("crash.deep.native_oom"));
        }

        // ---------- 6) Onerilen adimlar ----------
        List<String> fixes = new ArrayList<>();
        if (lower.contains("optifine")) {
            fixes.add(L10n.get("crash.deep.fix_optifine"));
        }
        if (lower.contains("vulkanmod")) {
            fixes.add(L10n.get("crash.deep.fix_vulkan"));
        }
        if (modFile != null) {
            fixes.add(L10n.fmt("crash.deep.fix_mod_delete", clip(modFile, 80)));
        }
        if (lower.contains("duplicate mods") || lower.contains("duplicatemod")) {
            fixes.add(L10n.get("crash.deep.fix_deduplicate"));
        }
        if (lower.contains("opengl") || lower.contains("lwjgl") || lower.contains("nvidia")
            || lower.contains("nvoglv") || lower.contains("amdgpu") || lower.contains("igdumdim")
            || lower.contains("gl_out_of_memory")) {
            fixes.add(L10n.get("crash.deep.fix_driver"));
        }
        if (lower.contains("outofmemoryerror") || lower.contains("java heap space") || lower.contains("gc overhead limit")) {
            fixes.add(L10n.get("crash.deep.fix_ram"));
        }
        if (modFile != null || !suspected.isEmpty() || lower.contains("mixin") || lower.contains("modloading")
            || lower.contains("modresolution") || lower.contains("classnotfound") || lower.contains("noclassdef")) {
            fixes.add(L10n.get("crash.deep.fix_toggle"));
        }
        if (lower.contains("exception loading chunk") || lower.contains("nbtexception")
            || (lower.contains("corrupt") && lower.contains("chunk")) || lower.contains("failed to check lock")) {
            fixes.add(L10n.get("crash.deep.fix_world_backup"));
        }

        if (!fixes.isEmpty()) {
            out.add("");
            out.add(L10n.get("crash.deep.fix_header"));
            int n = 1;
            for (String f : fixes) {
                out.add("  " + (n++) + ". " + f);
            }
        }

        StringBuilder sb = new StringBuilder(String.join("\n", out));
        if (legacyAnalysis != null && !legacyAnalysis.isBlank()) {
            sb.append("\n\n").append(legacyAnalysis.trim());
        }
        return sb.toString().trim();
    }

    /* ---------------- yardimcilar ---------------- */

    private static List<String> findJarsNearException(String text) {
        String[] lines = text.split("\n");
        int exIdx = -1;
        for (int i = 0; i < lines.length && exIdx < 0; i++) {
            String t = lines[i].trim();
            if (t.contains("Caused by") || (t.contains("Exception") && t.contains(":"))
                || t.startsWith("Description:") || t.contains("Mixin apply failed")) {
                exIdx = i;
            }
        }
        if (exIdx < 0) {
            exIdx = 0;
        }
        Pattern jar = Pattern.compile("([A-Za-z0-9_.\\- ]+\\.jar)");
        LinkedHashSet<String> found = new LinkedHashSet<>();
        for (int i = Math.max(0, exIdx - 2); i <= Math.min(lines.length - 1, exIdx + 2); i++) {
            Matcher m = jar.matcher(lines[i]);
            while (m.find() && found.size() < 4) {
                found.add(m.group(1).trim());
            }
        }
        return new ArrayList<>(found);
    }

    private static LinkedHashSet<String> findNonFrameworkFrames(String text) {
        // "at paket.Sinif.method(Sources.java:123)" bicimindeki frame'leri tara
        Pattern at = Pattern.compile("at\\s+([\\w$]+\\.[\\w$.]+)\\.([\\w$<>]+)\\(([^)]+)\\)");
        Matcher m = at.matcher(text);
        LinkedHashSet<String> mods = new LinkedHashSet<>();
        while (m.find() && mods.size() < 3) {
            String pkg = m.group(1);
            boolean framework = false;
            for (String f : FRAMEWORK_PACKAGES) {
                if (pkg.startsWith(f)) {
                    framework = true;
                    break;
                }
            }
            if (!framework) {
                // sinif adindan okunur bir mod etiketi turet
                String label = pkg;
                int idx = label.lastIndexOf('.');
                if (idx > 0) {
                    label = label.substring(0, idx);
                }
                mods.add(label);
            }
        }
        return mods;
    }

    private static String firstGroup(String text, String regex) {
        try {
            Matcher m = Pattern.compile(regex).matcher(text);
            if (m.find()) {
                return m.group(1).trim();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String join(List<String> items, int max) {
        List<String> sub = items.subList(0, Math.min(items.size(), max));
        return String.join(", ", sub);
    }

    private static String clip(String s, int max) {
        String t = s.replaceAll("\\s+", " ").trim();
        return t.length() > max ? t.substring(0, max - 1) + "…" : t;
    }
}

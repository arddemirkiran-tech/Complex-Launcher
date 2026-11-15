/*
 * v50 — native acceleration bridge.
 *
 * The launcher verifies every library, native and mod jar against the SHA-1
 * published by Mojang / Modrinth before it starts the JVM. Done in Java that is
 * a serial read + hash of the whole libraries folder on every launch; the
 * bundled `clsx-native` binary (Rust, see native/clsx-native) hashes the same
 * batch across all cores.
 *
 * Everything degrades gracefully: when the binary is missing, too old, or the
 * process cannot be started, callers fall back to HttpUtil.sha1Matches() and
 * the launcher behaves exactly as before.
 */
package com.lubv.launcher.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class NativeAccel {
    private static final String EXE = isWindows() ? "clsx-native.exe" : "clsx-native";
    private static Boolean cachedAvailable;
    private static String cachedBinary;
    private static String cachedInfo = "";

    private NativeAccel() {
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    /** Directories searched for the native binary, most specific first. */
    private static List<File> candidates() {
        List<File> list = new ArrayList<File>();
        String prop = System.getProperty("clsx.native.dir");
        if (prop != null && !prop.isBlank()) {
            list.add(new File(prop, EXE));
        }
        try {
            list.add(new File(Paths.getLauncherDir(), "native/" + EXE));
            list.add(new File(Paths.getLauncherDir(), EXE));
        } catch (Throwable ignored) {
            // Paths unavailable -> keep going with the remaining candidates
        }
        try {
            list.add(new File(Paths.GAME_DIR, "native/" + EXE));
        } catch (Throwable ignored) {
            // same
        }
        // Running from a jar: <jar dir>/native/<exe>
        try {
            File code = new File(NativeAccel.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File base = code.isFile() ? code.getParentFile() : code;
            if (base != null) {
                list.add(new File(base, "native/" + EXE));
                if (base.getParentFile() != null) {
                    list.add(new File(base.getParentFile(), "native/" + EXE));
                }
            }
        } catch (Throwable ignored) {
            // same
        }
        return list;
    }

    /** The native binary if present and executable, otherwise null. */
    public static synchronized String binary() {
        if (cachedAvailable != null) {
            return cachedAvailable ? cachedBinary : null;
        }
        for (File f : candidates()) {
            if (!f.isFile() || f.length() == 0) {
                continue;
            }
            if (!isWindows() && !f.canExecute()) {
                // Best effort: a jar extracted on Linux usually keeps the bit off.
                //noinspection ResultOfMethodCallIgnored
                f.setExecutable(true);
            }
            cachedBinary = f.getAbsolutePath();
            cachedAvailable = Boolean.TRUE;
            return cachedBinary;
        }
        cachedAvailable = Boolean.FALSE;
        return null;
    }

    public static synchronized boolean available() {
        return binary() != null;
    }

    /** `clsx-native info` output, cached; empty string when unavailable. */
    public static synchronized String info() {
        if (!cachedInfo.isEmpty()) {
            return cachedInfo;
        }
        String bin = binary();
        if (bin == null) {
            return "";
        }
        try {
            Process p = new ProcessBuilder(bin, "info").redirectErrorStream(true).start();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line.trim());
                }
            }
            p.waitFor(5, TimeUnit.SECONDS);
            cachedInfo = sb.toString();
        } catch (Exception ignored) {
            cachedInfo = "";
        }
        return cachedInfo;
    }

    /**
     * Hashes a batch of files in one native pass.
     *
     * @return absolute path -> lowercase SHA-1; unreadable files are absent.
     *         An empty map means "native path unusable, do it in Java".
     */
    public static Map<String, String> sha1Many(List<File> files) {
        Map<String, String> result = new HashMap<String, String>();
        String bin = binary();
        if (bin == null || files == null || files.isEmpty()) {
            return result;
        }
        File listFile = null;
        Process process = null;
        try {
            listFile = File.createTempFile("clsx-hash-", ".txt");
            StringBuilder sb = new StringBuilder(files.size() * 64);
            for (File f : files) {
                if (f != null && f.isFile()) {
                    sb.append(f.getAbsolutePath()).append('\n');
                }
            }
            if (sb.length() == 0) {
                return result;
            }
            try (Writer w = new OutputStreamWriter(Files.newOutputStream(listFile.toPath()), StandardCharsets.UTF_8)) {
                w.write(sb.toString());
            }
            process = new ProcessBuilder(bin, "hash", listFile.getAbsolutePath())
                .redirectErrorStream(false)
                .start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    int tab = line.indexOf('\t');
                    if (tab <= 0) {
                        continue;
                    }
                    String digest = line.substring(0, tab).trim();
                    String path = line.substring(tab + 1);
                    if (digest.isEmpty() || "-".equals(digest) || digest.length() != 40) {
                        continue;
                    }
                    result.put(path, digest.toLowerCase());
                }
            }
            if (!process.waitFor(180, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return new HashMap<String, String>();
            }
            if (process.exitValue() != 0) {
                return new HashMap<String, String>();
            }
        } catch (Exception ignored) {
            if (process != null) {
                process.destroyForcibly();
            }
            return new HashMap<String, String>();
        } finally {
            if (listFile != null) {
                //noinspection ResultOfMethodCallIgnored
                listFile.delete();
            }
        }
        return result;
    }
}

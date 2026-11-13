/*
 * v50 — Vulcan Mode capability probe.
 *
 * Answers a single, concrete question: can this machine actually run a Vulkan
 * renderer? It does not trust a GPU name; it checks for the Vulkan loader and
 * for at least one installed ICD (the driver-side Vulkan implementation), which
 * is exactly what a renderer needs to initialise.
 *
 * Windows: %WINDIR%\System32\vulkan-1.dll + HKLM\SOFTWARE\Khronos\Vulkan\Drivers
 * Linux:   libvulkan.so.1 on the loader path + vulkaninfo --summary when present
 *
 * All probes are best effort and time-boxed: a failure only means "no Vulkan",
 * never a launcher error.
 */
package com.lubv.launcher.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class VulkanProbe {
    /** Result of a capability check. */
    public static final class Info {
        public boolean available;
        public boolean loaderPresent;
        public int icdCount;
        public String gpu = "";
        public String driver = "";
        public String detail = "";

        public String summary() {
            if (!available) {
                return detail.isEmpty() ? "no Vulkan device" : detail;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(gpu.isEmpty() ? "Vulkan device" : gpu);
            if (!driver.isEmpty()) {
                sb.append(" (driver ").append(driver).append(')');
            }
            sb.append(", ").append(icdCount).append(" ICD");
            return sb.toString();
        }
    }

    private static Info cached;

    private VulkanProbe() {
    }

    public static synchronized Info detect() {
        if (cached != null) {
            return cached;
        }
        cached = isWindows() ? detectWindows() : detectUnix();
        return cached;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static Info detectWindows() {
        Info info = new Info();
        File loader = new File(new File(System.getenv().getOrDefault("WINDIR", "C:\\Windows"), "System32"), "vulkan-1.dll");
        if (!loader.isFile()) {
            // 32-bit only systems keep it in SysWOW64
            File alt = new File(new File(System.getenv().getOrDefault("WINDIR", "C:\\Windows"), "SysWOW64"), "vulkan-1.dll");
            loader = alt.isFile() ? alt : loader;
        }
        info.loaderPresent = loader.isFile();
        info.icdCount = countIcdsWindows();
        info.available = info.loaderPresent && info.icdCount > 0;
        if (!info.loaderPresent) {
            info.detail = "vulkan-1.dll not found (no Vulkan runtime installed)";
        } else if (info.icdCount == 0) {
            info.detail = "Vulkan loader present but no ICD installed (GPU driver has no Vulkan)";
        }
        if (info.available) {
            String gpu = run("powershell", "-NoProfile", "-NonInteractive", "-Command",
                "(Get-CimInstance Win32_VideoController | Where-Object { $_.Name -notmatch 'Basic Display|Remote' } | Select-Object -First 1).Name");
            String drv = run("powershell", "-NoProfile", "-NonInteractive", "-Command",
                "(Get-CimInstance Win32_VideoController | Where-Object { $_.Name -notmatch 'Basic Display|Remote' } | Select-Object -First 1).DriverVersion");
            info.gpu = clean(gpu);
            info.driver = clean(drv);
        }
        return info;
    }

    private static int countIcdsWindows() {
        String out = run("reg", "query", "HKLM\\SOFTWARE\\Khronos\\Vulkan\\Drivers");
        int count = 0;
        if (out != null) {
            for (String line : out.split("\\R")) {
                String t = line.trim();
                // ICD entries are DLL paths with a REG_ type, e.g.
                // C:\Windows\system32\nvoglv64.dll    REG_DWORD    0x0
                // The DLL name never contains "vulkan", so key off the value
                // type and the .dll suffix instead.
                if (t.toUpperCase().contains("REG_") && t.toLowerCase().contains(".dll")) {
                    count++;
                }
            }
        }
        // Vulkan layers also live under Khronos\Vulkan\ImplicitLayers; they do
        // not count as devices, so the Drivers key above is authoritative.
        if (count > 0) {
            return count;
        }
        // Fallback: the per-vendor runtime DLL next to the loader also implies an ICD.
        File system32 = new File(System.getenv().getOrDefault("WINDIR", "C:\\Windows"), "System32");
        String[] vendorDlls = {"nvoglv64.dll", "amdvlk64.dll", "amdvlk32.dll", "igvk64.dll", "igvk32.dll", "vulkan-1.dll"};
        for (String dll : vendorDlls) {
            if (new File(system32, dll).isFile() && !"vulkan-1.dll".equals(dll)) {
                count++;
            }
        }
        return count;
    }

    private static Info detectUnix() {
        Info info = new Info();
        boolean loader = new File("/usr/lib/x86_64-linux-gnu/libvulkan.so.1").isFile()
            || new File("/usr/lib/libvulkan.so.1").isFile()
            || new File("/usr/lib64/libvulkan.so.1").isFile();
        String summary = run("vulkaninfo", "--summary");
        info.loaderPresent = loader;
        if (summary != null && summary.toLowerCase().contains("deviceproperties")) {
            info.icdCount = Math.max(1, countOccurrences(summary.toLowerCase(), "deviceid"));
            info.gpu = firstMatch(summary, "deviceName");
            info.driver = firstMatch(summary, "driverName");
            info.available = true;
        } else {
            info.available = false;
            info.detail = loader ? "Vulkan loader present but no usable device (install a Vulkan driver)" : "libvulkan.so.1 not found";
        }
        return info;
    }

    private static int countOccurrences(String haystack, String needle) {
        int n = 0;
        int i = 0;
        while ((i = haystack.indexOf(needle, i)) >= 0) {
            n++;
            i += needle.length();
        }
        return n;
    }

    private static String firstMatch(String text, String key) {
        for (String line : text.split("\\R")) {
            String t = line.trim();
            if (t.toLowerCase().startsWith(key.toLowerCase())) {
                int eq = t.indexOf('=');
                if (eq > 0) {
                    return t.substring(eq + 1).trim();
                }
            }
        }
        return "";
    }

    private static String clean(String s) {
        if (s == null) {
            return "";
        }
        String t = s.replace("\r", " ").trim();
        if (t.isEmpty() || t.toLowerCase().contains("not recognized") || t.startsWith("Exception")) {
            return "";
        }
        return t.length() > 120 ? t.substring(0, 120) : t;
    }

    /** Runs a short, non-interactive command and returns its stdout (or null). */
    private static String run(String... command) {
        try {
            List<String> cmd = new ArrayList<String>();
            for (String c : command) {
                cmd.add(c);
            }
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line).append('\n');
                    if (sb.length() > 8192) {
                        break;
                    }
                }
            }
            if (!p.waitFor(6, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                return null;
            }
            return sb.toString();
        } catch (Exception ignored) {
            return null;
        }
    }
}

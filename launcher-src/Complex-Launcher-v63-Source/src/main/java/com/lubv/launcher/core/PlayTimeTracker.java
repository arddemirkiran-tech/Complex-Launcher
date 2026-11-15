/*
 * Oynama suresi takibi.
 *
 * ONEMLI: Eskiden sure SADECE oyun kapaninca (Process.waitFor geri
 * donunce) yaziliyordu. Kullanici launcher'i oyun acikken kapatirsa
 * (cok yaygin: oyunu baslatip pencereyi kapatiyor) sure hicbir yere
 * kaydedilmiyor ve instance kartinda sonsuza kadar "Henuz oynanmadi"
 * yaziyordu.
 *
 * Cozum: oyun baslarken instance klasorune kucuk bir oturum isaretcisi
 * yazilir. Sure, isaretcideki baslangic zamanindan hesaplanir; boylece
 * launcher oyun sirasinda kapansa bile bir sonraki acilista gecen sure
 * instance'a eklenir ve isaretci silinir.
 */
package com.lubv.launcher.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public final class PlayTimeTracker {
    /** Instance klasoru icindeki oturum isaretcisi. */
    private static final String MARKER = ".play_session";

    private PlayTimeTracker() {
    }

    private static File instanceDir(String name) {
        return new File(Paths.INSTANCES_DIR, name);
    }

    private static File marker(String name) {
        return new File(instanceDir(name), MARKER);
    }

    private static long startedAt(String name) {
        if (name == null || name.isBlank()) {
            return -1L;
        }
        File f = marker(name);
        if (!f.isFile()) {
            return -1L;
        }
        try (FileInputStream in = new FileInputStream(f)) {
            Properties p = new Properties();
            p.load(in);
            return Long.parseLong(p.getProperty("startedAt", "0"));
        }
        catch (Exception ignored) {
            return -1L;
        }
    }

    /** Oyun baslarken cagrilir: oturum baslangicini diske yazar. */
    public static synchronized void begin(String instanceName) {
        if (instanceName == null || instanceName.isBlank()) {
            return;
        }
        try {
            instanceDir(instanceName).mkdirs();
            Properties p = new Properties();
            p.setProperty("startedAt", String.valueOf(System.currentTimeMillis()));
            try (FileOutputStream out = new FileOutputStream(marker(instanceName))) {
                p.store(out, "complex-launcher play session");
            }
        }
        catch (Exception ignored) {
            // takip basarisiz olsa da oyun baslatmayi engellemeyiz
        }
    }

    /** Isaretci varsa gecen saniye, yoksa -1. */
    public static synchronized long elapsedSeconds(String instanceName) {
        long started = startedAt(instanceName);
        if (started <= 0L) {
            return -1L;
        }
        return Math.max(0L, (System.currentTimeMillis() - started) / 1000L);
    }

    /**
     * Oturumu kapatir ve sureyi instance'a ekler. {@code reportedSeconds}
     * oyun cikisinda olculen suredir; isaretciden hesaplanan sure daha
     * buyukse o kullanilir (launcher'in kacirdigi kisim boylece kurtarilir).
     * Eklenen saniye doner.
     */
    public static synchronized long end(String instanceName, long reportedSeconds) {
        if (instanceName == null || instanceName.isBlank()) {
            return 0L;
        }
        long fromMarker = elapsedSeconds(instanceName);
        File f = marker(instanceName);
        boolean hadMarker = f.isFile();
        if (hadMarker && !f.delete()) {
            // silinemezse bile sureyi ekleyelim; isaretci bir sonraki
            // acilista sifirlanir (sure iki kez sayilmaz, cunku end()
            // isaretciyi okuduktan sonra siler).
            f.deleteOnExit();
        }
        long added = Math.max(reportedSeconds, fromMarker);
        if (added < 0L) {
            added = 0L;
        }
        if (!hadMarker && added <= 0L) {
            return 0L;
        }
        Instance inst = Instance.load(instanceName);
        inst.totalPlaySeconds += added;
        inst.lastPlayedMs = System.currentTimeMillis();
        inst.save();
        return added;
    }

    /**
     * Launcher acilirken cagrilir: onceki oturum kapanmadan (launcher
     * kapatildi/coktu) kalmis isaretcileri bulup surelerini ekler.
     */
    public static synchronized void recoverAll() {
        File[] dirs = Paths.INSTANCES_DIR.listFiles(File::isDirectory);
        if (dirs == null) {
            return;
        }
        for (File dir : dirs) {
            String name = dir.getName();
            if (marker(name).isFile()) {
                end(name, -1L);
            }
        }
    }

    /**
     * Launcher kapanirken cagrilir: oyun hala acik olsa bile o ana kadar
     * gecen sureyi kaydeder, isaretciyi siler.
     */
    public static synchronized void finishRunningSessions() {
        recoverAll();
    }
}

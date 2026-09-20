/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.mods;

import com.lubv.launcher.mods.ModManager;
import com.lubv.launcher.mods.ModrinthApi;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class AutoModInstaller {
    private static final String EMBEDDIUM_PROJECT_ID = "sk9rgfiA";
    private static final String OCULUS_PROJECT_ID = "GchcoXML";
    private static final String SODIUM_PROJECT_ID = "AANobbMI";
    private static final String IRIS_PROJECT_ID = "YL57xq9U";
    private static final String JEI_PROJECT_ID = "nFNmxrk";

    public static void autoInstall(File modsDir, String loader, String mcVersion, Consumer<String> log) {
        log.accept("=== AUTO-INSTALL BASLADI: loader=" + loader + " mcVersion=" + mcVersion + " ===");
        if (modsDir == null) {
            log.accept("HATA: modsDir=null");
            return;
        }
        if (loader == null) {
            log.accept("HATA: loader=null");
            return;
        }
        modsDir.mkdirs();
        log.accept("modsDir=" + modsDir.getAbsolutePath() + " exists=" + modsDir.exists());
        if ("Forge".equalsIgnoreCase(loader)) {
            AutoModInstaller.installOne(modsDir, EMBEDDIUM_PROJECT_ID, "Embeddium", "forge", mcVersion, log);
            AutoModInstaller.installOne(modsDir, OCULUS_PROJECT_ID, "Oculus", "forge", mcVersion, log);
            AutoModInstaller.installOne(modsDir, JEI_PROJECT_ID, "JEI", "forge", mcVersion, log);
        } else if ("Fabric".equalsIgnoreCase(loader)) {
            AutoModInstaller.installOne(modsDir, SODIUM_PROJECT_ID, "Sodium", "fabric", mcVersion, log);
            AutoModInstaller.installOne(modsDir, IRIS_PROJECT_ID, "Iris", "fabric", mcVersion, log);
            AutoModInstaller.installOne(modsDir, JEI_PROJECT_ID, "JEI", "fabric", mcVersion, log);
        } else if ("NeoForge".equalsIgnoreCase(loader)) {
            AutoModInstaller.installOne(modsDir, SODIUM_PROJECT_ID, "Sodium", "neoforge", mcVersion, log);
            AutoModInstaller.installOne(modsDir, IRIS_PROJECT_ID, "Iris", "neoforge", mcVersion, log);
            AutoModInstaller.installOne(modsDir, JEI_PROJECT_ID, "JEI", "neoforge", mcVersion, log);
        } else {
            log.accept("Bilinmeyen loader: " + loader + " - mod kurulumu atlandi");
        }
        log.accept("=== AUTO-INSTALL TAMAMLANDI ===");
    }

    private static void installOne(File modsDir, String projectId, String name, String loader, String mcVersion, Consumer<String> log) {
        log.accept("[" + name + "] Basliyor...");
        try {
            List<ModManager.InstalledMod> existing = ModManager.loadRegistry(modsDir);
            log.accept("[" + name + "] Registry okundu, " + existing.size() + " mod kurulu");
            for (ModManager.InstalledMod m : existing) {
                if (!projectId.equals(m.projectId)) continue;
                log.accept("[" + name + "] Zaten kurulu: " + m.fileName + " (" + m.versionNumber + ") - ATLADI");
                return;
            }
        }
        catch (Exception e) {
            log.accept("[" + name + "] Registry okuma hatasi: " + e.getMessage());
        }
        try {
            log.accept("[" + name + "] Modrinth API'sine istek gonderiliyor...");
            List<ModrinthApi.ModVersion> versions = ModrinthApi.getVersions(projectId, loader, mcVersion);
            log.accept("[" + name + "] " + versions.size() + " versiyon bulundu");
            if (versions.isEmpty()) {
                log.accept("[" + name + "] UYARI: Uyumlu versiyon bulunamadi! (loader=" + loader + ", MC=" + mcVersion + ")");
                return;
            }
            ModrinthApi.ModVersion best = ModrinthApi.pickBestVersion(versions);
            if (best == null) {
                log.accept("[" + name + "] UYARI: pickBestVersion null dondu!");
                return;
            }
            log.accept("[" + name + "] Secilen versiyon: " + best.versionNumber + " (" + best.fileName + ")");
            log.accept("[" + name + "] Indiriliyor: " + best.downloadUrl);
            boolean ok = ModManager.installProject(modsDir, projectId, loader, mcVersion);
            log.accept("[" + name + "] installProject sonucu: " + ok);
            File downloaded = new File(modsDir, best.fileName);
            log.accept("[" + name + "] Dosya mevcut mu: " + downloaded.exists() + " (" + downloaded.getAbsolutePath() + ")");
        }
        catch (Exception e) {
            log.accept("[" + name + "] HATA: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}


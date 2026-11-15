/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.core;

import com.lubv.launcher.core.Instance;
import com.lubv.launcher.core.Paths;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InstanceManager {
    private InstanceManager() {
    }

    public static List<String> listNames() {
        File dir = Paths.INSTANCES_DIR;
        ArrayList<String> names = new ArrayList<String>();
        File[] children = dir.listFiles(File::isDirectory);
        if (children != null) {
            for (File c : children) {
                if (c.getName().startsWith(".")) continue;
                names.add(c.getName());
            }
        }
        Collections.sort(names);
        return names;
    }

    public static Instance create(String name) {
        Instance inst = Instance.load(name);
        inst.dir().mkdirs();
        inst.modsDir().mkdirs();
        inst.save();
        return inst;
    }

    public static void delete(String name) {
        InstanceManager.deleteRecursive(new File(Paths.INSTANCES_DIR, name));
    }

    private static void deleteRecursive(File f) {
        File[] children = f.listFiles();
        if (children != null) {
            for (File c : children) {
                InstanceManager.deleteRecursive(c);
            }
        }
        f.delete();
    }

    public static String ensureDefault(String activeInstance) {
        List<String> names = InstanceManager.listNames();
        if (names.isEmpty()) {
            InstanceManager.create("default");
            names = InstanceManager.listNames();
        }
        if (activeInstance == null || activeInstance.isEmpty() || !names.contains(activeInstance)) {
            return names.get(0);
        }
        return activeInstance;
    }

    public static void migrateLegacyMods(String targetInstance) {
        File legacy = Paths.MODS_DIR;
        if (!legacy.isDirectory()) {
            return;
        }
        File[] jars = legacy.listFiles((d, n) -> n.toLowerCase().endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            return;
        }
        File target = new File(new File(Paths.INSTANCES_DIR, targetInstance), "mods");
        target.mkdirs();
        for (File j : jars) {
            File dest = new File(target, j.getName());
            if (dest.exists()) continue;
            j.renameTo(dest);
        }
    }
}


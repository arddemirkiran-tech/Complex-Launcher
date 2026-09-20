/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.core;

import com.lubv.launcher.core.Paths;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CrashLog {
    private static final File FILE = new File(Paths.GAME_DIR, "launcher_errors.log");

    private CrashLog() {
    }

    public static void log(Throwable t) {
        try {
            Paths.GAME_DIR.mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE, true));){
                pw.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]");
                t.printStackTrace(pw);
                pw.println();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}


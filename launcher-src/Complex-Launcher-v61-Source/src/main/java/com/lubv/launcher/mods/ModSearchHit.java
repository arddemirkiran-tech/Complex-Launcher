/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.mods;

import com.lubv.launcher.mods.CurseForgeApi;
import com.lubv.launcher.mods.ModrinthApi;

public class ModSearchHit {
    public String title;
    public String description;
    public String iconUrl;
    public long downloads;
    public boolean curseforge;
    public ModrinthApi.ModResult modrinth;
    public CurseForgeApi.ModResult curseforgeMod;

    public static ModSearchHit from(ModrinthApi.ModResult r) {
        ModSearchHit h = new ModSearchHit();
        h.title = r.title;
        h.description = r.description;
        h.iconUrl = r.iconUrl;
        h.downloads = r.downloads;
        h.curseforge = false;
        h.modrinth = r;
        return h;
    }

    public static ModSearchHit from(CurseForgeApi.ModResult r) {
        ModSearchHit h = new ModSearchHit();
        h.title = r.name;
        h.description = r.summary;
        h.iconUrl = r.logoUrl;
        h.downloads = r.downloadCount;
        h.curseforge = true;
        h.curseforgeMod = r;
        return h;
    }
}


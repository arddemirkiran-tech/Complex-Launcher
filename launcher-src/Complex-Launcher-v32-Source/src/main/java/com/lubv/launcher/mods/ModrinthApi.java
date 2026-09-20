/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.mods;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lubv.launcher.core.HttpUtil;
import java.io.IOException;
import java.lang.invoke.CallSite;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ModrinthApi {
    private static final String BASE = "https://api.modrinth.com/v2";

    public static List<ModResult> search(String query, String loader, String mcVersion) throws IOException {
        StringBuilder facets = new StringBuilder("[");
        ArrayList<String> facetParts = new ArrayList<String>();
        facetParts.add("[\"project_type:mod\"]");
        if (loader != null && !loader.equalsIgnoreCase("vanilla")) {
            facetParts.add("[\"categories:" + loader.toLowerCase() + "\"]");
        }
        // V29.7.1: cop veya "latest" degeri versions facet'ine girerse Modrinth
        // 0 sonuc dondurur -> "mod paneli calismiyor" gibi gorunur. Filtre
        // SADECE gercek bir MC surumunde eklenir.
        if (ModrinthApi.looksLikeMcVersion(mcVersion) && !"latest".equalsIgnoreCase(mcVersion)) {
            facetParts.add("[\"versions:" + mcVersion.trim() + "\"]");
        }
        facets.append(String.join((CharSequence)",", facetParts)).append("]");
        String url = "https://api.modrinth.com/v2/search?query=" + ModrinthApi.enc(query) + "&limit=30&facets=" + ModrinthApi.enc(facets.toString());
        JsonObject resp = ModrinthApi.getJsonRetryUrl(url);
        JsonArray hits = resp.getAsJsonArray("hits");
        ArrayList<ModResult> results = new ArrayList<ModResult>();
        for (int i = 0; i < hits.size(); ++i) {
            JsonObject hit = hits.get(i).getAsJsonObject();
            ModResult r = new ModResult();
            r.id = hit.get("project_id").getAsString();
            r.slug = hit.get("slug").getAsString();
            r.title = hit.get("title").getAsString();
            r.description = hit.has("description") ? hit.get("description").getAsString() : "";
            r.iconUrl = hit.has("icon_url") && !hit.get("icon_url").isJsonNull() ? hit.get("icon_url").getAsString() : null;
            int n = r.downloads = hit.has("downloads") ? hit.get("downloads").getAsInt() : 0;
            if (hit.has("categories")) {
                JsonArray cats = hit.getAsJsonArray("categories");
                for (int j = 0; j < cats.size(); ++j) {
                    r.categories.add(cats.get(j).getAsString());
                }
            }
            results.add(r);
        }
        return results;
    }

    public static List<ModResult> searchPlugins(String query, String mcVersion) throws IOException {
        StringBuilder facets = new StringBuilder("[");
        ArrayList<String> facetParts = new ArrayList<String>();
        facetParts.add("[\"project_type:plugin\"]");
        // V29.7.1: cop veya "latest" degeri versions facet'ine girerse Modrinth
        // 0 sonuc dondurur -> "mod paneli calismiyor" gibi gorunur. Filtre
        // SADECE gercek bir MC surumunde eklenir.
        if (ModrinthApi.looksLikeMcVersion(mcVersion) && !"latest".equalsIgnoreCase(mcVersion)) {
            facetParts.add("[\"versions:" + mcVersion.trim() + "\"]");
        }
        facets.append(String.join((CharSequence)",", facetParts)).append("]");
        String url = "https://api.modrinth.com/v2/search?query=" + ModrinthApi.enc(query) + "&limit=30&facets=" + ModrinthApi.enc(facets.toString());
        JsonObject resp = ModrinthApi.getJsonRetryUrl(url);
        JsonArray hits = resp.getAsJsonArray("hits");
        ArrayList<ModResult> results = new ArrayList<ModResult>();
        for (int i = 0; i < hits.size(); ++i) {
            JsonObject hit = hits.get(i).getAsJsonObject();
            ModResult r = new ModResult();
            r.id = hit.get("project_id").getAsString();
            r.slug = hit.get("slug").getAsString();
            r.title = hit.get("title").getAsString();
            r.description = hit.has("description") ? hit.get("description").getAsString() : "";
            r.iconUrl = hit.has("icon_url") && !hit.get("icon_url").isJsonNull() ? hit.get("icon_url").getAsString() : null;
            int n = r.downloads = hit.has("downloads") ? hit.get("downloads").getAsInt() : 0;
            if (hit.has("categories")) {
                JsonArray cats = hit.getAsJsonArray("categories");
                for (int j = 0; j < cats.size(); ++j) {
                    r.categories.add(cats.get(j).getAsString());
                }
            }
            results.add(r);
        }
        return results;
    }

    public static List<ModResult> searchShaders(String query) throws IOException {
        String url = "https://api.modrinth.com/v2/search?query=" + ModrinthApi.enc(query) + "&limit=30&facets=" + ModrinthApi.enc("[[\"project_type:shader\"]]");
        JsonObject resp = ModrinthApi.getJsonRetryUrl(url);
        JsonArray hits = resp.getAsJsonArray("hits");
        ArrayList<ModResult> results = new ArrayList<ModResult>();
        for (int i = 0; i < hits.size(); ++i) {
            JsonObject hit = hits.get(i).getAsJsonObject();
            ModResult r = new ModResult();
            r.id = hit.get("project_id").getAsString();
            r.slug = hit.get("slug").getAsString();
            r.title = hit.get("title").getAsString();
            r.description = hit.has("description") ? hit.get("description").getAsString() : "";
            r.iconUrl = hit.has("icon_url") && !hit.get("icon_url").isJsonNull() ? hit.get("icon_url").getAsString() : null;
            r.downloads = hit.has("downloads") ? hit.get("downloads").getAsInt() : 0;
            results.add(r);
        }
        return results;
    }

    public static List<ModResult> searchModpacks(String query) throws IOException {
        String url = "https://api.modrinth.com/v2/search?query=" + ModrinthApi.enc(query) + "&limit=30&facets=" + ModrinthApi.enc("[[\"project_type:modpack\"]]");
        JsonObject resp = ModrinthApi.getJsonRetryUrl(url);
        JsonArray hits = resp.getAsJsonArray("hits");
        ArrayList<ModResult> results = new ArrayList<ModResult>();
        for (int i = 0; i < hits.size(); ++i) {
            JsonObject hit = hits.get(i).getAsJsonObject();
            ModResult r = new ModResult();
            r.id = hit.get("project_id").getAsString();
            r.slug = hit.get("slug").getAsString();
            r.title = hit.get("title").getAsString();
            r.description = hit.has("description") ? hit.get("description").getAsString() : "";
            r.iconUrl = hit.has("icon_url") && !hit.get("icon_url").isJsonNull() ? hit.get("icon_url").getAsString() : null;
            int n = r.downloads = hit.has("downloads") ? hit.get("downloads").getAsInt() : 0;
            if (hit.has("categories")) {
                JsonArray cats = hit.getAsJsonArray("categories");
                for (int j = 0; j < cats.size(); ++j) {
                    r.categories.add(cats.get(j).getAsString());
                }
            }
            results.add(r);
        }
        return results;
    }

    public static List<ModResult> searchResourcepacks(String query) throws IOException {
        String url = "https://api.modrinth.com/v2/search?query=" + ModrinthApi.enc(query) + "&limit=30&facets=" + ModrinthApi.enc("[[\"project_type:resourcepack\"]]");
        JsonObject resp = ModrinthApi.getJsonRetryUrl(url);
        JsonArray hits = resp.getAsJsonArray("hits");
        ArrayList<ModResult> results = new ArrayList<ModResult>();
        for (int i = 0; i < hits.size(); ++i) {
            JsonObject hit = hits.get(i).getAsJsonObject();
            ModResult r = new ModResult();
            r.id = hit.get("project_id").getAsString();
            r.slug = hit.get("slug").getAsString();
            r.title = hit.get("title").getAsString();
            r.description = hit.has("description") ? hit.get("description").getAsString() : "";
            r.iconUrl = hit.has("icon_url") && !hit.get("icon_url").isJsonNull() ? hit.get("icon_url").getAsString() : null;
            r.downloads = hit.has("downloads") ? hit.get("downloads").getAsInt() : 0;
            results.add(r);
        }
        return results;
    }

    /**
     * Version list with transient-fault tolerance: the URL built here is
     * fetched via getTextWithRetry so a single Modrinth hiccup doesn't make
     * a mods-panel install die with "uyumlu sürüm bulunamadı".
     *
     * projectId MAY be a project ID or a slug — Modrinth accepts both in the
     * /project/{id|slug}/version endpoint. If it's null/blank (e.g. a search
     * hit whose parser forgot to read project_id) we still succeed using the
     * slug instead of hitting /project/null/version -> 404.
     */
    public static List<ModVersion> getVersionsRetry(String projectId, String loader, String mcVersion) throws IOException {
        String pid = projectId != null ? projectId.trim() : "";
        if (pid.isEmpty() || "null".equalsIgnoreCase(pid)) {
            throw new IOException("Mod kimligi (project id) alinamadi - arama sonucu bozuk, tekrar arayin");
        }
        String url = "https://api.modrinth.com/v2/project/" + ModrinthApi.enc(pid) + "/version";
        if (loader != null || mcVersion != null) {
            ArrayList<String> qp = new ArrayList<String>();
            if (loader != null && !loader.equalsIgnoreCase("vanilla")) {
                qp.add("loaders=" + ModrinthApi.enc("[\"" + loader.toLowerCase() + "\"]"));
            }
            if (mcVersion != null && !mcVersion.isBlank()) {
                qp.add("game_versions=" + ModrinthApi.enc("[\"" + mcVersion + "\"]"));
            }
            if (!qp.isEmpty()) {
                url = url + "?" + String.join((CharSequence)"&", qp);
            }
        }
        JsonArray arr = JsonParser.parseString(HttpUtil.getTextWithRetry(url, 3)).getAsJsonArray();
        ArrayList<ModVersion> versions = new ArrayList<ModVersion>();
        for (int i = 0; i < arr.size(); ++i) {
            ModVersion mv = ModrinthApi.parseVersion(arr.get(i).getAsJsonObject());
            if (mv == null) continue;
            versions.add(mv);
        }
        return versions;
    }

    /**
     * V32: project_id null gelen dependency'lerin projesini version_id'den
     * cozer (version kaydinin project_id alanindan). Null donerse cagiran
     * katman 3 fallback devreye girer - dep artik sessizce atlanmaz.
     */
    private static final java.util.Map<String, String> projectIdByVersion = new java.util.concurrent.ConcurrentHashMap<String, String>();

    static String resolveProjectIdFromVersion(String versionId) {
        if (versionId == null || versionId.isBlank()) return null;
        String cached = ModrinthApi.projectIdByVersion.get(versionId);
        if (cached != null) return cached;
        try {
            String body = HttpUtil.getTextWithRetry("https://api.modrinth.com/v2/version/" + versionId, 3);
            if (body == null) return null;
            JsonObject v = JsonParser.parseString(body).getAsJsonObject();
            if (!v.has("project_id") || v.get("project_id").isJsonNull()) return null;
            String pid = v.get("project_id").getAsString();
            ModrinthApi.projectIdByVersion.put(versionId, pid);
            return pid;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static ModVersion getVersion(String versionId) throws IOException {
        JsonObject v = JsonParser.parseString(HttpUtil.getTextWithRetry("https://api.modrinth.com/v2/version/" + versionId, 3)).getAsJsonObject();
        return ModrinthApi.parseVersion(v);
    }

    private static ModVersion parseVersion(JsonObject v) {
        ModVersion mv = new ModVersion();
        // V32 (canli testte yakalandi): listedeki TEK bir bozuk/eksik version
        // objesi burada NPE atiyor ve BUTUN surum listesini dusuruyordu ->
        // mod hic kurulamiyordu. Zorunlu alanlar dogrulanir; kismi bozuk
        // kayitlar atlanir ama liste hayatta kalir.
        if (v == null || !v.has("id") || v.get("id").isJsonNull()
                || !v.has("version_number") || v.get("version_number").isJsonNull()
                || !v.has("files") || !v.getAsJsonArray("files").iterator().hasNext()) {
            return null;
        }
        mv.id = v.get("id").getAsString();
        mv.versionNumber = v.get("version_number").getAsString();
        mv.versionType = v.has("version_type") && !v.get("version_type").isJsonNull() ? v.get("version_type").getAsString() : "release";
        JsonArray files = v.getAsJsonArray("files");
        if (files.size() == 0) {
            return null;
        }
        JsonObject primaryFile = files.get(0).getAsJsonObject();
        for (int f = 0; f < files.size(); ++f) {
            JsonObject file = files.get(f).getAsJsonObject();
            if (!file.has("primary") || !file.get("primary").getAsBoolean()) continue;
            primaryFile = file;
            break;
        }
        mv.fileName = primaryFile.has("filename") && !primaryFile.get("filename").isJsonNull() ? primaryFile.get("filename").getAsString() : null;
        mv.downloadUrl = primaryFile.has("url") && !primaryFile.get("url").isJsonNull() ? primaryFile.get("url").getAsString() : null;
        mv.sha1 = primaryFile.has("hashes") && primaryFile.getAsJsonObject("hashes").has("sha1") ? primaryFile.getAsJsonObject("hashes").get("sha1").getAsString() : null;
        if (mv.fileName == null || mv.downloadUrl == null) {
            return null; // indirilemez kayit - listeyi dusurmesin
        }
        // Yayin tarihi (epoch ms): pickBestVersion ayni oncelikteki (release vs
        // release) surumlerden EN YENISINI secmek icin kullanilir - API
        // donus sirasina guvenmek yerine gercek tarihe bakariz.
        if (v.has("date_published") && !v.get("date_published").isJsonNull()) {
            try {
                mv.datePublished = java.time.Instant.parse(v.get("date_published").getAsString()).toEpochMilli();
            }
            catch (Exception ignored) {
                mv.datePublished = 0L;
            }
        }
        JsonArray gv = v.getAsJsonArray("game_versions");
        for (int j = 0; j < gv.size(); ++j) {
            mv.gameVersions.add(gv.get(j).getAsString());
        }
        JsonArray ld = v.getAsJsonArray("loaders");
        for (int j = 0; j < ld.size(); ++j) {
            mv.loaders.add(ld.get(j).getAsString());
        }
        if (v.has("dependencies")) {
            JsonArray deps = v.getAsJsonArray("dependencies");
            for (int d = 0; d < deps.size(); ++d) {
                JsonObject dep = deps.get(d).getAsJsonObject();
                Dependency dd = new Dependency();
                dd.type = dep.has("dependency_type") && !dep.get("dependency_type").isJsonNull() ? dep.get("dependency_type").getAsString() : "";
                dd.projectId = dep.has("project_id") && !dep.get("project_id").isJsonNull() ? dep.get("project_id").getAsString() : null;
                dd.versionId = dep.has("version_id") && !dep.get("version_id").isJsonNull() ? dep.get("version_id").getAsString() : null;
                // V32: project_id null + version_id dolu dep Modrinth tarafindan
                // boyle dondurulur ve eskiden HIC islenmeden atlanirdi - dep
                // kurulmuyor, oyun icinde mod yuklenmiyordu. version_id'den
                // cagirip projeyi geri cozebiliriz.
                if (dd.projectId == null && dd.versionId != null) {
                    dd.projectId = ModrinthApi.resolveProjectIdFromVersion(dd.versionId);
                }
                mv.dependencies.add(dd);
            }
        }
        return mv;
    }

    /**
     * En iyi surumu secer: release > beta > alpha, ayni oncelikte olanlardan
     * EN YENI yayin tarihli kazanir. Eski kod donus sirasindaki ilkine
     * takiliyordu; Modrinth genelde yeni->eski dondugu icin tesaduifen
     * dogruydu - siralama degisirse eski surum secilebiliyordu.
     */
    public static ModVersion pickBestVersion(List<ModVersion> versions) {
        return ModrinthApi.pickBestVersion(versions, null);
    }

    /**
     * V29.7 SIKI SURUM GUVENLIGI: mcVersion verildiginde SADECE o MC
     * surumunu destekleyen surumler arasindan secilir. Hicbiri
     * desteklemiyorsa NULL doner - yanlis MC surumu icin derlenmis bir
     * jar'in kurulmasi oyunun hic acilmamasina yol acar; "yanlis surum
     * sessizce insin" ihtimali tamamen kaldirildi.
     */
    public static ModVersion pickBestVersion(List<ModVersion> versions, String mcVersion) {
        if (versions == null || versions.isEmpty()) {
            return null;
        }
        List<ModVersion> pool = versions;
        if (mcVersion != null && !mcVersion.isBlank()) {
            ArrayList<ModVersion> exact = new ArrayList<ModVersion>();
            for (ModVersion v : versions) {
                if (v.gameVersions != null && v.gameVersions.contains(mcVersion)) {
                    exact.add(v);
                }
            }
            if (exact.isEmpty()) {
                // Bu MC surumu icin HIC surum yok - yanlis jar'i kurmak yerine
                // reddet (cagiran "uyumlu surum yok" mesaji gosterir).
                return null;
            }
            pool = exact;
        }
        ModVersion best = null;
        int bestPrio = Integer.MAX_VALUE;
        for (ModVersion v : pool) {
            int p = "release".equalsIgnoreCase(v.versionType) ? 0 : ("beta".equalsIgnoreCase(v.versionType) ? 1 : 2);
            boolean better = p < bestPrio || (p == bestPrio && best != null && v.datePublished > best.datePublished);
            if (!better) continue;
            bestPrio = p;
            best = v;
        }
        return best;
    }

    /**
     * Filtresiz surum listesinden yukleyici + MC surumune EN UYUMLU olan secilir.
     * Katmanlama: birebir MC eslesmesi > ayni minor cizgi (1.21.x) > herhangi biri;
     * her katmanda loader eslesmesi tercih edilir. Bagimlilik modu icin "hic
     * surum yok" durumunu imkansiz kilar.
     */
    public static ModVersion pickBestCompatible(List<ModVersion> versions, String loader, String mcVersion) {
        if (versions == null || versions.isEmpty()) {
            return null;
        }
        String wantLoader = loader != null && !loader.isBlank() && !loader.equalsIgnoreCase("vanilla") ? loader.toLowerCase() : null;
        String mcMinor = null;
        if (mcVersion != null) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(\\d+\\.\\d+)").matcher(mcVersion);
            if (m.find()) {
                mcMinor = m.group(1);
            }
        }
        // V29.7 SIKI GECIT: mcVersion biliniyorsa adaylar SADECE ayni surum
        // cizgisinden (birebir eslesen veya ayni minor cizgi) secilir. Tamamen
        // farkli bir MC surumu icin derlenmis dep jar'i kurmak yerine null
        // doner - cagiran bunu "bagimlilik bulunamadi" olarak raporlar.
        List<ModVersion> candidates = versions;
        if (mcVersion != null) {
            candidates = new ArrayList<ModVersion>();
            for (ModVersion v : versions) {
                if (v == null || v.gameVersions == null) continue;
                if (v.gameVersions.contains(mcVersion)) {
                    candidates.add(v);
                    continue;
                }
                if (mcMinor != null) {
                    for (String gv : v.gameVersions) {
                        if (gv != null && gv.startsWith(mcMinor)) {
                            candidates.add(v);
                            break;
                        }
                    }
                }
            }
            if (candidates.isEmpty()) {
                return null;
            }
        }
        ModVersion best = null;
        int bestScore = Integer.MIN_VALUE;
        for (ModVersion v : candidates) {
            if (v == null || v.fileName == null || v.downloadUrl == null) continue;
            boolean loaderOk = wantLoader != null && v.loaders != null && v.loaders.stream().anyMatch(l -> l.equalsIgnoreCase(wantLoader));
            boolean mcExact = mcVersion != null && v.gameVersions != null && v.gameVersions.contains(mcVersion);
            int score = (loaderOk ? 2 : 0) + (mcExact ? 4 : 0);
            if (score > bestScore) {
                bestScore = score;
                best = v;
            } else if (score == bestScore && best != null && v.datePublished > best.datePublished) {
                best = v;
            }
        }
        return best;
    }

    public static ModResult getProject(String projectId) {
        try {
            String body = HttpUtil.getTextWithRetry("https://api.modrinth.com/v2/project/" + projectId, 3);
            if (body == null) return null;
            JsonObject resp = JsonParser.parseString(body).getAsJsonObject();
            ModResult r = new ModResult();
            r.id = resp.get("id").getAsString();
            r.slug = resp.has("slug") ? resp.get("slug").getAsString() : "";
            r.title = resp.get("title").getAsString();
            r.description = resp.has("description") ? resp.get("description").getAsString() : "";
            r.iconUrl = resp.has("icon_url") && !resp.get("icon_url").isJsonNull() ? resp.get("icon_url").getAsString() : null;
            int n = r.downloads = resp.has("downloads") ? resp.get("downloads").getAsInt() : 0;
            if (resp.has("categories")) {
                JsonArray cats = resp.getAsJsonArray("categories");
                for (int j = 0; j < cats.size(); ++j) {
                    r.categories.add(cats.get(j).getAsString());
                }
            }
            return r;
        }
        catch (Exception e) {
            return null;
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /** Back-compat alias: now routes through the retrying implementation. */
    public static List<ModVersion> getVersions(String projectId, String loader, String mcVersion) throws IOException {
        return getVersionsRetry(projectId, loader, mcVersion);
    }

    /**
     * V30 KOK COZUM: dogrulamada kullanilacak ETKIN MC surumu. Sadece gercek
     * bir MC surumustring'i dogrulamaya girer; null/blank/"latest"/cop
     * degerler null dondurur (o durumda zincir katmanlari surumsuz calisir
     * ama asagidaki seciciler yine en uyumlu surumu secer).
     */
    public static String effectiveMcVersion(String mcVersion) {
        if (mcVersion == null) return null;
        String v = mcVersion.trim();
        if (v.isEmpty() || "latest".equalsIgnoreCase(v)) return null;
        return ModrinthApi.looksLikeMcVersion(v) ? v : null;
    }

    /**
     * V35.3 TEK DOGRU NOKTA: bir surum kaydi hedef MC surumunu destekliyor mu?
     * effMc null ise (surum bilinmiyor/latest) her sey kabul - seciciler zaten
     * en uyumluyu secer. Null version da false (düsülebilir kayit kurma).
     * Politika pickBestCompatible ile ayni: birebir eslesme VEYA ayni minor
     * cizgi (1.21.x) kabul; FARKLI cizgi (1.21 hedefken 1.20/1.21.11-vs-26.2
     * gibi) RED - o jar oyunu patlatir. ensureVersion/installShaderPair da
     * dahil butun kurulum yollari bu metodu kullanir.
     */
    public static boolean versionSupportsMc(ModVersion v, String mcVersion) {
        if (v == null) return false;
        String effMc = ModrinthApi.effectiveMcVersion(mcVersion);
        if (effMc == null) return true;
        if (v.gameVersions == null) return false;
        if (v.gameVersions.contains(effMc)) return true;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(\\d+\\.\\d+)").matcher(effMc);
        if (m.find()) {
            String minor = m.group(1);
            for (String gv : v.gameVersions) {
                if (gv != null && gv.startsWith(minor)) return true;
            }
        }
        return false;
    }

    /**
     * V29.7.1: iki MC surumu arasindaki yaklasik mesafe (kucuk = yakin).
     * Surum cizgileri farkliysa buyuk ceza; ayni cizgide patch farki kadar.
     * Ayristirilamayan degerler -1 dondurur (aday sayilma).
     */
    public static int versionGap(String requested, String candidate) {
        if (requested == null || candidate == null) return -1;
        try {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("^(\\d+)(\\.(\\d+))?(\\.(\\d+))?");
            java.util.regex.Matcher mr = p.matcher(requested.trim());
            java.util.regex.Matcher mc = p.matcher(candidate.trim());
            if (!mr.find() || !mc.find()) return -1;
            int rMajor = Integer.parseInt(mr.group(1));
            int rMinor = mr.group(3) != null ? Integer.parseInt(mr.group(3)) : 0;
            int rPatch = mr.group(5) != null ? Integer.parseInt(mr.group(5)) : 0;
            int cMajor = Integer.parseInt(mc.group(1));
            int cMinor = mc.group(3) != null ? Integer.parseInt(mc.group(3)) : 0;
            int cPatch = mc.group(5) != null ? Integer.parseInt(mc.group(5)) : 0;
            if (rMajor != cMajor) return 1000 + Math.abs(rMajor - cMajor) * 100;
            if (rMinor != cMinor) return 100 + Math.abs(rMinor - cMinor) * 10;
            return Math.abs(rPatch - cPatch);
        }
        catch (Exception e) {
            return -1;
        }
    }

    /**
     * V29.7.1: arama istekleri icin 3 denemeli koruma. Eskiden search()
     * tek atimlikti - gecici bir ag hatasi arama sonuçlarini bosaltiyor,
     * kullanici "panel bozuk" saniyordu. 4xx hala hizli basarisiz olur.
     */
    private static JsonObject getJsonRetryUrl(String url) throws IOException {
        IOException last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return HttpUtil.getJson(url);
            }
            catch (IOException e) {
                last = e;
                String msg = e.getMessage() == null ? "" : e.getMessage();
                if (msg.contains("HTTP 4")) {
                    throw e;
                }
                if (attempt < 3) {
                    try {
                        Thread.sleep(250L * attempt);
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Kesildi", ie);
                    }
                }
            }
        }
        throw last != null ? last : new IOException("Arama basarisiz");
    }

    /** True if s looks like a real Minecraft version (1.x, ancient alpha/beta
     * formats, and the NEW post-2026 versioning like "26.2"). Guards the
     * game_versions facet against garbage like "2.22" that can never match
     * and silently yields "no compatible version". V29.7 FIX: "26.2" Once
     * taninmiyordu -> surum filtresi dusuyor -> Sodium 1.21.11 iniyordu. */
    public static boolean looksLikeMcVersion(String s) {
        if (s == null || s.isBlank()) return false;
        String v = s.trim();
        return v.matches("1\\.\\d+(\\.\\d+)?")          // 1.21.1, 1.16.5 ...
            || v.matches("[ab]1\\.\\d+(\\.\\d+)?(_\\d+)?") // a1.2.3, b1.7.3 ...
            || v.matches("\\d{2,}(\\.\\d+)+")           // 26.2, 26.1.1 ... (yeni versiyonlama)
            || v.equalsIgnoreCase("latest");
    }

    public static class ModResult {
        public String id;
        public String slug;
        public String title;
        public String description;
        public String iconUrl;
        public int downloads;
        public List<String> categories = new ArrayList<String>();
    }

    public static class ModVersion {
        public String id;
        public String versionNumber;
        public String versionType = "release";
        public String fileName;
        public String downloadUrl;
        public String sha1;
        public long datePublished;
        public List<String> gameVersions = new ArrayList<String>();
        public List<String> loaders = new ArrayList<String>();
        public List<Dependency> dependencies = new ArrayList<Dependency>();
    }

    public static class Dependency {
        public String type;
        public String projectId;
        public String versionId;
    }
}


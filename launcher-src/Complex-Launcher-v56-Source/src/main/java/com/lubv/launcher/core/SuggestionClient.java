package com.lubv.launcher.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;

/**
 * v46 - Oneri &amp; geri bildirim istemcisi.
 *
 * <p>Launcher'daki "Oneri &amp; Geri Bildirim" kutusu ile sitedeki ayni
 * kutu <b>ayni uca</b> yazar (api.php). Yani launcher'dan gonderilen bir
 * oneri, hicbir sey yapmaya gerek kalmadan sitedeki herkese acik listede
 * aninda gorunur; Ingilizce arayuzde de ayni sekilde calisir (dil bilgisi
 * sadece "lang" alani olarak gonderilir).
 *
 * <p>Uc nokta varsayilan olarak {@link #DEFAULT_API} adresidir. Gelistirme
 * veya test icin degistirilebilir:
 * <pre>java -Dcomplex.suggest.api=http://127.0.0.1:8099/api.php ...</pre>
 * veya {@code COMPLEX_SUGGEST_API} ortam degiskeni ile.
 */
public final class SuggestionClient {
    /** Sitedeki api.php - hem site formu hem launcher bunu kullanir. */
    public static final String DEFAULT_API = "https://complexlauncher.gt.tc/api.php";

    public static final int TEXT_MIN = 4;
    public static final int TEXT_MAX = 1000;

    /** Sitedeki kategori degerleriyle birebir ayni olmali. */
    private static final String[] CATEGORY_KEYS = {"idea", "bug", "feature", "mod", "other"};

    private SuggestionClient() {
    }

    /** Kullanimdaki uc adresi (sistem ozelligi / ortam degiskeni / varsayilan). */
    public static String apiBase() {
        String value = System.getProperty("complex.suggest.api");
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv("COMPLEX_SUGGEST_API");
        }
        return value == null || value.trim().isEmpty() ? DEFAULT_API : value.trim();
    }

    /** Sitenin kok adresi (api.php'nin bulundugu klasor). */
    public static String siteUrl() {
        String base = apiBase();
        int slash = base.lastIndexOf('/');
        return slash > 0 ? base.substring(0, slash + 1) : base;
    }

    /** Sitedeki oneri bolumunun tam adresi. */
    public static String ideasUrl() {
        return siteUrl() + "#ideas";
    }

    /** Kombodaki sirayla ayni: idea, bug, feature, mod, other. */
    public static String categoryKey(int index) {
        if (index < 0 || index >= CATEGORY_KEYS.length) {
            return "other";
        }
        return CATEGORY_KEYS[index];
    }

    /** Sunucuya hangi isletim sisteminden gonderildigini bildirir. */
    public static String currentPlatform() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            return "windows";
        }
        if (os.contains("mac")) {
            return "macos";
        }
        if (os.contains("linux") || os.contains("unix")) {
            return "linux";
        }
        return "other";
    }

    /**
     * Oneriyi siteye gonderir. Bu method bloklar; UI thread'inden cagrilmamali.
     *
     * @param text     oneri metni (zorunlu, 4-1000 karakter)
     * @param name     opsiyonel ad (bos olabilir)
     * @param category {@link #categoryKey(int)} degerlerinden biri
     * @param lang     arayuz dili ("tr" / "en")
     * @param version  launcher surumu (bos olabilir)
     * @param platform isletim sistemi (bkz. {@link #currentPlatform()})
     */
    public static Result submit(String text, String name, String category, String lang, String version, String platform) {
        JsonObject body = new JsonObject();
        body.addProperty("text", text == null ? "" : text);
        body.addProperty("name", name == null ? "" : name);
        body.addProperty("category", category == null ? "other" : category);
        body.addProperty("lang", lang == null ? "en" : lang);
        body.addProperty("version", version == null ? "" : version);
        body.addProperty("platform", platform == null ? "other" : platform);
        body.addProperty("website", ""); // honeypot: bos kalmali

        try {
            JsonObject response = HttpUtil.postJsonAllowError(apiBase() + "?action=submit", body);
            Result result = new Result();
            result.ok = readBoolean(response, "ok");
            result.id = readInt(response, "id");
            result.errorCode = readString(response, "error");
            result.message = readString(response, "message");
            if (!result.ok && result.errorCode == null) {
                result.errorCode = "rejected";
            }
            return result;
        }
        catch (IOException ioException) {
            return Result.network(ioException.getMessage());
        }
        catch (RuntimeException runtimeException) {
            // Sunucu JSON yerine HTML dondurdu (ornek: barindirma
            // saglayicisinin guvenlik sayfasi) veya beklenmedik bir govde.
            return Result.network(runtimeException.getMessage());
        }
    }

    private static String readString(JsonObject jsonObject, String key) {
        if (jsonObject == null || !jsonObject.has(key) || jsonObject.get(key).isJsonNull()) {
            return null;
        }
        JsonElement element = jsonObject.get(key);
        return element.isJsonPrimitive() ? element.getAsString() : null;
    }

    private static boolean readBoolean(JsonObject jsonObject, String key) {
        String value = readString(jsonObject, key);
        return value != null && value.equalsIgnoreCase("true");
    }

    private static int readInt(JsonObject jsonObject, String key) {
        String value = readString(jsonObject, key);
        if (value == null) {
            return 0;
        }
        try {
            return (int) Double.parseDouble(value);
        }
        catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    /** Gonderim sonucu. {@code ok == false} ise {@code errorCode} dolu olur. */
    public static final class Result {
        public boolean ok;
        /** Sunucudan donen hata kodu: text_too_short, rate_limited, network, ... */
        public String errorCode;
        /** Sunucunun insan-okur mesaji (genelde Ingilizce). */
        public String message;
        /** Kaydedilen onerinin id'si (ok ise). */
        public int id;

        static Result network(String detail) {
            Result result = new Result();
            result.ok = false;
            result.errorCode = "network";
            result.message = detail == null ? "" : detail;
            return result;
        }
    }
}

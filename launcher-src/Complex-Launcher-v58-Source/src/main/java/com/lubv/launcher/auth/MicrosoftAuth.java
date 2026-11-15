/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lubv.launcher.auth.MinecraftSession;
import com.lubv.launcher.core.HttpUtil;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.function.Consumer;

public class MicrosoftAuth {
    private static final String CLIENT_ID = "00000000402b5328";
    private static final String REDIRECT_URI = "https://login.live.com/oauth20_desktop.srf";
    private static final String SCOPE = "XboxLive.signin offline_access";
    private static final String AUTHORIZE_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize";
    private static final String TOKEN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    private static final String VERIFIER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";

    public static AuthRequest beginLogin() {
        String string = MicrosoftAuth.randomVerifier();
        String string2 = MicrosoftAuth.codeChallenge(string);
        String string3 = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize?client_id=" + MicrosoftAuth.urlEnc(CLIENT_ID) + "&response_type=code&redirect_uri=" + MicrosoftAuth.urlEnc(REDIRECT_URI) + "&scope=" + MicrosoftAuth.urlEnc(SCOPE) + "&code_challenge=" + MicrosoftAuth.urlEnc(string2) + "&code_challenge_method=S256";
        return new AuthRequest(string, string3);
    }

    public static MinecraftSession completeLogin(AuthRequest authRequest, String string, Consumer<String> consumer) throws Exception {
        String string2 = MicrosoftAuth.extractCode(string);
        if (string2 == null) {
            throw new Exception("Kod al\u0131namad\u0131. L\u00fctfen taray\u0131c\u0131n\u0131n adres \u00e7ubu\u011fundaki y\u00f6nlendirme adresini oldu\u011fu gibi yap\u0131\u015ft\u0131r\u0131n.");
        }
        consumer.accept("Microsoft token al\u0131n\u0131yor...");
        JsonObject jsonObject = HttpUtil.postFormAllowError(TOKEN_URL, "grant_type=authorization_code&client_id=" + MicrosoftAuth.urlEnc(CLIENT_ID) + "&code=" + MicrosoftAuth.urlEnc(string2) + "&redirect_uri=" + MicrosoftAuth.urlEnc(REDIRECT_URI) + "&code_verifier=" + MicrosoftAuth.urlEnc(authRequest.verifier) + "&scope=" + MicrosoftAuth.urlEnc(SCOPE));
        if (jsonObject.has("error")) {
            String string3 = jsonObject.has("error_description") ? jsonObject.get("error_description").getAsString() : jsonObject.get("error").getAsString();
            throw new Exception("Microsoft reddetti: " + string3);
        }
        String string4 = jsonObject.get("access_token").getAsString();
        String string5 = jsonObject.get("refresh_token").getAsString();
        return MicrosoftAuth.finishLogin(string4, string5, consumer);
    }

    public static MinecraftSession loginWithRefreshToken(String string, Consumer<String> consumer) throws Exception {
        consumer.accept("Kay\u0131tl\u0131 hesapla giri\u015f yap\u0131l\u0131yor...");
        JsonObject jsonObject = HttpUtil.postFormAllowError(TOKEN_URL, "grant_type=refresh_token&client_id=" + MicrosoftAuth.urlEnc(CLIENT_ID) + "&refresh_token=" + MicrosoftAuth.urlEnc(string) + "&scope=" + MicrosoftAuth.urlEnc(SCOPE));
        if (jsonObject.has("error")) {
            throw new Exception(jsonObject.get("error").getAsString());
        }
        String string2 = jsonObject.get("access_token").getAsString();
        String string3 = jsonObject.get("refresh_token").getAsString();
        return MicrosoftAuth.finishLogin(string2, string3, consumer);
    }

    private static MinecraftSession finishLogin(String string, String string2, Consumer<String> consumer) throws Exception {
        consumer.accept("Xbox Live ile do\u011frulan\u0131yor...");
        JsonObject jsonObject = new JsonObject();
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("AuthMethod", "RPS");
        jsonObject2.addProperty("SiteName", "user.auth.xboxlive.com");
        jsonObject2.addProperty("RpsTicket", "d=" + string);
        jsonObject.add("Properties", jsonObject2);
        jsonObject.addProperty("RelyingParty", "http://auth.xboxlive.com");
        jsonObject.addProperty("TokenType", "JWT");
        JsonObject jsonObject3 = HttpUtil.postJson("https://user.auth.xboxlive.com/user/authenticate", jsonObject);
        String string3 = jsonObject3.get("Token").getAsString();
        String string4 = jsonObject3.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
        consumer.accept("XSTS token al\u0131n\u0131yor...");
        JsonObject jsonObject4 = new JsonObject();
        JsonObject jsonObject5 = new JsonObject();
        jsonObject5.addProperty("SandboxId", "RETAIL");
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(string3);
        jsonObject5.add("UserTokens", jsonArray);
        jsonObject4.add("Properties", jsonObject5);
        jsonObject4.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        jsonObject4.addProperty("TokenType", "JWT");
        JsonObject jsonObject6 = HttpUtil.postJsonAllowError("https://xsts.auth.xboxlive.com/xsts/authorize", jsonObject4);
        if (jsonObject6.has("XErr")) {
            long l = jsonObject6.get("XErr").getAsLong();
            if (l == 2148916233L) {
                throw new Exception("Bu Microsoft hesab\u0131n\u0131n bir Xbox hesab\u0131 yok. Xbox.com \u00fczerinden bir hesap olu\u015fturun.");
            }
            if (l == 2148916238L) {
                throw new Exception("Bu hesap bir \u00e7ocuk hesab\u0131 - ebeveyn izni gerekiyor (Aile G\u00fcvenli\u011fi ayarlar\u0131).");
            }
            throw new Exception("Xbox do\u011frulama hatas\u0131: " + l);
        }
        String string5 = jsonObject6.get("Token").getAsString();
        consumer.accept("Minecraft servisleri ile giri\u015f yap\u0131l\u0131yor...");
        JsonObject jsonObject7 = new JsonObject();
        jsonObject7.addProperty("identityToken", "XBL3.0 x=" + string4 + ";" + string5);
        JsonObject jsonObject8 = HttpUtil.postJson("https://api.minecraftservices.com/authentication/login_with_xbox", jsonObject7);
        String string6 = jsonObject8.get("access_token").getAsString();
        consumer.accept("Profil bilgisi al\u0131n\u0131yor...");
        JsonObject jsonObject9 = HttpUtil.getJsonAuthAllowError("https://api.minecraftservices.com/minecraft/profile", string6);
        if (jsonObject9.has("error") || !jsonObject9.has("id")) {
            throw new Exception("Bu Microsoft hesab\u0131 Minecraft sat\u0131n almam\u0131\u015f g\u00f6r\u00fcn\u00fcyor.");
        }
        String string7 = jsonObject9.get("id").getAsString();
        String string8 = jsonObject9.get("name").getAsString();
        consumer.accept("\u2713 Giri\u015f ba\u015far\u0131l\u0131: " + string8);
        MinecraftSession minecraftSession = new MinecraftSession();
        minecraftSession.username = string8;
        minecraftSession.uuid = MicrosoftAuth.formatUuid(string7);
        minecraftSession.accessToken = string6;
        minecraftSession.msRefreshToken = string2;
        minecraftSession.userType = "msa";
        return minecraftSession;
    }

    private static String extractCode(String string) {
        if (string == null) {
            return null;
        }
        String string2 = string.trim();
        if (string2.isEmpty()) {
            return null;
        }
        int n = string2.indexOf("code=");
        if (n >= 0) {
            String string3 = string2.substring(n + 5);
            int n2 = string3.indexOf(38);
            if (n2 >= 0) {
                string3 = string3.substring(0, n2);
            }
            return (string3 = string3.trim()).isEmpty() ? null : string3;
        }
        if (string2.contains("://")) {
            return null;
        }
        return string2;
    }

    private static String randomVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 64; ++i) {
            stringBuilder.append(VERIFIER_CHARS.charAt(secureRandom.nextInt(VERIFIER_CHARS.length())));
        }
        return stringBuilder.toString();
    }

    private static String codeChallenge(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] byArray = messageDigest.digest(string.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(byArray);
        }
        catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String formatUuid(String string) {
        if (string.contains("-")) {
            return string;
        }
        return string.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
    }

    private static String urlEnc(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    public static class AuthRequest {
        public final String verifier;
        public final String authUrl;

        AuthRequest(String string, String string2) {
            this.verifier = string;
            this.authUrl = string2;
        }
    }
}


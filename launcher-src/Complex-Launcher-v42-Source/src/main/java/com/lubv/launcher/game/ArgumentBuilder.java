/*
 * Decompiled with CFR 0.152.
 */
package com.lubv.launcher.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lubv.launcher.auth.MinecraftSession;
import com.lubv.launcher.core.Instance;
import com.lubv.launcher.core.Paths;
import com.lubv.launcher.core.SessionServerMock;
import com.lubv.launcher.core.SkinServer;
import com.lubv.launcher.game.GameVersion;
import com.lubv.launcher.game.OsRules;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArgumentBuilder {
    public static List<String> buildFullCommand(LaunchContext launchContext, String string) {
        int n;
        boolean bl;
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add(string);
        boolean bl2 = launchContext.extraJvmArgs != null && launchContext.extraJvmArgs.contains("-Xmx");
        boolean bl3 = bl = launchContext.extraJvmArgs != null && launchContext.extraJvmArgs.contains("-Xms");
        if (!bl2) {
            arrayList.add("-Xmx" + launchContext.ramGB + "G");
        }
        if (!bl) {
            int n2 = launchContext.ramGB >= 4 ? 2 : (launchContext.ramGB >= 2 ? 1 : 0);
            if (n2 > 0) {
                arrayList.add("-Xms" + n2 + "G");
            } else {
                arrayList.add("-Xms512m");
            }
        }
        arrayList.add("-noverify");
        arrayList.add("-Djava.library.path=" + launchContext.nativesDir.getAbsolutePath());
        arrayList.add("-Dorg.lwjgl.librarypath=" + launchContext.nativesDir.getAbsolutePath());
        arrayList.add("-Dminecraft.launcher.brand=Complex-Launcher");
        arrayList.add("-Dminecraft.launcher.version=42.0.0");
        if (OsRules.CURRENT_OS == OsRules.Os.MAC) {
            arrayList.add("-XstartOnFirstThread");
        }
        arrayList.add("-Dfml.ignorePatchDiscrepancies=true");
        arrayList.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
        n = SessionServerMock.port();
        if (n > 0) {
            String string2 = "http://127.0.0.1:" + n;
            arrayList.add("-Dminecraft.api.auth.host=" + string2);
            arrayList.add("-Dminecraft.api.session.host=" + string2);
            arrayList.add("-Dminecraft.api.account.host=" + string2);
            arrayList.add("-Dminecraft.api.services.host=" + string2);
            arrayList.add("-Dminecraft.auth.host=" + string2);
            arrayList.add("-Dminecraft.session.host=" + string2);
            arrayList.add("-Dauthlib.injector.yggdrasil.preferedYggdrasilServerUrl=" + string2);
        }
        if ("Dahili GPU".equals(launchContext.gpuSelection)) {
            arrayList.add("-Dsun.java2d.d3d=false");
            arrayList.add("-Dforge.enabledGPU=0");
            arrayList.add("-Dorg.lwjgl.system.SharedLibraryExtractPath=" + System.getProperty("java.io.tmpdir"));
        } else if ("Dis GPU".equals(launchContext.gpuSelection)) {
            arrayList.add("-Dforge.enabledGPU=1");
        }
        if (launchContext.extraJvmArgs != null && !launchContext.extraJvmArgs.isBlank()) {
            for (String string2 : launchContext.extraJvmArgs.trim().split("\\s+")) {
                arrayList.add(string2);
            }
        }
        if (launchContext.version.jvmArgumentsNew != null) {
            ArgumentBuilder.resolveArgList(launchContext.version.jvmArgumentsNew, ArgumentBuilder.placeholders(launchContext)).forEach(arrayList::add);
        }
        if (launchContext.loaderProfile != null && launchContext.loaderProfile.jvmArgumentsNew != null) {
            ArgumentBuilder.resolveArgList(launchContext.loaderProfile.jvmArgumentsNew, ArgumentBuilder.loaderPlaceholders(launchContext)).forEach(arrayList::add);
        }
        arrayList.addAll(launchContext.extraJvmArgList);
        // -cp: bazi surum JSON'lari (jvmArgumentsNew) kendi -cp ${classpath}
        // girdisini icerir; o zaman tekrar eklemeye gerek yok.
        boolean classpathPresent = false;
        for (String existingArg : arrayList) {
            if ("-cp".equals(existingArg) || "-classpath".equals(existingArg)
                    || (existingArg != null && existingArg.startsWith("-cp"))) {
                classpathPresent = true;
                break;
            }
        }
        if (!classpathPresent) {
            arrayList.add("-cp");
            arrayList.add(launchContext.classpath);
        }
        // JVM argumanlarini tekillestir (main class'tan onceki kisim):
        // kullanici jvmArgs + perf bayraklari + surum JSON bayraklari ust uste
        // binebiliyor; Java yine de calisir ama komut sisar ve karisiklik yaratir.
        int mainClassIndex = arrayList.size() - 1;
        arrayList = ArgumentBuilder.dedupeJvmArgs(arrayList, mainClassIndex);
        String string3 = launchContext.forgeMainClassOverride != null ? launchContext.forgeMainClassOverride : (launchContext.loaderProfile != null && launchContext.loaderProfile.mainClass != null ? launchContext.loaderProfile.mainClass : launchContext.version.mainClass);
        arrayList.add(string3);
        Map<String, String> map = ArgumentBuilder.placeholders(launchContext);
        if (launchContext.version.gameArgumentsNew != null) {
            ArgumentBuilder.resolveArgList(launchContext.version.gameArgumentsNew, map).forEach(arrayList::add);
        } else if (launchContext.version.minecraftArgumentsLegacy != null) {
            for (String string4 : launchContext.version.minecraftArgumentsLegacy.split(" ")) {
                arrayList.add(ArgumentBuilder.substitute(string4, map));
            }
        } else {
            arrayList.add("--username");
            arrayList.add(launchContext.session.username);
            arrayList.add("--version");
            arrayList.add(launchContext.version.id);
            arrayList.add("--gameDir");
            arrayList.add(launchContext.gameDir.getAbsolutePath());
            arrayList.add("--assetsDir");
            arrayList.add(Paths.ASSETS_DIR.getAbsolutePath());
            arrayList.add("--uuid");
            arrayList.add(launchContext.session.uuid);
            arrayList.add("--accessToken");
            arrayList.add(launchContext.session.accessToken);
        }
        Object object = ArgumentBuilder.effectiveSkinUrl(launchContext.session);
        if (object != null && !((String)object).isBlank()) {
            arrayList.add("--userProperties");
            arrayList.add(ArgumentBuilder.buildUserPropertiesJson(launchContext.session, (String)object));
        }
        arrayList.addAll(launchContext.extraGameArgs);
        if (launchContext.loaderProfile != null) {
            if (launchContext.loaderProfile.gameArgumentsNew != null) {
                ArgumentBuilder.resolveArgList(launchContext.loaderProfile.gameArgumentsNew, ArgumentBuilder.loaderPlaceholders(launchContext)).forEach(arrayList::add);
            } else if (launchContext.loaderProfile.minecraftArgumentsLegacy != null) {
                object = ArgumentBuilder.loaderPlaceholders(launchContext);
                for (String string5 : launchContext.loaderProfile.minecraftArgumentsLegacy.split(" ")) {
                    arrayList.add(ArgumentBuilder.substitute(string5, (Map<String, String>)object));
                }
            }
        }
        ArgumentBuilder.removeQuickPlayArgs(arrayList);
        ArgumentBuilder.patchIgnoreListForVanillaJar(arrayList, launchContext);
        return arrayList;
    }

    private static ArrayList<String> dedupeJvmArgs(List<String> args, int mainClassIndex) {
        java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<String>();
        ArrayList<String> out = new ArrayList<String>(args.size());
        for (int i = 0; i < args.size(); ++i) {
            String token = args.get(i);
            if (i <= mainClassIndex) {
                if (seen.add(token)) {
                    out.add(token);
                }
            } else {
                out.add(token);
            }
        }
        return out;
    }

    private static void patchIgnoreListForVanillaJar(List<String> list, LaunchContext launchContext) {
        if (launchContext.clientJar == null) {
            return;
        }
        String string = launchContext.clientJar.getName();
        for (int i = 0; i < list.size(); ++i) {
            String string2 = list.get(i);
            if (string2 == null || !string2.startsWith("-DignoreList=")) continue;
            if (string2.contains(string)) {
                return;
            }
            list.set(i, string2 + "," + string);
            return;
        }
    }

    private static void removeQuickPlayArgs(List<String> list) {
        for (int i = list.size() - 1; i >= 0; --i) {
            String string = list.get(i);
            if (string == null || !string.startsWith("--quickPlay")) continue;
            if (i + 1 < list.size() && !list.get(i + 1).startsWith("--")) {
                list.remove(i + 1);
            }
            list.remove(i);
        }
    }

    private static Map<String, String> placeholders(LaunchContext launchContext) {
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
        linkedHashMap.put("auth_player_name", launchContext.session.username);
        linkedHashMap.put("version_name", launchContext.version.id);
        linkedHashMap.put("game_directory", launchContext.gameDir.getAbsolutePath());
        linkedHashMap.put("assets_root", Paths.ASSETS_DIR.getAbsolutePath());
        linkedHashMap.put("assets_index_name", launchContext.version.assetIndexId);
        linkedHashMap.put("auth_uuid", launchContext.session.uuid);
        linkedHashMap.put("auth_access_token", launchContext.session.accessToken);
        linkedHashMap.put("user_type", launchContext.session.userType);
        linkedHashMap.put("version_type", "release");
        linkedHashMap.put("clientid", "complex-launcher");
        linkedHashMap.put("auth_xuid", launchContext.session.uuid.replace("-", ""));
        linkedHashMap.put("user_properties", "{}");
        linkedHashMap.put("natives_directory", launchContext.nativesDir.getAbsolutePath());
        linkedHashMap.put("launcher_name", "Complex-Launcher");            linkedHashMap.put("launcher_version", "40.0.0");
        linkedHashMap.put("classpath", launchContext.classpath);
        linkedHashMap.put("library_directory", Paths.LIBRARIES_DIR.getAbsolutePath());
        linkedHashMap.put("classpath_separator", File.pathSeparator);
        return linkedHashMap;
    }

    private static Map<String, String> loaderPlaceholders(LaunchContext launchContext) {
        Map<String, String> map = ArgumentBuilder.placeholders(launchContext);
        if (launchContext.loaderProfile != null) {
            map.put("version_name", launchContext.loaderProfile.id);
        }
        return map;
    }

    private static List<String> resolveArgList(JsonArray jsonArray, Map<String, String> map) {
        ArrayList<String> arrayList = new ArrayList<String>();
        if (jsonArray == null) {
            return arrayList;
        }
        for (int i = 0; i < jsonArray.size(); ++i) {
            boolean bl;
            JsonElement jsonElement = jsonArray.get(i);
            if (jsonElement.isJsonPrimitive()) {
                arrayList.add(ArgumentBuilder.substitute(jsonElement.getAsString(), map));
                continue;
            }
            if (!jsonElement.isJsonObject()) continue;
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean bl2 = bl = !jsonObject.has("rules") || OsRules.isAllowed(jsonObject.getAsJsonArray("rules"));
            if (!bl || !jsonObject.has("value")) continue;
            JsonElement jsonElement2 = jsonObject.get("value");
            if (jsonElement2.isJsonArray()) {
                for (int j = 0; j < jsonElement2.getAsJsonArray().size(); ++j) {
                    arrayList.add(ArgumentBuilder.substitute(jsonElement2.getAsJsonArray().get(j).getAsString(), map));
                }
                continue;
            }
            arrayList.add(ArgumentBuilder.substitute(jsonElement2.getAsString(), map));
        }
        return arrayList;
    }

    private static String substitute(String string, Map<String, String> map) {
        String string2 = string;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() == null) continue;
            string2 = string2.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return string2;
    }

    private static String effectiveSkinUrl(MinecraftSession minecraftSession) {
        if (minecraftSession == null) {
            return null;
        }
        if (minecraftSession.skinUrl != null && !minecraftSession.skinUrl.isBlank()) {
            return minecraftSession.skinUrl;
        }
        return SkinServer.skinUrl(minecraftSession.username);
    }

    private static String buildUserPropertiesJson(MinecraftSession minecraftSession, String string) {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("profileId", minecraftSession.uuid.replace("-", ""));
            jsonObject.addProperty("profileName", minecraftSession.username);
            JsonObject jsonObject2 = new JsonObject();
            JsonObject jsonObject3 = new JsonObject();
            jsonObject3.addProperty("url", string);
            jsonObject2.add("SKIN", jsonObject3);
            jsonObject.add("textures", jsonObject2);
            String string2 = Base64.getEncoder().encodeToString(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
            JsonObject jsonObject4 = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            JsonObject jsonObject5 = new JsonObject();
            jsonObject5.addProperty("name", "textures");
            jsonObject5.addProperty("value", string2);
            jsonArray.add(jsonObject5);
            jsonObject4.add("textures", jsonArray);
            return jsonObject4.toString();
        }
        catch (Exception exception) {
            return "{}";
        }
    }

    public static class LaunchContext {
        public GameVersion version;
        public GameVersion loaderProfile;
        public MinecraftSession session;
        public File clientJar;
        public String classpath;
        public File gameDir;
        public File nativesDir;
        public int ramGB;
        public String extraJvmArgs;
        public String forgeMainClassOverride;
        public String gpuSelection = "Otomatik";
        public String preLaunchCmd = "";
        public Instance instance = null;
        public List<String> extraGameArgs = new ArrayList<String>();
        public List<String> extraJvmArgList = new ArrayList<String>();
    }
}


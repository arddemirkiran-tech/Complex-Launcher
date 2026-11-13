<img src="logo_1024.png" align="left" width="350">
 
 
 
 # 🚀 Complex Launcher — Advanced Minecraft Launcher

A next-generation Minecraft launcher focused on robust mod management, real-time performance monitoring, and a flexible, modern user interface.

## ⬇️ Download (Latest: v54)

| File | Description |
|---|---|
| [`Complex-Launcher-Setup.exe`](https://github.com/arddemirkiran-tech/Complex-Launcher/releases/latest/download/Complex-Launcher-Setup.exe) | Windows installer — auto-downloads the latest release, creates shortcuts, checks Java |
| [`Complex-Launcher-v54-Windows.zip`](https://github.com/arddemirkiran-tech/Complex-Launcher/releases/latest/download/Complex-Launcher-v54-Windows.zip) | Portable Windows build (bundled runtime) |
| [`Complex-Launcher-v54-Linux.zip`](https://github.com/arddemirkiran-tech/Complex-Launcher/releases/latest/download/Complex-Launcher-v54-Linux.zip) | Portable Linux build |

Website: **https://complexlauncher.gt.tc**

## 🆕 What's new in v54

- **Turkish characters fixed** — the bundled Space Grotesk and Bricolage Grotesque files were Google-Fonts *subsets* (latin + latin-ext) that shared one family name. Java resolved the family to the latin face, which has no `ş`/`ğ`/`İ`/`ı` glyphs, so those letters were drawn with the missing-glyph glyph (a `?` on screen). Each typeface is now a single merged face (720 and 476 glyphs, full Latin + Latin Extended) under a dedicated family name (`Complex Space Grotesk`, `Complex Bricolage`), and `BrandFonts` refuses to register a face unless it can draw ASCII plus `ÇĞİÖŞÜçğıöşü`, falling back to a system font otherwise.
- Includes all v53 improvements (balanced home hero, ~1 s launch preparation).
- Version bump to **v54** across the launcher and update checks.

## 🆕 What's new in v53

- **Balanced home page** — the selected-instance hero card used to take `weighty = 1.0` and swallow every spare vertical pixel, leaving a huge empty gradient and pushing the recent-instances / game-settings cards off screen. It now keeps its natural height (~240 px) and the remaining space goes to the settings card, so both columns end together.
- **Much faster game launch** — the launcher-side preparation now finishes in about a second on an already-installed instance (a `[Hız] Launcher hazırlığı … ms` line is printed to the console):
  - version and loader JSONs (Minecraft, Fabric loader/profile, Forge promotions, NeoForge metadata) are read from the local cache first instead of hitting the network on every launch;
  - the resolved Java path and detected major version are persisted in `java-select.properties`, so `java -version` is no longer spawned for every candidate on each start;
  - ~400 library JARs are no longer SHA-1 hashed on every start — a size/mtime fingerprint in `versions/<id>/.libs-ready` skips verification while it matches;
  - thousands of assets are skipped through `assets/indexes/<index>.ready` once they have been fully downloaded and verified;
  - natives extraction checks `.natives-ready` with a cheap size/mtime fingerprint before reading any native JAR.
- Version bump to **v53** across the launcher and update checks.

## 🆕 What's new in v52

- **One-click sidebar tabs** — tab selection now happens on mouse press instead of click, so a tab never needs a second press when the pointer drifts or the window was just activated. Release still works as a fallback; no double switching.
- **Home page glitch fixed** — the console was transparent but still marked opaque, so selecting text or appending logs painted over stale pixels and the repaint leaked to the whole window (the page looked frozen and the lower half duplicated). The console now paints its own solid card colour and repaints stay inside it. Home refresh runs at a smooth 30 FPS, and only when the animated background is not already driving it, so the two timers cannot tear. The hero chips no longer call `revalidate()` every 500 ms for unchanged text, and press animations on accent/ghost/danger/launch buttons scale their text with the background.
- **Theme Creator hover previews** — hovering a color row (or a font family / title or button weight in the dropdown) immediately repaints the live mockup with that value; leaving without selecting restores it, and hover no longer mutates the saved draft. The dialog and its scroll pane are now fully opaque, so applying a theme no longer leaves copied/ghosted panels behind.
- **Theme Creator** — the preset list now comes from the real 22 themes (the old hardcoded list silently fell back to Nebula), every one of the 13 colors has an editable hex field, the theme name is bound live, Randomize keeps your name, a Close button restores the previous theme, and the falling-photos section actually appears.
- **Installer language reaches the launcher** — the language picked in the Setup EXE (`%LOCALAPPDATA%\ComplexLauncher\setup-lang.txt`) now sets the launcher language on first run. A later change inside the launcher wins; re-running Setup with another language applies the new choice.
- Version bump to **v52** across the launcher and update checks.

## 🆕 What's new in v45

- **Mods: "Change Version" (`Sürüm Değiştir…`)** — right-click an installed mod and pick another build of *that same mod*. The Minecraft version and loader stay the same; you get a real picker with every compatible build (newest first), the currently installed one marked and pre-selected.
- **Safe switching** — the chosen build is downloaded with SHA-1 verification, required dependencies are resolved, the registry is updated and the **old jar is deleted**, so two versions of the same mod can never sit in `mods/` together. Picking the version you already have is a no-op, and the entry is disabled for CurseForge / local JAR mods (no resolvable version list).
- Includes all v36–v44 fixes below.

## 🆕 Also in v36

- **My Servers: per-server instances** — every server gets its own `Server-<ip>` instance; Mods / Shaders / Resource Packs tabs install **per server**. New **Mods** button on each server card.
- **Loader selection fixed** — picking Fabric/Forge/NeoForge for a server now actually launches with that loader.
- **Fabric launch fixed (twice)** — coordinate-based library dedup ends the *"duplicate ASM classes"* crash; a new crash watchdog detects VulkanMod renderer crashes, removes the mod and falls back to the stable Sodium renderer automatically.
- **Auto-install bug fixed at the root** — Sodium / Iris / Embeddium / Oculus no longer install themselves when their auto-install checkbox is OFF; a hard preference gate now guards every automatic install path.
- **Design modes** — Settings → Design: **Modern** (animated gradient orbs + star field background, 30 FPS capped, pauses when hidden) or **Classic** (the clean lightweight default).
- Version bump to **v36** across the launcher, Discord RPC and update checks.

## 🌟 Key Features

- **Mod & Modpack Management (`ModsPanel`, `ModpacksPanel`)**  
  Automatic mod downloads, dependency resolution, and one-click installation of curated modpacks.

- **Visual Customization (`ResourcepacksPanel`, `ShaderPanel`)**  
  Easy installation and switching of resource packs and shaders.

- **Server Management (`ServersPanel`, `MyServersPanel`)**  
  Quick-connect UI for local and remote servers (Arclight, Paper, Forge, Fabric).

- **Flexible Login (`LoginDialog`, `OfflineLoginDialog`)**  
  Support for online (premium) authentication and offline/local profiles for pirate players
.

- **UI & Theming (`Theme`, `GalleryStrip`, `SplashScreen`)**  
  Modern components and configurable themes for a polished launcher experience.

## ⚙️ Installation & `install_java.bat` Status
Now The Repo Has a java installation bat 
updated with v27


## 🚀 System Optimization Recommendations

- RAM allocation: On systems with 16 GB RAM, allocate **6–8 GB** to Minecraft for heavily-modded packs to improve stability.
- Network: If using VPNs, tunnels, or proxies, monitor ping values to diagnose latency when connecting to remote servers.

## Contributing & Next Steps

If you'd like, I can:
- Add a startup script that prefers a bundled JRE and falls back to system Java.
- Provide Gradle/Maven build instructions and an example runnable JAR configuration.
- Add usage examples, screenshots, release notes, and a short roadmap.

Tell me which items you'd like added and I will prepare and commit the scripts and README updates.
[https://complexlauncher.gt.tc/](https://complexlauncher.gt.tc/)

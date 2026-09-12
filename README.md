<img src="logo_1024.png" align="left" width="350">
 
 
 
 # 🚀 Complex Launcher — Advanced Minecraft Launcher

A next-generation Minecraft launcher focused on robust mod management, real-time performance monitoring, and a flexible, modern user interface.

## ⬇️ Download (Latest: v33)

| File | Description |
|---|---|
| [`Complex-Launcher-Setup.exe`](https://github.com/arddemirkiran-tech/Complex-Launcher/releases/latest/download/Complex-Launcher-Setup.exe) | Windows installer — auto-downloads the latest release, creates shortcuts, checks Java |
| [`Complex-Launcher-v36-Windows.zip`](https://github.com/arddemirkiran-tech/Complex-Launcher/releases/latest/download/Complex-Launcher-v36-Windows.zip) | Portable Windows build (bundled runtime) |
| [`Complex-Launcher-v36-Linux.zip`](https://github.com/arddemirkiran-tech/Complex-Launcher/releases/latest/download/Complex-Launcher-v36-Linux.zip) | Portable Linux build |

Website: **https://complexlauncher.gt.tc**

## 🆕 What's new in v36

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

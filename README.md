# 🚀 LUBV Minecraft Launcher

A next-generation Minecraft launcher with advanced mod management, real-time performance monitoring, and a customizable user interface.

## 🌟 Key Features

Based on the repository structure, the launcher includes the following modules:

- **Comprehensive Mod & Modpack Management (`ModsPanel`, `ModpacksPanel`)** — automatic mod download, dependency resolution, and one-click installation of curated modpacks.
- **Visual Customization (`ResourcepacksPanel`, `ShaderPanel`)** — easy management and application of resource packs and shaders.
- **Performance & System Monitoring (`FpsOverlay`, `SystemMonitor`)** — in-game overlays for FPS, CPU/GPU usage, memory pressure, and bottleneck detection; useful for testing on systems like Ryzen 5 + RTX 3070.
- **Server Management (`ServersPanel`, `MyServersPanel`)** — quick-connect UI for local and remote servers (Arclight, Paper, Forge, Fabric).
- **Flexible Login Options (`LoginDialog`, `OfflineLoginDialog`)** — supports both online (premium) accounts and offline profiles for local testing.
- **UI & Theming (`Theme`, `GalleryStrip`, `SplashScreen`)** — modern UI components and configurable themes for a polished launcher experience.

## ⚙️ Installation & `install_java.bat` Status

The repository currently does not include an `install_java.bat` file. This is not a blocker. You can either install Java manually (recommended) or add a batch script to automate Java checks and starting the launcher.

### Java Requirements

- For modern Fabric and Forge modpacks (Minecraft 1.20.1+), use **Java 17** or **Java 21**.
- Ensure `JAVA_HOME` is set and the `java` executable is available on your PATH so the launcher can start game instances reliably.

### Example Batch File (`install_java.bat`)

Save the following as `install_java.bat` or `start_launcher.bat` to check for Java and start the launcher. Adjust the startup command to match your built artifact (jar or main class):

```bat
@echo off
title LUBV Launcher - Java Check

echo Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
  echo Java not found or not in PATH.
  echo Please install Java 17 or Java 21 and set JAVA_HOME and PATH accordingly.
  pause
  exit /b 1
)

echo Java detected:
java -version

echo Starting LUBV Launcher...
:: Replace the following line with your launcher startup command
:: Example (runnable jar):
:: java -jar build/libs/lubv-launcher.jar
:: Example (from compiled classes):
:: java -cp "out/production/classes;lib/*" com.lubv.launcher.Main

pause
```

Notes:
- Replace the sample startup lines with the actual path to your JAR or classpath + main class.
- For distribution, consider bundling a platform-appropriate JRE or providing platform-specific installation instructions.

## 🚀 System Optimization Recommendations

- RAM allocation: On systems with 16 GB RAM, allocate between **6 GB and 8 GB** to Minecraft for heavily-modded packs to improve stability.
- Monitoring: Use the built-in `SystemMonitor` and `FpsOverlay` to observe CPU/GPU usage, memory pressure, and frame rate while testing modpacks.
- Network: If using VPNs or tunnels, monitor ping values in `SystemMonitor` to diagnose latency spikes when connecting to remote servers.

## Contributing & Next Steps

I can:

- Add a polished `install_java.bat` and a cross-platform shell script `install_java.sh` and commit them to the repo.
- Add a launcher start script that prefers a bundled JRE and falls back to system Java.
- Provide build and distribution guidance (Gradle/Maven) and example run configurations.

Tell me which of the above you'd like me to add and I'll commit the scripts and README updates.

# 🚀 Complex Launcher — Advanced Minecraft Launcher

A next-generation Minecraft launcher focused on robust mod management, real-time performance monitoring, and a flexible, modern user interface.

## 🌟 Key Features

- **Mod & Modpack Management (`ModsPanel`, `ModpacksPanel`)**  
  Automatic mod downloads, dependency resolution, and one-click installation of curated modpacks.

- **Visual Customization (`ResourcepacksPanel`, `ShaderPanel`)**  
  Easy installation and switching of resource packs and shaders.

- **Performance & System Monitoring (`FpsOverlay`, `SystemMonitor`)**  
  In-game overlays to track FPS, CPU/GPU usage, memory pressure, and detect performance bottlenecks in real time.

- **Server Management (`ServersPanel`, `MyServersPanel`)**  
  Quick-connect UI for local and remote servers (Arclight, Paper, Forge, Fabric).

- **Flexible Login (`LoginDialog`, `OfflineLoginDialog`)**  
  Support for online (premium) authentication and offline/local profiles for testing.

- **UI & Theming (`Theme`, `GalleryStrip`, `SplashScreen`)**  
  Modern components and configurable themes for a polished launcher experience.

## ⚙️ Installation & `install_java.bat` Status

The repository currently does not include an `install_java.bat`. This is not a blocker — you can install Java manually (recommended) or add a small script to automate environment checks and launcher startup.

### Java requirements
- For modern Fabric/Forge modpacks (Minecraft 1.20.1+), we recommend **Java 17** or **Java 21**.
- Make sure `JAVA_HOME` is set and the `java` executable is available on your PATH so the launcher can start game instances reliably.

### Example batch file (`install_java.bat`)
Save the following as `install_java.bat` or `start_launcher.bat` and adjust the startup command to match your build artifact (jar or main class):

```bat
@echo off
title Complex Launcher - Java Check

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

echo Starting Complex Launcher...
:: Replace the following lines with your actual startup command:
:: Example (runnable jar):
:: java -jar build/libs/complex-launcher.jar
:: Example (from compiled classes):
:: java -cp "out/production/classes;lib/*" com.complex.launcher.Main

pause
```

Notes:
- Replace the example startup lines with the actual path to your JAR or the classpath and main class.
- For distribution, consider bundling a platform-appropriate JRE or providing platform-specific installation instructions.

## 🚀 System Optimization Recommendations

- RAM allocation: On systems with 16 GB RAM, allocate **6–8 GB** to Minecraft for heavily-modded packs to improve stability.
- Monitoring: Use `SystemMonitor` and `FpsOverlay` to observe CPU/GPU usage, memory pressure, and frame rate while testing.
- Network: If using VPNs, tunnels, or proxies, monitor ping values to diagnose latency when connecting to remote servers.

## Contributing & Next Steps

If you'd like, I can:
- Add a polished `install_java.bat` and a cross-platform `install_java.sh`.
- Add a startup script that prefers a bundled JRE and falls back to system Java.
- Provide Gradle/Maven build instructions and an example runnable JAR configuration.
- Add usage examples, screenshots, release notes, and a short roadmap.

Tell me which items you'd like added and I will prepare and commit the scripts and README updates.

<img src="logo_1024.png" align="left" width="350">
 
 
 
 # 🚀 Complex Launcher — Advanced Minecraft Launcher

A next-generation Minecraft launcher focused on robust mod management, real-time performance monitoring, and a flexible, modern user interface.

## 🌟 Key Features

- **Mod & Modpack Management (`ModsPanel`, `ModpacksPanel`)**  
  Automatic mod downloads, dependency resolution, and one-click installation of curated modpacks.

- **Visual Customization (`ResourcepacksPanel`, `ShaderPanel`)**  
  Easy installation and switching of resource packs and shaders.

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

@echo off
setlocal enabledelayedexpansion
title ComplexLauncher - Java 21 Yukleyici
color 0B

echo =========================================
echo   ComplexLauncher - Java 21 Yukleyici
echo =========================================
echo.

:: Zaten Java 17+ var mi kontrol et
java -version >nul 2>&1
if %errorlevel% == 0 (
    for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set JVER=%%v
        set JVER=!JVER:"=!
        for /f "tokens=1 delims=." %%m in ("!JVER!") do set JMAJ=%%m
    )
    if !JMAJ! GEQ 17 (
        echo Java !JVER! zaten yuklu. Isleme gerek yok.
        echo.
        pause
        exit /b 0
    )
    echo Mevcut Java surumu !JVER! ^(eski^). Java 21 yuklenecek...
    echo.
)

:: Indirme URL - Eclipse Temurin 21 LTS (Windows x64)
set JDK_URL=https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.3%%2B9/OpenJDK21U-jdk_x64_windows_hotspot_21.0.3_9.msi
set JDK_MSI=%TEMP%\java21_installer.msi

echo Java 21 (Eclipse Temurin LTS) indiriliyor...
echo Kaynak: %JDK_URL%
echo.

:: PowerShell ile indir
powershell -NonInteractive -NoProfile -ExecutionPolicy Bypass -Command ^
    "try { $p = New-Object System.Net.WebClient; $p.DownloadFile('%JDK_URL%', '%JDK_MSI%'); Write-Host 'Indirme tamamlandi.' } catch { Write-Host ('HATA: ' + $_.Exception.Message); exit 1 }"

if %errorlevel% neq 0 (
    echo.
    echo HATA: Java indirilemedi. Internet baglantinizi kontrol edin.
    echo Manuel olarak su adresten indirin:
    echo https://adoptium.net/temurin/releases/?version=21
    pause
    exit /b 1
)

echo.
echo Java 21 yukleniyor... (UAC onay penceresi acilabilir)
msiexec /i "%JDK_MSI%" /qb ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome INSTALLDIR="C:\Program Files\Eclipse Adoptium\jdk-21" /L*v "%TEMP%\java21_install.log"

if %errorlevel% neq 0 (
    echo.
    echo HATA: Yukleme basarisiz. Log dosyasi: %TEMP%\java21_install.log
    pause
    exit /b 1
)

del /f /q "%JDK_MSI%" 2>nul

echo.
echo Java 21 basariyla yuklendi!
echo.

:: PATH'i guncelle
set "NEW_JAVA=C:\Program Files\Eclipse Adoptium\jdk-21\bin"
java -version
echo.
echo ComplexLauncher artik Java 21 ile calisacak.
echo.
pause


Notes:
- Replace the example startup lines with the actual path to your JAR or the classpath and main class.
- For distribution, consider bundling a platform-appropriate JRE or providing platform-specific installation instructions.

## 🚀 System Optimization Recommendations

- RAM allocation: On systems with 16 GB RAM, allocate **6–8 GB** to Minecraft for heavily-modded packs to improve stability.
- Network: If using VPNs, tunnels, or proxies, monitor ping values to diagnose latency when connecting to remote servers.

## Contributing & Next Steps

If you'd like, I can:
- Add a polished `install_java.bat` and a cross-platform `install_java.sh`.
- Add a startup script that prefers a bundled JRE and falls back to system Java.
- Provide Gradle/Maven build instructions and an example runnable JAR configuration.
- Add usage examples, screenshots, release notes, and a short roadmap.

Tell me which items you'd like added and I will prepare and commit the scripts and README updates.

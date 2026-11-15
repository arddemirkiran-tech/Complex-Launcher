<img src="logo_1024.png" align="left" width="350">
 
 
 
 # 🚀 Complex Launcher — Advanced Minecraft Launcher

A next-generation Minecraft launcher focused on robust mod management, real-time performance monitoring, and a flexible, modern user interface.

## ⬇️ Download (Latest: v63)

| File | Description |
|---|---|
| [`Complex-Launcher-Setup.exe`](https://github.com/arddemirkiran-tech/Complex-Launcher/releases/latest/download/Complex-Launcher-Setup.exe) | Windows installer — auto-downloads the latest release, creates shortcuts, checks Java |
| [`Complex-Launcher-v63-Windows.zip`](https://github.com/arddemirkiran-tech/Complex-Launcher/releases/latest/download/Complex-Launcher-v63-Windows.zip) | Portable Windows build (bundled runtime) |
| [`Complex-Launcher-v63-Linux.zip`](https://github.com/arddemirkiran-tech/Complex-Launcher/releases/latest/download/Complex-Launcher-v63-Linux.zip) | Portable Linux build |

Website: **https://complexlauncher.gt.tc**

## What's new in v63

- **Play time was never recorded.** Time was only saved when the game closed; if the launcher was closed while the game was still running (the common case), `onGameExit` never fired and the time was lost, so instance cards stayed on "Never played". Now a `.play_session` marker is written when the game starts, recovered on next launch (covering crashes and closing the launcher mid-game), and partial time is flushed when the launcher closes. The recorded value uses the larger of the measured exit time and the marker-based time.
- **Short play times showed as "0h 46m".** Instance cards now show "46m" for sub-hour times.

## What's new in v62

- **The Modpacks page opened empty.** It defaulted to the CurseForge source, but the build ships no CurseForge API key, so every query failed and the list stayed blank with no message. The page now starts on Modrinth (no key needed), preloads popular modpacks on open, and falls back to Modrinth with a clear note if CurseForge is selected without a key.
- **Modpack search had no live search or ESC reset** while the mod, shader and resource pack pages had both; typing now searches after 450 ms, ESC clears, and switching source immediately loads that source's popular list.
- **List descriptions were cut mid-sentence with no ellipsis.** A new `EllipsisLabel` truncates to the row's real width, which also removed the pointless horizontal scrollbar under the result lists.
- **Folder paths still broke mid-word.** The v61 zero-width-space trick did not work inside a Swing text area (`...\shaderpack` + `s`). The info block is now built from labels and long paths are middle-truncated on one line, so the folder name stays readable.
- **Turkish characters:** "Seçili Modpack'i Kur", "Yerel İçe Aktar", the local-import hint and the source toggle tooltip now use proper Turkish characters and have an English variant.

## What's new in v61

- **The installed-side footer (info + Remove / Open Folder) was missing on first paint.** On the Shaders and Resource Packs pages the right panel lives in a `JLayeredPane` whose children were only given bounds in a resize listener, so the footer did not lay out until a mouse move forced a repaint and the panel appeared to shift. The layered pane now lays its children out on every layout pass.
- **Info text set after construction was never revalidated**, so the block kept its stale height; `revalidate()` is called after the text is filled and a duplicated `add(infoPanel)` in the shader panel was removed.
- **Long folder paths wrapped mid-word** (`...\shaderpacks` rendered as `shaderpac / ks`); the new `TextWrap.breakPath` helper inserts zero-width spaces after path separators so lines break at folder boundaries.
- **Typo fix:** "Otomatik kurulacak shader ayarlanmamis" -> "ayarlanmamış" (shader and resource pack panels).

## What's new in v60

- **Same tooltip bug fixed on the other three list pages.** Shaders, Resource Packs and Modpacks search lists still called `setToolTipText(...)` on every `mouseMoved`, which made Swing rebuild the popup and left overlapping tooltips that looked like a duplicating list. The listeners are gone; tooltips now come from Swing's own `getToolTipText(MouseEvent)` via the shared `ListTips` helper (shader and resource pack results also got title + description tooltips).
- **Enable/disable on a mod was reporting a state that never happened.** A name already ending in `.disabled` got a second `.disabled` appended, so nothing was renamed while the registry still recorded "disabled" - the mod kept loading in game. A failed `renameTo` (file locked by a running game) was ignored the same way. The name is normalised now, `Files.move` is used as a fallback, and failures show a "file is in use" warning instead of a fake state.
- **ZIP import path check allowed sibling folders.** `safeResolve` only compared `startsWith(basePath)`, so `/instances/foo` also matched `/instances/foo-bar` and an entry like `../foo-bar/x` could write into another instance. Canonical path plus separator check is used for instance import and modpack installs.
- **Update version comparison could crash.** A tag like `1..2` threw `NumberFormatException`; version parts are parsed safely now.

## What's new in v59

- **Mods page no longer glitches while moving the mouse.** The installed/search lists called `setToolTipText(...)` on every `mouseMoved` event; Swing's `ToolTipManager` rebuilds the popup on each of those calls, so with mods present the pointer sweeping across rows left overlapping/pinned tooltip windows that looked like the list "duplicating and breaking" (with no mods there are no rows, which is why it only happened with mods installed). Tooltips are now produced by Swing itself through `JList.getToolTipText(MouseEvent)` (new `ListTips` helper) - no programmatic show/hide, nothing repainted while the mouse moves.
- **Fixed the silent "launcher does not open" case.** The single-instance check showed the "already running" message via `invokeLater` and then called `System.exit(0)` before the EDT could display it, so the app just vanished with no window and no message. The message is now shown before exiting.
- **Stale/inherited `launcher.lock` no longer blocks startup.** The launcher lock file could stay held after the game/server it had started, making the launcher refuse to start until the game was closed. If the lock cannot be taken and no other launcher process is actually running, the lock file is recreated and startup continues; a real second launcher still gets the "already running" notice.

## What's new in v58

- **Instances tab no longer duplicates cards.** The grid was rebuilt from background threads (modpack install, max-performance, instance create/delete); mutating Swing off the EDT left ghost cards stacked on top of each other. Refreshes now always run on the EDT and bursts are coalesced into one rebuild, and names are de-duplicated. The hover animation also allocated a new `Timer` on every mouse-enter and never stopped timers of replaced cards (invisible cards repainting 60x/s); each card now reuses one timer.
- **Mods tab: no duplicate rows.** Entries with the same jar name (left over from older installs) and rows whose jar no longer exists are filtered out; the selection is restored by file name instead of being reset on every refresh, the list is not rebuilt when the content did not change, and the "no mods yet" placeholder can no longer be toggled/removed as if it were a mod.
- **Forge/NeoForge metadata is checked before launch.** Only Fabric's `fabric.mod.json` used to be read; now `META-INF/mods.toml` and `META-INF/neoforge.mods.toml` are parsed for mod id, version and **mandatory dependencies**, with Maven version ranges (`[1.20,1.21)`, `[1.20,)`, `(,1.21]`, `[1.20.1]`, multiple ranges, MC-prefixed versions like `1.20.1-14.4.4`, unresolvable `${file.jarVersion}`). Wrong-loader jars (Fabric jar in a Forge instance and vice versa) are reported too. The check now runs on **every modded launch** instead of only when Maximum Performance is on, still cached by `mods/.compat-ready`, and still only disables jars (`.disabled`) instead of deleting them.

## What's new in v57

- **Cut-off texts fixed.** When a panel was narrow, labels were clipped and Swing appended "..." to them. Fixed:
  - the **Maximum Performance panel** was rebuilt: title left, the ON/OFF switch now sized from its own label width (so "KAPALI" fits), the description wraps to the panel width instead of fixed line breaks, the status line ("On - 22/48 mods installed") sits on its own full-width row, and the Reinstall / Remove mods / JVM flags buttons wrap instead of overflowing;
  - **server card buttons** ("Play / Mods / Edit / Delete") are sized from their text instead of a fixed 50-70 px, so "Edit"/"DuzEnle"-length labels no longer ellipsize;
  - the **local import description** on the Modpacks tab wraps to its card instead of using a fixed-width centered HTML block.
- New shared `WrapLabel` component so long descriptions wrap instead of being clipped.

## 🆕 What's new in v56

- **~50 ms launch preparation on every loader** — measured on 1.20.1: Forge 49 ms, Fabric 58 ms (version JSON 31-33 ms, Java check 5-6 ms, loader profile 5-15 ms, natives 3-4 ms, assets 2-3 ms). Three repeated costs were removed:
  - the library marker is now **per loader** (`.libs-ready-vanilla` / `-fabric` / `-forge`), so switching loaders no longer re-hashes ~60 MB / 79 JARs every time (measured 108 ms);
  - the 23 MB client JAR is no longer SHA-1 hashed on every launch (measured 16-67 ms) — a size+mtime fingerprint in `.client-ready` covers it;
  - the mod compatibility scan behind Maximum Performance is cached in `mods/.compat-ready` and skipped entirely while the mod set is unchanged (22 jars: 51 ms cold, 8 ms warm; seconds with 100+ mods).
- **Per-stage timing logs** — the console now prints `[Hız] Sürüm bilgisi: 31 ms`, `[Hız] Loader (Forge): 5 ms`, `[Hız] Kütüphaneler`, `[Hız] Natives`, `[Hız] Kaynaklar` and the total, so the prep time of each loader is visible instead of guessed.
- Version bump to **v56** across the launcher and update checks.

## 🆕 What's new in v55

- **Maximum Performance gets real controls** — the settings panel now shows a live status line (`On - 42/48 mods installed`) plus three buttons: **Reinstall** (rebuilds the performance set for this instance), **Remove mods** (deletes the jars this mode installed, registry entry included) and **JVM flags** (shows the GC/JIT flags it adds for the current RAM and Java version). Installed jars are tracked in `.maxperf-set.json`, so mods you installed yourself are never touched.
- **Download percentage animation** — turning the mode on fades a glass overlay over the window with a large percentage, an animated progress bar and the mod being installed (`[7/48] FerriteCore`). It eases to 100 % and fades out on its own when the install finishes.
- **Auto-off on instance switch** — the mode is now per instance. Switching to another instance (list, quick switch, rename, delete) turns it back to **OFF**, saves the setting and cancels a running install, so one instance's performance set never leaks into another.
- **Remaining `?` glyphs fixed** — emoji and symbols the bundled faces cannot draw (feedback categories, crash dialog arrow, console warning) were removed, and Modrinth mod/shader/resource-pack titles, descriptions and server MOTDs are now stripped of characters the fonts cannot render while Turkish characters are preserved.
- Version bump to **v55** across the launcher and update checks.

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

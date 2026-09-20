# Complex Launcher v44 - Source (com.lubv package)

This archive contains the `com.lubv.*` Java source for Complex Launcher,
reconstructed from the shipped v26 jar via CFR decompilation and then
hand-fixed, extended, and verified to compile cleanly with `--release 17`.

## What's in here

```
src/main/java/com/lubv/
├── agent/          Java agent used for in-process patches (namechanger etc.)
├── launcher/
│   ├── core/        HTTP client, instance management, settings, l10n,
│   │                 session/account logic, server-list-ping client
│   ├── game/         Java runtime detection, performance-flag tuning,
│   │                 the actual game launch process
│   ├── mods/         Modrinth/CurseForge API clients, mod install/update logic
│   ├── update/       Self-update mechanism (checks GitHub releases)
│   └── ui/           All Swing UI: MainWindow, ModsPanel, ShaderPanel,
│                      ResourcepacksPanel, GalleryStrip, Theme, UiFx, etc.
```

This is **only** the project's own code. Third-party libraries (Gson,
FlatLaf, TwelveMonkeys) are not included here - they're pulled in via
Maven/the classpath at build time and are also already present, compiled,
inside `Complex-Launcher.jar` in the release ZIPs.

## What changed from the original v26 code

### Rendering bugs
- **Fixed a duplicate/ghost widget rendering bug**: several `UpdatePanel`
  status methods (`showUpdateAvailable`, `showUpToDate`, `showChecking`,
  etc.) changed a component's text/visibility without calling
  `revalidate()`/`repaint()`. Without those calls Swing doesn't
  recompute layout, which could leave a stale copy of the update button
  visually stuck on screen alongside the new one.
- **Fixed custom instance icons causing render stutter/corruption**:
  `InstanceIcons.render()` used to call `ImageIO.read()` from disk on
  *every single paintComponent call* - since instance cards repaint at
  ~60fps during hover animations, this meant disk I/O dozens of times
  per second, blocking the UI thread and causing render frames to pile
  up (visually looking like duplicated/misplaced windows). Custom PNG
  icons are now cached by file path + last-modified time, so the file
  is only re-read when it actually changes.
- **Fixed the JVM-arguments field truncating/breaking on long text**
  (two-part fix): the field was originally a single-line `JTextField`,
  which was first changed to a wrapping multi-line `JTextArea`. That
  alone wasn't enough - the wrapping `JScrollPane` had its
  `preferredSize` width mistakenly set to `0`, and since `BoxLayout`
  sizes components from `preferredSize` (not `maximumSize`), the field
  still rendered at almost no width. Long flag lists (e.g. the ~20
  flags Maximum Performance mode auto-adds) would visually break the
  sidebar. The scroll pane's preferred width is now set to 260px to
  match the sidebar's usable inner width. This was verified visually
  by scripting a real click-and-type test against the running app
  (see screenshots during development) - the field now wraps text
  correctly and scrolls vertically instead of overflowing.

### Translation
- **Fixed in-place translation not working**: `HttpUtil`'s `open()`
  helper was sending a hard-coded `Referer: https://modrinth.com/`
  header on *every* outgoing request, including calls to Google
  Translate's endpoint. That unexpected referer caused Translate to
  silently reject/mishandle the request, and since `translateText()`
  caught all exceptions and returned the original text unchanged, the
  failure was invisible - it just looked like the button did nothing.
  The referer is now only sent to Modrinth/CurseForge hosts.

### Mods, shaders, resource packs
- Mod thumbnails / gallery images no longer get stuck as broken
  placeholders - `HttpUtil` sends realistic headers and retries with a
  cooldown instead of caching a permanent failure.
- The mod details panel opens as a full-size overlay over the
  installed-mods list, only via the dedicated "mod info" button
  (selecting a list item no longer force-opens it).
- The **same overlay-detail pattern was added to the Shaders and
  Resource Packs tabs** - double-click an installed item to see its
  full Modrinth description, project link, and an in-place translate
  button (no browser opens).
- Mod dependency installation was fixed (`ModManager.installProject`
  now resolves the full dependency tree instead of just the main file;
  a silent error in `loadRegistry` that could wipe the visible mod list
  was also fixed).
- Search results default to Modrinth instead of CurseForge.
- CurseForge support now follows the same architecture as Prism
  Launcher/HMCL: `CurseForgeApi.DEFAULT_API_KEY` can hold an
  app-owned key so users don't need to supply their own (currently
  empty - see "CurseForge API key" below).
- Downloads use a 256KB buffer (up from 8KB) and download files over
  8MB in 4 parallel Range-request chunks when the server supports it,
  falling back to a single stream automatically if not.

### Auto-install / "Maximum Performance"
- You can add your own mods/shaders/resource packs to the auto-install
  list from Settings. Adding an item now opens a **real search dialog**
  (same look as the Mods/Shaders search screens, with results list and
  description preview) instead of requiring you to type an exact
  Modrinth slug.
- A "Maximum Performance Mode" toggle in Settings picks GC/JIT flags
  suited to the system's RAM (ZGC for 7GB+, tuned G1GC below that) and
  installs 15 well-known performance mods (Sodium, Lithium, Entity
  Culling, FerriteCore, Starlight, C2ME, etc.) for the current instance.

### Instances
- Instances can be renamed (right-click menu).
- Instance icons (grass/diamond/nether/end/ocean/cave/forest/desert/
  snow/custom) are now actually rendered on the instance card - they
  used to be saved but never drawn.
- Each instance can be assigned its own account (name/skin), overriding
  the global active account only for that instance.
- Play-time tracking now attributes time to the instance that was
  actually launched, not whichever instance happens to be selected in
  the UI when the game exits.
- Instance card gradients are now derived from the active theme's
  accent color (`Theme.ACCENT`) instead of 8 hard-coded colors that
  never changed no matter which theme was selected - this was the main
  reason themes felt "fake."

### Cat easter egg
A hidden cat easter egg (click the logo): completely rewritten as a
60 FPS, delta-time based animation with 5 random variants (fishing,
ramp jump, dive, double-leap-miss, chase), a real splash/ripple
particle system, correctly-grounded shadow, and everything scaled 2x
from the original size. Further visually refined with: a soft
body-bounce while running, a smoother two-segment tail curve, subtle
top-lit highlight shading on the body and head for a rounder/3D look,
bright green cat-like eyes with a catchlight instead of flat black
dots, and a small nose/mouth.

### Other
- Server list entries now fetch and display the server's real favicon
  via a proper Minecraft Server List Ping client (`ServerPing.java`).
- Crash-log analysis recognizes ~25 categories of common Minecraft
  crashes (world corruption, port conflicts, missing classes, stack
  overflows, GPU/VRAM issues, datapack JSON errors, etc.) instead of 6,
  each with an actionable bilingual message.
- A "Delete Launcher" danger-zone button in Settings removes all
  instances/mods/accounts/settings after a two-step confirmation.
- A large number of CFR decompiler artifacts (variable name collisions
  across nested lambdas, type erosion from `Object` casts, broken
  for-each/try-with-resources reconstruction) were fixed by hand across
  `GameLauncher`, `ModpacksPanel`, `MyServersPanel` and others so the
  code compiles cleanly - several of these files did not compile at all
  as originally decompiled, meaning their logic had never actually been
  buildable from source before.

## CurseForge API key

There is no way to access CurseForge's API without a key - Prism
Launcher and HMCL don't either; they embed their own developer key so
users don't have to supply one. To do the same here:

1. Get a free API key at https://console.curseforge.com with your own
   account (this can't be done on your behalf - it's tied to your
   account/app registration).
2. Paste it into `CurseForgeApi.DEFAULT_API_KEY` (currently `""`).

Once set, CurseForge search/install works for every user without them
entering a key, while still letting a user override it with their own
key in Settings if they want to.

## Building

You'll need a JDK (17 recommended, since that's the bundled runtime
target) and the compiled dependency classes on your classpath. The
simplest way to get those is to point `javac` at the existing
`Complex-Launcher.jar` from the release ZIP, which already contains
Gson, FlatLaf and TwelveMonkeys:

```bash
javac -encoding UTF-8 --release 17 \
  -d out \
  -cp Complex-Launcher.jar \
  $(find src/main/java/com/lubv -name '*.java')
```

Then merge the resulting `.class` files back into a copy of
`Complex-Launcher.jar` (or build a full Maven project with the three
dependencies declared - `pom.xml` is not included here since it wasn't
part of the original extracted source).

## Known limitations

- `com/lubv/agent/AgentMain.java` references a few classes
  (`GameProfileTransformer`, `HotkeyListener`, `IpcClient`) that were
  not present in the shipped jar and are therefore not included here.
  This file is unrelated to the launcher UI and is not required to
  build or run the main application.

# Complex Launcher v47 - Source (com.lubv package)

This archive contains the `com.lubv.*` Java source for Complex Launcher,
reconstructed from the shipped v26 jar via CFR decompilation and then
hand-fixed, extended, and verified to compile cleanly with `--release 17`.

This is v47 of the source archive. The v46 addition (``FeedbackSection.java``
and ``SuggestionClient.java``) and the v47 rewrite of ``ui/DesktopShortcut.java``
are included here.

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
│                      ResourcepacksPanel, GalleryStrip, Theme, UiFx,
│                      FeedbackSection (v46) etc.
```

`core/SuggestionClient.java` (v46) posts the launcher's feedback to the
website's `api.php`; see "What's new in v46" below.

This is **only** the project's own code. Third-party libraries (Gson,
FlatLaf, TwelveMonkeys) are not included here - they're pulled in via
Maven/the classpath at build time and are also already present, compiled,
inside `Complex-Launcher.jar` in the release ZIPs.

## What's new in v47

### The desktop shortcut: always created, always working

Up to v46 the desktop shortcut was a **one-shot**: it was created once and
then never looked at again (a `shortcut_created_v2` marker file suppressed
all later attempts). If the launcher was later updated or moved, the
shortcut kept pointing at the old location and simply stopped working - and
if that first attempt failed (other JVM, locked-down PowerShell, a desktop
path with non-ASCII characters), the user never got one at all.

`ui/DesktopShortcut.java` was rewritten so that the shortcut is re-checked
and repaired on **every start and after every update**:

- **It always points at something that actually runs.** In order of
  preference: the real `Complex-Launcher.exe` if the package has one,
  otherwise the bundled `runtime\bin\javaw.exe -jar "…\Complex-Launcher.jar"`,
  otherwise the JVM the launcher itself is running on. It no longer depends
  on `.jar` file association at all.
- **It repairs itself.** If the link is missing, points at a file that no
  longer exists, or has the wrong target / arguments / working folder, it is
  rewritten in place. If the user deletes it, it comes back.
- **It cleans up the old ones.** Stale `Complex Launcher…` links are removed:
  broken targets, the un-suffixed legacy variant, and version-named leftovers
  such as `Complex Launcher v45.lnk` (these are deleted even when their target
  is still valid, which is exactly the case after an in-place update).
  Shortcuts that are not ours are never touched.
- **After an update the link follows the launcher.** Because every start
  re-aligns the link with wherever the launcher now lives, an update that ends
  up in a different folder simply re-points the shortcut there.
- **It finds the real desktop**, including OneDrive-redirected ones
  (`[Environment]::GetFolderPath('Desktop')`), and falls back to the
  `User Shell Folders` registry value, tried in several encodings, when
  PowerShell can't run at all.
- **It survives a broken or absent PowerShell** through a `cscript`/VBS
  fallback. That VBS file is now written as **UTF-16LE with a BOM** - cscript
  reads an unmarked file as ANSI, so on a path like `…\Yeni klasör (7)\…`
  the fallback used to fail silently and produce no shortcut.
- **It never mis-reads a path.** PowerShell output is forced to UTF-8, and the
  link is only rewritten when the current target could be read back cleanly,
  so a garbled read can never trigger a wrong rewrite.
- The Linux `.desktop` and macOS `.command` equivalents get the same
  check/repair/cleanup treatment, and the Settings screen's shortcut button
  still forces a fresh write - now reporting the actual error instead of
  failing silently.

The behaviour above was verified on Windows with a probe harness that drives
the real production class through 30 checks: shortcut creation, running the
shortcut's own command line (bundled `javaw.exe` + `-jar`), stale/legacy link
cleanup, leaving foreign shortcuts alone, re-creating a deleted link,
re-pointing the link when the launcher has moved (simulated update),
no unnecessary rewrites, and the cscript fallback on a path containing spaces,
parentheses and non-ASCII characters.

## What's new in v46

### Feedback & suggestions box (launcher → website)
**Settings → Feedback & suggestions** now contains a suggestion box: a
category picker (idea / bug report / feature request / mod request /
other), an optional name, a 1000-character message with a live counter, a
status line and a **View on the site** button.

- `ui/FeedbackSection.java` is the whole card. It is language-aware: every
  label comes from `L10n`, so it reads Turkish or English depending on the
  selected language, and the chosen language is sent along with the post.
- `core/SuggestionClient.java` does the HTTP work
  (`POST api.php?action=submit`). Category keys are the exact strings the
  website uses, so launcher and website posts land in the same public list.
- Submitting happens on a background thread (`suggestion-submit`); the
  button is disabled while sending and the result is reported in the status
  line: success, `rate_limited`, `text_too_short`, server rejection and
  “site unreachable” each have their own localised message.
- The endpoint is `https://complexlauncher.gt.tc/api.php` by default and can
  be swapped for development/testing with
  `-Dcomplex.suggest.api=http://127.0.0.1:8099/api.php` or the
  `COMPLEX_SUGGEST_API` environment variable.

### The website side (`site/api.php`)
The public website now has a **Suggestions** section with the same form and
a live list of everything posted (from the launcher or from the browser).
It is backed by a single PHP file that needs **no database**:

| Request | Result |
| --- | --- |
| `GET api.php?action=list&limit=60` | newest-first list (max 200) |
| `POST api.php?action=submit` | stores a post, returns `{"ok":true,"id":N}` |
| `POST api.php?action=delete&key=…&id=…` | moderation, needs `ADMIN_KEY` |

Posts are kept in `data/suggestions.json` (newest 400), rate-limited per
salted IP hash (one post per 20 s, 12 per hour), protected by a honeypot
field, and the `data/` folder is denied by `.htaccess`. The message text is
stored raw and always rendered with `textContent`, so nothing is parsed as
HTML anywhere.

Deployment: copy `api.php` next to `index.html` on the PHP host (InfinityFree:
`htdocs/`), make sure the folder is writable, and set `ADMIN_KEY` in
`api.php` if you want the delete endpoint. Long bug reports with logs still
belong in the GitHub issue tracker; the suggestion box is for short ideas.

## What's new in v45

### Mods: "Change Version" (`Sürüm Değiştir…`)
Right-clicking an installed mod now offers **Change Version…** in the
context menu (`ModsPanel.showInstalledMenu`). It installs another build of
that *same mod* — the Minecraft version and the loader do not change.

- `ModsPanel.changeSelectedVersion` fetches the project's version list from
  Modrinth for the **current loader + MC version** on a background thread.
- `ModsPanel.showVersionPicker` opens a modal picker (`VersionListRenderer`)
  sorted newest-first, showing version number, release type, date and
  supported MC versions, with the **currently installed** build marked and
  pre-selected.
- `ModsPanel.installChosenVersion` downloads the chosen version through
  `ModManager.installWithDependencies` (SHA-1 verified download, dependency
  chain, registry update), then deletes the **old jar** (and its `.disabled`
  counterpart) so two versions of the same mod can never sit in `mods/`
  together. It reuses the existing cancel button / progress bar.
- Picking the version that is already installed is a no-op (no re-download),
  and the menu entry is disabled for mods that have no Modrinth project id
  (CurseForge and local JAR installs), since their registry entries carry no
  resolvable version list.

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

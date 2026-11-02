# NotiSprite — Milestones & Task Board

Derived from [`design.md`](./design.md). Work top-down; a milestone is done only when every task is checked **and** its "Done when" criteria hold.

---

## M0 — Project Scaffolding ✅

**Goal:** A buildable Android app skeleton matching the design's package layout.

- [x] Create Android project (Kotlin, minSdk 26, targetSdk 36 — capped at 36 because AGP 9.0.1 does not support compileSdk 37)
- [x] Set up package root `com.notisprite.app` with `views/`, `services/`, `managers/`, `data/`, `ui/`
- [x] Add dependencies: AndroidX Core/AppCompat, Material, Palette, coroutines, DataStore (or Room), Timber
- [x] Declare permissions in `AndroidManifest.xml`: `SYSTEM_ALERT_WINDOW`, `WAKE_LOCK`
- [x] Register `NotificationListenerService` (`services.NotiSpriteListener`) with `BIND_NOTIFICATION_LISTENER_SERVICE` permission + intent filter
- [x] Add Android Lint baseline + ktlint checks wired into `check`, and a GitHub Actions CI workflow

**Done when:** `./gradlew assembleDebug` succeeds and the app launches to an empty screen. ✅ Verified locally: `assembleDebug`, `ktlintCheck` and `lint` all pass.

**M0 notes:**
- Toolchain: Gradle 9.2.1, AGP 9.0.1 (`android.builtInKotlin=false`), Kotlin 2.2.20, Java 17 target.
- `androidx.core` is pinned to 1.17.0; 1.19.x requires AGP 9.1+ and compileSdk 37.
- The listener service uses `android:exported="false"` (the design's `true` is unnecessary and would expose the service).
- The M0 landing screen reports notification-access and overlay-access state with settings deep links; the listener only logs traffic until M2.
- Detekt is deferred; Android Lint + ktlint cover static checks for now.

---

## M1 — Pixel-Art Sprite Engine

**Goal:** Crisp pixel sprites drawn on a true-black canvas.

- [ ] Implement `PixelSpriteView` per design §5.1 (`isAntiAlias = false`, `filterBitmap = false`)
- [ ] Make sprite size/scale a parameter instead of the hard-coded `96` in `onDraw`
- [ ] Add nearest-neighbor scaling that respects 16x16 and 24x24 source sizes
- [ ] Load sprites from `assets/sprites/` and decode without pre-scaling (no `Bitmap.createScaledBitmap` blur)
- [ ] Guarantee `#000000` background fill; verify no translucent/alpha-composited background
- [ ] Add a preview/debug screen that renders every bundled sprite at 1x/4x

**Done when:** A debug screen shows all sprites with hard pixel edges and pure black surroundings.

---

## M2 — Notification Listener Service

**Goal:** Capture notifications reliably and normalize them into an internal model.

- [ ] Implement `NotiSpriteListener : NotificationListenerService`
- [ ] Handle `onNotificationPosted` / `onNotificationRemoved` and active-notification bookkeeping
- [ ] Ignore ongoing/group-summary/own-app notifications and empty-content notifications
- [ ] Add package allow/deny filtering
- [ ] Define `SpriteNotification` model (packageName, key, icon bitmap, post time)
- [ ] Extract the notifying app's icon for fallback/palette use
- [ ] Expose events through a `SharedFlow` to the overlay layer
- [ ] Add listener-permission status check + "grant access" deep link to system settings

**Done when:** Posting/removing notifications from a test app emits correctly filtered events with no duplicates.

---

## M3 — Overlay & Screen-Off Presentation

**Goal:** Show sprites over a dark/locked screen without waking the whole UI.

- [ ] Implement `OverlayManager` using `WindowManager` + view attach/detach lifecycle
- [ ] **Validate window type:** design says `TYPE_APPLICATION`, which only attaches to an activity — switch to `TYPE_APPLICATION_OVERLAY` (API 26+) or a `FLAG_SHOW_WHEN_LOCKED` full-screen activity, and document the decision
- [ ] Declare/request the "draw over other apps" permission (`SYSTEM_ALERT_WINDOW`)
- [ ] Add overlay view flags: not focusable, not touchable (until Phase 3), full-screen, no dim
- [ ] Acquire/release a partial `WAKE_LOCK` (or short wake + timeout) around alert events per design §4.1
- [ ] Ensure overlay auto-dismisses after a configurable timeout or on notification removal
- [ ] Handle screen-on/off and user-unlock broadcasts so the overlay never leaks

**Done when:** A notification shows a sprite over a black screen and the overlay is fully removed afterwards.

---

## M4 — Burn-In & Sensor Protections

**Goal:** Protect the panel and avoid wasting battery in pockets.

- [ ] Implement `OverlayProtectionManager` per design §5.2
- [ ] Apply random ±5 px offset every 60 s (clamp inside screen bounds; keep offset in a safe range)
- [ ] Add proximity sensor listener gating rendering on/off
- [ ] Register/unregister sensors in sync with overlay lifecycle (no leaks)
- [ ] Add accelerated animation driver (e.g. `Choreographer` or 60 s frame step) instead of manual `stepBreathingAnimation()` polling
- [ ] Verify CPU/battery cost with a 10-minute screen-off soak test

**Done when:** Sprite position drifts over time, never leaves the screen, and hides immediately when the sensor is covered.

---

## M5 — Mapping, Stacking & Dynamic Palette

**Goal:** Right sprite for the right app, intelligently tinted.

- [ ] Implement `SpriteMapper` (package name → sprite asset) with a default fallback sprite
- [ ] Persist user overrides for sprite mappings
- [ ] Implement multi-notification queue with two display strategies: cycle every N seconds, or side-by-side micro-sprites
- [ ] Implement `Palette`-based extraction from the app icon to tint monochrome fallback sprites
- [ ] Cache tinted bitmaps (avoid re-extraction per frame)
- [ ] Debounce rapid notification bursts so sprites don't flicker

**Done when:** Two different apps show two different sprites, and stacking behaves correctly for 3+ pending notifications.

---

## M6 — Animation Modes & Settings UI

**Goal:** User-facing control over look and behavior.

- [ ] Implement animation modes: Breathing (20%→100% alpha), Pixel Pulse (scale), Static retro blink
- [ ] Add speed/intensity controls and persist the selected mode
- [ ] Build settings screen: enable/disable, sprite size, animation mode, alert duration, cycle interval, mapping editor
- [ ] Build onboarding with clear permission steps for notification access + overlay access
- [ ] Add a "Test alert" button that renders a sprite without waiting for a real notification

**Done when:** Changing each setting visibly changes the overlay on the next alert and survives app restart.

---

## M7 — Phase 2: Custom Sprite Pack Editor (from design §6)

**Goal:** Let users draw their own 16x16 sprites in-app.

- [ ] Build a 16x16 pixel grid editor with a retro palette
- [ ] Support zoom/pan and per-pixel color picking (eyedropper)
- [ ] Add save/load of custom sprite packs, plus export/import (JSON or PNG sheet)
- [ ] Render user sprites through the same `PixelSpriteView` path
- [ ] Add deletion/renaming and protect bundled sprites from edits

**Done when:** A user can draw, save, assign, and display a custom sprite without restarting the app.

---

## M8 — Phase 3: Tap-to-Preview (from design §6)

**Goal:** Inspect notifications on the dark canvas without waking the full display.

- [ ] Make the overlay touchable only while an alert is visible
- [ ] On tap, show a compact notification card (app, title, snippet) over black
- [ ] Handle multiple pending notifications with swipe/step navigation
- [ ] Keep the screen-off illusion: no status bar, no dim, minimal brightness
- [ ] Dismiss or open the app on explicit action, then cleanly tear down the overlay

**Done when:** Tapping a sprite reveals the notification summary and can dismiss it without unlocking the phone.

---

## Cross-Cutting: Quality & Release

- [ ] Unit tests for `SpriteMapper`, notification filtering, and cycle logic
- [ ] Instrumented test for overlay attach/detach and sensor gating
- [ ] Battery/AMOLED soak test report (screen-off, 30+ min)
- [ ] Accessibility & battery-optimization docs in onboarding
- [ ] Play Store listing assets, privacy policy (notification access justification)
- [ ] Tag `v1.0.0` MVP release (M0–M6)

---

## Open Questions / Risks to Validate

1. **Overlay window type** — `TYPE_APPLICATION` in the design cannot draw over other apps; confirm `TYPE_APPLICATION_OVERLAY` vs. lock-screen activity (§4, §5.2).
2. **Screen-off rendering** — Android may not render overlays while the display is truly off; decide between short wake-ups and ambient/AOD-style behavior.
3. **`filterBitmap = false`** — design sets it inside `Paint().apply {}` but it is a `Paint` property in modern APIs; verify on target SDKs.
4. **Burn-in offset drift** — a pure random walk can wander; clamp cumulative offset to ±5 px around center.
5. **Animation cost** — one `invalidate()` per alpha step at full-screen size may be expensive; consider a fixed-size window.

# NotiSprite

Pixel-art notification sprites for AMOLED screens. See [design.md](./design.md) for the
technical design and [todo.md](./todo.md) for the milestone plan.

## Status

M0 is complete. The app builds, launches, and shows the state of the two
system permissions it needs (notification access and overlay access). Overlay
rendering, the sprite engine, and the protections arrive in later milestones.

## Requirements

- JDK 17+
- Android SDK with platform 36 and build-tools 36.0.0
- `local.properties` pointing at your SDK (`sdk.dir=...`)

## Build and verify

```bash
./gradlew assembleDebug     # debug APK
./gradlew lint              # Android Lint (abort on error)
./gradlew testDebugUnitTest # unit tests
```

CI runs the same three tasks on every push and pull request
(`.github/workflows/android.yml`).

## Package layout

```
com.notisprite.app
├── NotiSpriteApp.kt        application entry point
├── data/                   persistence (settings, sprite mappings)  [M5/M6]
├── managers/               overlay + protection managers           [M3/M4]
├── services/               notification listener service           [M2]
├── ui/                     activities and screens
└── views/                  custom views (PixelSpriteView)          [M1]
```

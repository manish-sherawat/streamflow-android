# StreamFlow — Android (Kotlin + Jetpack Compose)

MVP scaffold for the StreamFlow OTT app, built per `PRD.md` / `TRD.md` / `Implementation_Plan.md` / `design.md`. This is Phase 0–1 of that plan: a compiling project structure with the core screens wired to a mock data layer so the UI and ExoPlayer pipeline are exercisable immediately, with a clean seam to swap in Firestore and later the Node.js backend.

## What's implemented

| Area | Status |
|---|---|
| Project scaffold (Gradle, Hilt, Compose, Media3, Coil) | ✅ |
| Design system (`ui/theme/*`) matching `design.md` tokens | ✅ |
| Home screen — hero banner + rails | ✅ |
| Title Detail screen — pills, actors, episodes, More Like This | ✅ |
| Player screen — ExoPlayer + HLS adaptive bitrate + resume sync | ✅ |
| Search — debounced (300ms) | ✅ |
| Watchlist (My List) | ✅ |
| Navigation — floating glass bottom nav, Compose Navigation | ✅ |
| Data layer | Mock repository only — see below |
| Auth (Firebase) | Not started — Phase 1.5 |
| Firestore-backed repository | Not started — Phase 1.5 |
| DRM / downloads | Not started — Phase 5 |

## Data layer: mock-first by design

`CatalogRepository` is an interface (`data/repository/CatalogRepository.kt`). The only implementation right now is `MockCatalogRepository`, which returns static sample titles pointed at **real public HLS test streams** (Mux and Apple's bipbop test streams), so you can build and immediately verify:
- Rails render and scroll
- Tapping a poster navigates to Detail
- Hitting Play actually starts adaptive-bitrate HLS playback in ExoPlayer
- Progress/resume, watchlist, and search all work end-to-end against in-memory state

**To move to Phase 1.5:** implement `FirestoreCatalogRepository : CatalogRepository` following the Firestore schema in `TRD.md` §4, then swap the binding in `di/AppModule.kt`. No screen or ViewModel code needs to change — they all depend on the interface.

## Opening the project

1. Open in **Android Studio Koala (2024.1)** or newer.
2. If Android Studio doesn't auto-generate the Gradle wrapper jar/scripts, run `gradle wrapper` once from the project root with a local Gradle 8.7+ install, or just let Android Studio's "Sync Project" regenerate them — either works, they're gitignored on purpose since the binary wrapper jar isn't meaningful to hand-author.
3. **Firebase:** the `app/build.gradle.kts` applies the `google-services` plugin, which requires `app/google-services.json`. A placeholder with setup steps is at `app/google-services.json.SAMPLE`. Until you add a real one:
   - Either create a Firebase project and drop in the real `google-services.json`, or
   - Strip the `com.google.gms.google-services` plugin line and the `firebase-*` dependencies from `app/build.gradle.kts` — the MVP screens don't touch Firebase yet.
4. Run on an emulator or device with internet access (the mock repository streams from public HLS test URLs).

## Project structure

```
app/src/main/java/com/streamflow/app/
  MainActivity.kt / StreamFlowApp.kt
  di/               — Hilt modules (repository binding lives here)
  navigation/       — NavGraph + route definitions
  data/
    model/          — Title, Episode, CastMember, Rail
    repository/      — CatalogRepository interface + MockCatalogRepository
  ui/
    theme/          — Color/Type/Dimens/Theme — implements design.md
    components/     — GlassPill, PosterCard, Buttons, CastAvatar, EpisodeCard, FloatingBottomNav
    screens/
      home/         — HomeScreen + HomeViewModel
      detail/       — TitleDetailScreen + TitleDetailViewModel
      player/       — PlayerScreen (ExoPlayer via AndroidView) + PlayerViewModel
      search/       — SearchScreen + SearchViewModel (debounced)
      profile/      — WatchlistScreen/ViewModel, ProfileScreen (stub)
```

## Known gaps / next steps (see `Implementation_Plan.md`)

- **Launcher icon**: only an `mipmap-anydpi-v26` adaptive icon (simple vector mark) is included. Devices on API 24–25 (below the adaptive-icon floor) have no fallback raster icon yet — add `mipmap-mdpi/hdpi/xhdpi/xxhdpi/ic_launcher.png` before shipping, or raise `minSdk` to 26.
- **Auth screens** (email/OTP + Google Sign-In) are not built yet — Phase 1.5.
- **Shared-element transition** from poster → detail hero (per `design.md` §6) is not wired — currently a standard push transition. `SharedTransitionLayout` (Compose 1.7+) is the intended mechanism.
- **Shimmer loading skeletons** are not implemented — rails currently show a centered spinner while loading, per `design.md` §6 this should be shimmer placeholders instead.
- **Data Saver toggle** — `PlayerViewModel` has a comment marking where `setMaxVideoBitrate()` should be wired to a settings flag.
- Unit/UI tests are not included in this scaffold.

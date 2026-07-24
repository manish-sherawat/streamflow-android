# Task Completion Checklist

## 🎯 Objectives & Tasks

### 1. Home Screen "See All" Grid Layout Window
- [x] **State Management**: Add `seeAllRail` state in `HomeScreen.kt` to handle full-screen section expansion.
- [x] **Trigger Callbacks**: Wire `onSeeAllClick` in all rail sections to trigger the expanded view.
- [x] **`SeeAllWindowDialog` Window**: Create full-screen window with header bar, item count pill (`X Items`), close button, and 3-column `LazyVerticalGrid` layout for poster cards.
- [x] **Item Navigation**: Ensure tapping any poster inside the Grid Window dismisses the window and navigates to the title details.

### 2. Anime World Section Visual Consistency Fix
- [x] **Accent Bar Alignment**: Update accent bar width to `3.dp` and color to `AccentPrimary` for visual consistency across all rails.
- [x] **Header Padding & Spacing**: Add `top = Spacing.sm` padding to match `RailSection` header spacing.
- [x] **Typography Standardization**: Standardize title text style to `13.sp`, bold, `0.8.sp` letter spacing, uppercase.
- [x] **"See All" CTA Row**: Update "See all" button to include `Icons.Filled.PlayArrow` icon and matching `AccentPrimary` styling.
- [x] **Rating Badge Formatting**: Fix IMDb rating string formatting in `PosterCard` to prevent duplicate star icons.

### 3. Header & Navigation Clean Up
- [x] **Remove VIP PRO Badge**: Remove the `VIP PRO` badge from the top right corner of `TopBrandBar` on the Home Screen.

### 4. Pull-to-Refresh Gesture Fix
- [x] **Fix Pull-to-Refresh Execution**: Resolved race condition between `rememberPullToRefreshState()` and ViewModel state where pull-to-refresh was prematurely calling `endRefresh()` and cancelling swipe gesture refreshes in `HomeScreen.kt` and `TitleDetailScreen.kt`.

### 5. My List / Watchlist Feature Fix
- [x] **Screen Auto-Refresh**: Added `LaunchedEffect(Unit)` in `WatchlistScreen.kt` to automatically fetch fresh saved titles whenever the user opens the "My List" tab.
- [x] **Repository Title Resolution**: Updated `getTitle` in `NeonCatalogRepository.kt` to check cached titles, fallback titles, fallback anime titles, and cached rails to guarantee saved titles are resolved and returned in `getWatchlist()`.

### 6. Video Player Gesture Sensitivity Reduction
- [x] **Brightness Gesture Sensitivity**: Reduced brightness drag sensitivity in `PlayerScreen.kt` by replacing `500f` divisor with `1800f` for smooth fine-tuned control.
- [x] **Volume Gesture Sensitivity**: Replaced per-pixel volume step increments with distance accumulation (`volumeAccumulator += deltaY` with `80.dp` threshold) to eliminate hyper-sensitive volume jumps during swipes.

### 7. Floating Bottom Navigation Bar Theme Update
- [x] **Replace Purple Accent Colors**: Replaced all M3 purple/violet container tints in `FloatingBottomNav.kt` with the app's signature `AccentPrimary` (Electric Blue `#2563EB`) icon indicators, text labels, soft glow pills (`AccentPrimary.copy(alpha = 0.15f)`), and glassmorphic surface borders (`GlassBorder`).

### 8. Hero Carousel Sequential Ordering Fix
- [x] **Eliminate Random Shuffling**: Removed `.shuffled()` calls in `NeonCatalogRepository.kt` so the hero section and home rails preserve exact, deterministic chronological ordering.
- [x] **Sequential Featured Selection**: Updated `heroTitles` in `HomeScreen.kt` to display featured and newly added titles in stable sequence without scrambling order on refresh.

### 9. Category Chips Grid View Layout
- [x] **Grid View Transformation**: Transformed category selection behavior in `HomeScreen.kt`. Clicking category chips (`Movies`, `TV Shows`, `Web Series`, `Anime`, `Trending`) replaces horizontal sliding rails with a responsive **3-Column Grid (`LazyVerticalGrid`)** showing section headers, item counts, and rank badges.

### 10. Build & Code Quality Verification
- [x] **Kotlin Compilation**: Verify build and compilation with `./gradlew compileDebugKotlin`.

# StreamFlow Android 🎬

**StreamFlow** is a premium, state-of-the-art Android video streaming application built with **Jetpack Compose**, **ExoPlayer (Media3)**, **Hilt**, and **Coroutines**. Designed with modern glassmorphic aesthetics, high-performance image caching, and rich interactive player gestures.

---

## ✨ Features & Highlights

### 📺 Advanced Custom ExoPlayer
- **Picture-in-Picture (PiP) Mode**: Seamless background playback with Android system PiP window support.
- **Audio Focus Handling**: Automatic pause/resume when background audio (Spotify, Podcasts) plays.
- **Playback Speed Selector**: Speeds from `0.5x`, `1.0x`, `1.25x`, `1.5x`, to `2.0x`.
- **Dynamic Aspect Ratio Control**: Toggle between `Fit (16:9)`, `Crop to Fill`, and `Stretch`.
- **Subtitle Styling Controls**: Custom font sizes (*Small*, *Medium*, *Large*) and text colors (*White*, *Yellow*, *Cyan*).
- **Glassmorphic Network Error Dialog**: 1-tap connection retry dialog overlay.
- **Gesture Controls**: Vertical swipe gestures for brightness & volume tuning, double-tap 10s seek ripples.

### 🏠 Home & Discovery
- **Hero Carousel Banner**: Featuring top trending titles.
- **Continue Watching Rail**: Visual watch progress bars for instant binge resumption.
- **Anime World & Categorized Rails**: Dedicated anime arc and genre rails.

### 🔍 Advanced Search & Filtering
- **Interactive Search Filter Sheet**: Filter by Content Type (*Movies*, *TV Shows*, *Anime*), IMDb Rating (*7.0+*, *8.0+*), and Genre.

### ⚡ Performance & Caching
- **Coil 30% Memory & Disk Caching**: Instant 0ms image loading and 60fps smooth scrolling.
- **Tactile Haptics & Spring Animations**: Tactile spring physics press feedback across poster cards and buttons.

---

## 🛠️ Tech Stack

- **UI**: Jetpack Compose, Material 3, Custom Glassmorphism Theme
- **Media**: AndroidX Media3 (ExoPlayer)
- **Dependency Injection**: Dagger Hilt
- **Async & Reactive**: Kotlin Coroutines & StateFlow
- **Image Loading**: Coil ImageLoader
- **Architecture**: MVVM + Clean Architecture Repository Pattern

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Jellyfish / Koala or newer
- JDK 17
- Android SDK 26+

### Build & Run
```bash
git clone https://github.com/manish-sherawat/streamflow-android.git
cd streamflow-android
./gradlew assembleDebug
```

---

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.

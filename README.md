# AutoClicker

> A rootless Android auto-clicker built on the Accessibility API and Android's system overlay.

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.09-4285F4?logo=jetpackcompose&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange)
![No Root](https://img.shields.io/badge/no%20root-required-brightgreen)

Simulates taps on any screen using `AccessibilityService.dispatchGesture()` — no root required. Built to explore how Android's accessibility and overlay systems work in practice: the `TYPE_APPLICATION_OVERLAY` window type, foreground service lifecycle, gesture coordinate mapping, and Android's per-service gesture serialization constraint.

See [USAGE.md](USAGE.md) for setup and usage instructions.

## Features

- Multiple named profiles, each with an independent tap position and settings
- Floating overlay panel (draggable, minimizable to a bubble) that persists over any app
- Per-profile config: tap vs. press-and-hold, fixed or infinite click count, interval, start delay
- Interval and position jitter to randomize timing and target coordinates
- Hold-to-drag profile reordering
- Profiles persist across restarts via Room

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose (Material 3), BOM 2024.09 |
| Architecture | MVVM — ViewModels + Repository |
| Persistence | Room |
| Navigation | Navigation Compose |
| Async | Coroutines (click loops, state management) |
| Gesture injection | `AccessibilityService` + `GestureDescription` |
| Overlay | `TYPE_APPLICATION_OVERLAY` foreground service |

## Architecture

The app is split across three main components:

- **Main app (MVVM)** — `ProfileListViewModel` and `ProfileEditViewModel` manage UI state and delegate persistence to `ClickerRepository`, which wraps Room. Navigation Compose handles screen routing.
- **OverlayService** — a foreground service that draws a Compose-based floating panel via `WindowManager` and `TYPE_APPLICATION_OVERLAY`. Holds the active profile reference and manages play/stop state independently of the main activity.
- **AutoClickerAccessibilityService** — dispatches `GestureDescription` instances via `dispatchGesture()`. Exposes itself as a `mutableStateOf` singleton so the UI can reactively observe whether the service is bound.

The overlay service and accessibility service communicate through the shared singleton, keeping them decoupled from the main activity lifecycle.

## How It Works

A few non-obvious constraints shaped the implementation:

**Gesture serialization** — Android serializes `dispatchGesture()` calls per accessibility service. Submitting a second gesture before the first completes silently drops it. Each click loop waits for the gesture callback before scheduling the next interval, rather than firing on a fixed timer.

**Coordinate mapping** — Overlay touch events are in window space, which includes the status bar height. Gesture coordinates are in display space, which does not. Click positions are corrected by subtracting the status bar height at dispatch time.

**Overlay lifecycle** — The floating panel runs in a foreground service so it survives app backgrounding and task switching. The service manages its own `WindowManager` layout params and handles drag, minimize, and stop independently of the main activity.

## Permissions

| Permission | Purpose |
|---|---|
| `BIND_ACCESSIBILITY_SERVICE` | Inject taps via `dispatchGesture()` |
| `SYSTEM_ALERT_WINDOW` | Draw the floating overlay over other apps |
| `FOREGROUND_SERVICE` | Keep the overlay service alive in the background |

The accessibility service only dispatches gestures — it reads no screen content.

## Running Locally

1. Clone and open in Android Studio (Koala or newer)
2. Let Gradle sync
3. Run on a device or emulator (Android 8.0 / API 26+)
4. Grant Accessibility and Overlay permissions when prompted

## Project Structure

```
app/src/main/java/com/autoclicker/
├── MainActivity.kt
├── data/
│   ├── ClickerDao.kt
│   ├── ClickerDatabase.kt
│   ├── ClickerProfile.kt            # Room entity
│   └── ClickerRepository.kt
├── service/
│   ├── AutoClickerAccessibilityService.kt   # gesture injection
│   └── OverlayService.kt                    # foreground service + Compose overlay
├── ui/
│   ├── overlay/OverlayPanel.kt
│   ├── screens/
│   │   ├── OnboardingScreen.kt
│   │   ├── ProfileEditScreen.kt
│   │   └── ProfileListScreen.kt
│   └── theme/Theme.kt
└── viewmodel/
    ├── ProfileEditViewModel.kt
    └── ProfileListViewModel.kt
```

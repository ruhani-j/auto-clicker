# AutoClicker

A rootless Android auto-clicker that simulates taps on any screen without
requiring root access.

## Details

A personal-use Android app built to explore how Android's `AccessibilityService`
and overlay system work together. Uses `dispatchGesture()` to inject taps on
any app, with a floating control panel that stays on top of whatever is on screen.
Supports multiple named profiles with independent settings and tap positions,
so you can switch between different click targets without reconfiguring each time.

## How it works

- `service/AutoClickerAccessibilityService.kt` — an `AccessibilityService` that
  Android calls to dispatch gestures. Exposes itself as a `mutableStateOf`
  singleton so the UI can reactively observe whether the service is bound.
  Each click loop waits for the `dispatchGesture()` callback before scheduling
  the next tick — Android serializes gestures per service, so firing before
  the previous gesture completes silently drops it.
- `service/OverlayService.kt` — a foreground service that draws a Compose-based
  floating panel via `WindowManager` and `TYPE_APPLICATION_OVERLAY`. Manages
  its own drag, minimize, and stop logic independently of the main activity,
  so the overlay survives app backgrounding and task switching.
- `data/` — Room database (`ClickerDatabase`, `ClickerDao`, `ClickerProfile`,
  `ClickerRepository`) storing profiles on-device only. No network calls, no
  external services.
- `ui/` — Jetpack Compose screens (see below).
- `MainActivity.kt` — entry point; handles onboarding permission flow and
  navigates between screens.

A coordinate correction is applied at dispatch time: overlay touch events are
in window space (which includes the status bar), but `GestureDescription`
coordinates are in display space (which does not). The status bar height is
subtracted when building the gesture path.

## Screens

| Screen | Purpose |
|---|---|
| **Profile list** | All saved profiles, each with a dot marking its tap position. Hold and drag to reorder. Tap the dot menu to edit or delete. Tap the play button in the top bar to launch the overlay. |
| **Profile edit** | Configure name, click type (tap or press-and-hold), click count (fixed or infinite), interval, start delay, and jitter settings. |
| **Onboarding** | Guides you through granting Accessibility and Overlay permissions on first launch. |

## Profile settings

| Setting | Description |
|---|---|
| **Name** | Label for the profile |
| **Click type** | Single tap, or press-and-hold with configurable hold duration |
| **Click count** | Fixed number of clicks, or infinite loop |
| **Interval** | Delay between clicks (minimum 50 ms) |
| **Interval jitter** | Randomly vary the interval by ±N ms per click |
| **Position jitter** | Randomly offset the tap position by ±N px per click |
| **Start delay** | Wait before the first click fires |

## Overlay

The floating panel launches as a foreground service, so it stays on screen over
any app. From the overlay you can drag it to a new position, minimize it to a
small bubble, tap the screen to reposition the click target, and start or stop
the active profile. A dot marks the current click target on screen; a brief
flash confirms each tap.

## Permissions

| Permission | Purpose |
|---|---|
| `BIND_ACCESSIBILITY_SERVICE` | Inject taps via `dispatchGesture()` |
| `SYSTEM_ALERT_WINDOW` | Draw the floating overlay over other apps |
| `FOREGROUND_SERVICE` | Keep the overlay service alive in the background |

The accessibility service only dispatches gestures — it reads no screen content.

## Tech stack

- **Kotlin 2.0** + **Jetpack Compose** (Material 3, BOM 2024.09)
- **Room** — local database for persisting profiles
- **Navigation Compose** — screen routing
- **Coroutines** — click timing loops and async state
- **MVVM** — ViewModels + Repository pattern

## Setup / build steps

1. Clone and open in Android Studio (Koala or newer).
2. Let Gradle sync.
3. Run on a device or emulator (Android 8.0 / API 26+).
4. Grant Accessibility and Overlay permissions when prompted.

See [USER_GUIDE.md](USER_GUIDE.md) for a full walkthrough of setup and usage.

## Notes

- No secrets, API keys, or network permissions are used. Everything stays on-device.
- Min SDK: 26 (Android 8.0). Target SDK: 35.
- The accessibility service declaration in `AndroidManifest.xml` requires
  `BIND_ACCESSIBILITY_SERVICE` — Android will refuse to bind the service without it.

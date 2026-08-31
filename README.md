# AutoClicker

An Android auto-clicker app I built to learn how Android's Accessibility API and overlay system work. No root required — it uses `dispatchGesture()` to simulate taps on any app.

See [USAGE.md](USAGE.md) for a guide on setting up and using the app.

## Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Room** — local database for persisting profiles
- **Navigation Compose** — screen routing
- **Coroutines** — click timing loops and async state
- **AccessibilityService + GestureDescription** — gesture injection without root
- **TYPE_APPLICATION_OVERLAY** — floating panel drawn over other apps
- **MVVM** architecture with ViewModels and Repository pattern

## What It Does

- Create multiple named clicker profiles, each with independent settings
- Floating overlay panel (draggable, minimizable to a bubble) that stays on top of any app
- Per-profile config: tap vs. press-and-hold, fixed or infinite clicks, interval, start delay
- Random jitter on interval and position to vary timing
- Profiles persist across restarts via Room

## Permissions Used

| Permission | Purpose |
|---|---|
| `BIND_ACCESSIBILITY_SERVICE` | Simulate taps via `dispatchGesture()` |
| `SYSTEM_ALERT_WINDOW` | Draw the floating overlay on top of other apps |
| `FOREGROUND_SERVICE` | Keep the overlay service running in the background |

The accessibility service only dispatches gestures — it doesn't read any screen content.

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
│   ├── ClickerProfile.kt        # Room entity
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

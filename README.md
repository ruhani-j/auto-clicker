# AutoClicker

An Android auto-clicker app built with Kotlin and Jetpack Compose. Uses the Accessibility API to simulate taps — no root required.

## Features

- **Multiple clicker profiles** — create, name, edit, reorder, and delete independent clickers
- **Floating overlay panel** — draggable control panel stays on top of any app; minimize to a bubble while running
- **Per-profile settings**
  - Single tap or press-and-hold (configurable hold duration)
  - Fixed click count or infinite loop
  - Interval between clicks (minimum 50 ms)
  - Random jitter on interval (±ms) and position (±px) to reduce detection
  - Start delay before first click
- **Persistent profiles** — stored in a Room database, survive app restarts
- **Material 3 UI** with live status indicators
- **Onboarding screens** that explain and link directly to the required permission settings

## Requirements

- Android API 26+ (Android 8.0 Oreo)
- Key settings (requested on first launch):
  - **Accessibility Service** — used to call `dispatchGesture()` via `GestureDescription`. AutoClicker does **not** read any screen content.
  - **Display Over Other Apps** (`SYSTEM_ALERT_WINDOW`) — required for the floating overlay panel.

## Getting Started

1. Clone the repo and open in Android Studio (Koala or newer).
2. Let Gradle sync.
3. Run on a device or emulator running Android 8+.
4. Follow the onboarding to grant Accessibility and Overlay permissions.
5. Tap **+** to create a clicker profile, configure it, then press **▶** in the top bar to start the overlay.

## Project Structure

```
app/src/main/java/com/autoclicker/
├── MainActivity.kt              # Entry point, Compose navigation host
├── data/
│   ├── ClickerDao.kt
│   ├── ClickerDatabase.kt
│   ├── ClickerProfile.kt        # Room entity
│   └── ClickerRepository.kt
├── service/
│   ├── AutoClickerAccessibilityService.kt   # dispatchGesture clicks
│   ├── OverlayLifecycleOwner.kt
│   └── OverlayService.kt                    # Foreground service + ComposeView overlay
├── ui/
│   ├── overlay/
│   │   └── OverlayPanel.kt              # Floating panel composable
│   ├── screens/
│   │   ├── OnboardingScreen.kt
│   │   ├── ProfileEditScreen.kt
│   │   └── ProfileListScreen.kt
│   └── theme/
│       └── Theme.kt
└── viewmodel/
    ├── ProfileEditViewModel.kt
    └── ProfileListViewModel.kt
```

## Permissions & Manifest

| Permission / Service | Why |
|---|---|
| `android.permission.BIND_ACCESSIBILITY_SERVICE` | Required to simulate taps via `AccessibilityService` |
| `android.permission.SYSTEM_ALERT_WINDOW` | Draw the floating overlay on top of other apps |
| `android.permission.FOREGROUND_SERVICE` | Keep the overlay service alive while running |
| `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` | Required for foreground services on API 34+ |

The `AccessibilityService` declaration in `AndroidManifest.xml` includes `canPerformGestures="true"`, which is what enables `dispatchGesture()`.

## Play Store Policy Note

Apps using `AccessibilityService` to automate interactions are **restricted** on the Play Store per [Google's policy](https://support.google.com/googleplay/android-developer/answer/10964491). Distribution via direct APK sideloading or through alternative stores is generally fine. If submitting to the Play Store, you must provide a compelling accessibility use case and receive explicit approval.

## Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Room** for local persistence
- **Navigation Compose** for screen routing
- **Coroutines** for click timing loops
- **AccessibilityService + GestureDescription** for gesture injection
- **TYPE_APPLICATION_OVERLAY** window for the floating panel

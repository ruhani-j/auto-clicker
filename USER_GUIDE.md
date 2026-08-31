# AutoClicker — User Guide

> Automate repetitive taps without root.

---

## What is this?

**AutoClicker** is a free Android app that simulates screen taps on your behalf using Android's Accessibility API — no root required.

You create one or more **profiles**, each describing where to tap, how often, and how many times. When you launch the overlay, a floating dot appears for each profile at its configured position. The dots fire taps on a loop in the background while you use any other app.

Everything runs on-device. No accounts, no internet access, no cloud sync.

---

## Why you might want this

- You play a mobile game with repetitive tapping (farming, idle clicking, gacha pulls).
- You need to stress-test a UI element repeatedly during development.
- You want to automate any tap-based interaction without writing scripts or rooting your phone.

---

## Requirements

- Android 8.0 (Oreo) or newer
- Two permissions granted on first launch (see Setup below)

---

## Setup

On first launch, AutoClicker shows an onboarding screen that walks you through two required permissions. Neither can be skipped — the app will not function without both.

### Accessibility Service

Tap **Grant** on the Accessibility card. Android opens **Settings → Accessibility → Installed Apps → AutoClicker**. Enable the service and confirm the system prompt.

This permission is what allows the app to inject taps on your behalf via `dispatchGesture()`.

### Display Over Other Apps

Tap **Grant** on the overlay card. Android opens the app's overlay permission page. Toggle **Allow display over other apps**.

This permission is what allows the floating control panel and dots to appear on top of other apps.

Once both permissions are granted, the **Get Started** button activates and takes you to the main screen.

---

## Creating a Profile

Tap **+** on the main screen. A new profile is created with default settings and the edit screen opens immediately.

| Setting | What it does |
|---|---|
| **Name** | A label shown on the dot popup and the profile card |
| **Click type** | Single Tap, or Press & Hold with a configurable hold duration |
| **Interval** | How long to wait between clicks (minimum 50 ms) |
| **Interval jitter** | Randomly vary each interval by ±N ms — useful for appearing more human |
| **Start delay** | Wait this long before the first click fires after starting |
| **Click count** | Infinite loop, or stop after a fixed number of taps |
| **X / Y position** | Pixel coordinates of the tap target on screen |
| **Position jitter** | Randomly offset the tap location by ±N px per click |

Tap **Save** (or the back arrow) to save. You can create as many profiles as you need — each becomes its own dot on the overlay.

---

## Starting the Overlay

Tap the **▶** button in the bottom-right corner of the main screen. A floating overlay appears over whatever is on screen, with:

- One **colored dot** for each profile, positioned at that profile's configured coordinates
- A **pill-shaped control bar** you can drag anywhere on screen

The overlay runs as a foreground service, so it stays active while you switch apps, lock the screen, or navigate anywhere else on your phone. A persistent notification shows while the service is running — tapping it returns you to the main app.

---

## The Control Bar

The control bar has three buttons:

| Button | What it does |
|---|---|
| **Play / Pause** | Starts or pauses all click loops. While paused, dots are touchable so you can reposition them. The icon turns blue when paused. |
| **Hide** | Hides all dots and collapses the control bar into a small floating bubble. Tap the bubble to restore everything. |
| **Stop (✕)** | Stops all click jobs, removes the overlay, and stops the foreground service entirely. |

You can drag the control bar itself to move it out of the way.

---

## The Clicker Dots

Each profile appears as a translucent colored circle on screen. The small white dot in the center marks the exact tap coordinate.

**While paused or before starting:**

- Drag a dot to reposition it — the new position saves automatically when you release.
- Tap a dot to open its **inline edit popup**, where you can adjust settings without leaving whatever app you're in.

**While running:**

- Dots pass all touches through to the underlying app so clicks reach their target.
- If the flash effect is enabled, each dot briefly flashes white when it fires a tap.

### Inline edit popup

Tapping a dot while paused opens a small popup with the most-used settings:

- Profile name
- **Tap / Hold** toggle
- **Interval** stepper (±100 ms per press)
- **Hold duration** stepper (only shown when Hold is selected)
- **Infinite** toggle and click count stepper
- **Done** button to close

Changes in the popup save immediately.

---

## Flash effect

The flash toggle (lightning bolt icon in the top bar) controls whether dots show a brief white flash each time they fire a tap. Turn it off if the flashing is distracting while you're actively watching the screen.

The setting is saved and remembered across restarts.

---

## Reordering Profiles

On the main screen, long-press any profile card and drag it to reorder. The new order is saved automatically and determines how dot colors are assigned (first profile gets green, second blue, and so on).

---

## Stopping

Tap the **✕ (Stop)** button in the overlay control bar. Alternatively, return to the AutoClicker app and tap the **■ (Stop)** button in the bottom-right corner.

---

## Tips

- **Set X/Y position in the profile editor first**, then fine-tune by dragging the dot on the overlay once you're inside the target app.
- **Use interval jitter** if you're automating something that rate-limits or detects robotic timing — even a ±50 ms jitter makes the pattern less regular.
- **Use position jitter** if the tap target moves slightly (e.g. a moving element) or if you want taps spread across a small area.
- **Use start delay** when the target app needs a moment to load before the first tap should fire.
- **Pause instead of stop** if you want to reposition dots or change settings mid-session without restarting the service.

---

## Privacy

- All profile data is stored locally in a Room (SQLite) database on your device.
- The app has no internet permission and makes no network requests.
- No analytics, crash reporting, or telemetry of any kind.
- Uninstalling the app removes all stored data.

---

## Feedback / contributing

This is a personal-use project shared openly. If you run into a bug or have a suggestion, open an issue or reach out directly.

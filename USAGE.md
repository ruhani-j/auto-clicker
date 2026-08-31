# AutoClicker — Usage Guide

## Setup

On first launch, AutoClicker will walk you through granting two permissions:

**Accessibility Service**
Go to Settings → Accessibility → Installed Apps → AutoClicker and enable it. This is what allows the app to inject taps on your behalf.

**Display Over Other Apps**
Go to Settings → Apps → AutoClicker → Display over other apps and enable it. This is required for the floating control panel.

Both are required. The app won't function without them.

---

## Creating a Profile

Tap **+** on the main screen to create a clicker profile. Each profile has its own settings and can be started independently.

| Setting | Description |
|---|---|
| **Name** | Label for the profile |
| **Click type** | Single tap, or press-and-hold with a configurable hold duration |
| **Click count** | Fixed number of clicks, or infinite loop |
| **Interval** | Delay between clicks (minimum 50 ms) |
| **Interval jitter** | Randomly vary the interval by ±N ms per click |
| **Position jitter** | Randomly offset the tap position by ±N px per click |
| **Start delay** | Wait before the first click fires |

Tap **Save** when done. You can create as many profiles as you need.

---

## Starting the Overlay

Tap **▶** in the top bar to launch the floating overlay. A control panel appears on top of whatever app is on screen.

From the overlay you can:
- **Drag** the panel to reposition it
- **Minimize** it to a bubble while clicks are running
- **Tap the screen** to set the click target — a dot marks the current position
- **Start / Stop** the selected profile

The overlay runs as a foreground service, so you can switch apps and it stays active.

---

## Stopping

Tap **Stop** in the overlay panel, or use the stop action in the persistent foreground service notification. You can also return to the AutoClicker app and stop it from there.

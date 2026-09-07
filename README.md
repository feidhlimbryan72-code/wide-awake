# Wide Awake (Dual-Platform: iOS & Android)

**Wide Awake** is an unapologetic, high-accountability gamified alarm clock app engineered specifically for heavy sleepers across both **iOS (Swift 6 & SwiftUI)** and **Android (Kotlin & Jetpack Compose)**, with an **Apple Watch companion app** and an **interactive Web Simulator**.

It locks the device screen and forces cognitive, physical, visual, and biometric disarm challenges before high-urgency siren audio playback can be silenced.

---

## Key Features

1. **Unforgiving Audio Playback**:
   - **iOS**: `AVAudioSession` with `.playback` and `[.duckOthers]` overrides the hardware mute switch and ducks other audio. Includes a synthetic emergency dual-frequency siren generator fallback (2400Hz - 3200Hz).
   - **Android**: Foreground service with `MediaPlayer` using `AudioAttributes.USAGE_ALARM` and `FLAG_AUDIBILITY_ENFORCED`, overriding Do Not Disturb (DND) and silent mode.
2. **90-Second Snooze Penalty**:
   - If challenges are not cleared within 90 seconds, volume ramps to 100% and intense continuous haptic vibrations trigger.
3. **Lock-In Disarm UI**:
   - **iOS**: Full-screen modal disabling dismissal or back gestures.
   - **Android**: `USE_FULL_SCREEN_INTENT`, `setShowWhenLocked(true)`, and `setTurnScreenOn(true)` waking the screen over the keyguard.
4. **User-Selectable Disarm Challenges**:
   - **Mental Arithmetic**: Multi-step calculations with custom numeric keypad.
   - **Dynamic Shake**: Accelerometer variance filter rejecting micro-shakes.
   - **Location Anchor Scanner**: Camera QR/Barcode scan at designated physical locations (e.g. bathroom or coffee maker).
   - **Memory Matrix**: Cognitive visual pattern recall on a 3x3 grid.
   - **Jigsaw Puzzle**: Numbered tile sliding puzzle.
   - **Wearable Biometrics**: Apple Watch / Wear OS step count and heart rate streaming.
   - **Real-Time Switcher**: Option to choose or switch challenge when waking up.
5. **Superwall Monetization Parity**:
   - Free tier: 1 active alarm and standard Math / Shake disarms.
   - Pro Weekly: **$4.99 / week** with **3-day free trial**.
   - Pro Lifetime: **$199.99 one-time unlock**.
   - Placement triggers: Pro disarms, unlimited alarms, onboarding completion.

---

## Project Structure

```
├── ios/
│   ├── App/
│   │   ├── WideAwakeApp.swift
│   │   └── AppDelegate.swift
│   ├── Models/
│   │   ├── Alarm.swift
│   │   ├── ChallengeType.swift
│   │   └── SubscriptionTier.swift
│   ├── Services/
│   │   ├── AudioPlayerService.swift
│   │   ├── AlarmSchedulerService.swift
│   │   ├── MotionTrackerService.swift
│   │   ├── WatchSyncService.swift
│   │   └── PaywallManager.swift
│   ├── Views/
│   │   ├── Alarms/ (AlarmListView, AlarmEditView)
│   │   ├── ActiveAlarm/ (ActiveAlarmView)
│   │   ├── Challenges/ (Math, Shake, Barcode, MemoryMatrix, Jigsaw)
│   │   └── Paywall/ (PaywallView)
│   ├── WatchApp/
│   │   ├── WideAwakeWatchApp.swift
│   │   ├── WatchWorkoutManager.swift
│   │   └── WatchDisarmView.swift
│   ├── Info.plist
│   └── Package.swift
│
├── android/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── app/
│       ├── build.gradle.kts
│       └── src/main/
│           ├── AndroidManifest.xml
│           ├── res/values/ (strings, colors, themes)
│           └── java/com/wideawake/app/
│               ├── WideAwakeApplication.kt
│               ├── model/ (Alarm, ChallengeType, SubscriptionTier)
│               ├── service/ (Audio, Scheduler, Motion, Health, Paywall)
│               └── ui/ (MainActivity, ActiveAlarmActivity, dashboard, challenges, paywall, theme)
│
└── web/
    └── index.html (Interactive standalone web simulator with Web Audio siren synthesizer)
```

---

## How to Test & Run

### 1. Interactive Simulator (Instant on any machine)
You can launch and test the full app experience immediately in your browser:
- Open [`web/index.html`](file:///c:/Users/feidh/Desktop/Antigravity%20apps/Wide%20awake/web/index.html) in Chrome, Edge, or Safari.
- Hear the emergency siren synthesizer in action (Web Audio API).
- Test the 90-second volume ramp and red alert penalty escalation.
- Try out all challenges: Math Keypad, Shake, Barcode anchor, Memory Matrix, and Jigsaw puzzle!

### 2. iOS
1. Open `ios/` in Xcode on macOS.
2. Select your device or simulator running iOS 16+.
3. Build and run.

### 3. Android
1. Open `android/` in Android Studio.
2. Ensure JDK 17 and Android SDK 34 are configured.
3. Run on physical Android device or emulator.

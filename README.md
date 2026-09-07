# Wide Awake (Dual-Platform: iOS & Android)

**Wide Awake** is an unapologetic, high-accountability gamified alarm clock app engineered specifically for heavy sleepers across both **iOS (Swift 6 & SwiftUI)** and **Android (Kotlin & Jetpack Compose)**.

It locks the device screen and forces cognitive, physical, visual, and biometric disarm challenges before high-urgency siren audio playback can be silenced.

---

## Key Features

1. **Unforgiving Audio Playback**:
   - **iOS**: `AVAudioSession` with `.playback` and `[.duckOthers]` overrides the hardware mute switch and ducks other audio. Includes a synthetic emergency dual-frequency siren generator fallback.
   - **Android**: Foreground service with `MediaPlayer` using `AudioAttributes.USAGE_ALARM` and `FLAG_AUDIBILITY_ENFORCED`, overriding Do Not Disturb (DND) and silent mode.
2. **90-Second Snooze Penalty**:
   - If challenges are not cleared within 90 seconds, volume ramps to 100% and intense continuous haptic vibrations trigger.
3. **Lock-In Disarm UI**:
   - **iOS**: Full-screen modal disabling dismissal or back gestures.
   - **Android**: `USE_FULL_SCREEN_INTENT`, `setShowWhenLocked(true)`, and `setTurnScreenOn(true)` waking the screen over the keyguard.
4. **Disarm Challenges**:
   - **Mental Arithmetic**: Multi-step calculations with custom numeric keypad.
   - **Dynamic Shake**: Accelerometer variance filter rejecting micro-shakes.
   - **Location Anchor Scanner**: Camera QR/Barcode scan at designated physical locations (e.g. bathroom or coffee maker).
   - **Wearable Biometrics**: Apple Watch / Wear OS step count and heart rate streaming.
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
│   │   ├── Challenges/ (Math, Shake, Barcode)
│   │   └── Paywall/ (PaywallView)
│   ├── Info.plist
│   └── Package.swift
│
└── android/
    ├── build.gradle.kts
    ├── settings.gradle.kts
    └── app/
        ├── build.gradle.kts
        └── src/main/
            ├── AndroidManifest.xml
            ├── res/values/ (strings, colors, themes)
            └── java/com/wakeorpay/app/
                ├── WakeOrPayApplication.kt
                ├── model/ (Alarm, ChallengeType, SubscriptionTier)
                ├── service/ (Audio, Scheduler, Motion, Health, Paywall)
                └── ui/ (MainActivity, ActiveAlarmActivity, dashboard, challenges, paywall, theme)
```

---

## Build & Run

### iOS
1. Open `ios/` in Xcode or open via Swift Package Manager:
   ```bash
   cd ios
   swift build
   ```
2. Run on physical device or simulator targeting iOS 16+.

### Android
1. Open `android/` in Android Studio.
2. Ensure Android SDK 34 and JDK 17 are installed.
3. Build and run:
   ```bash
   cd android
   ./gradlew assembleDebug
   ```

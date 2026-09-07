# Antigravity Agent Guidelines: Wide Awake

This file is automatically loaded by Google Antigravity when any Antigravity user opens this workspace.

---

## 1. Project Overview
**Wide Awake** is an unapologetic, high-accountability alarm clock app engineered for heavy sleepers across:
- **iOS (`/ios`)**: Swift 6, SwiftUI, Swift Concurrency (`Sendable`, `@MainActor`), `AVFoundation`, `CoreMotion`, `UserNotifications`, `HealthKit`, `WatchConnectivity`, `SuperwallKit`.
- **Android (`/android`)**: Kotlin, Jetpack Compose, Coroutines & Flow, `AlarmManager.setAlarmClock()`, `ForegroundService` with `AudioAttributes.USAGE_ALARM`, `CameraX` / ML Kit, `SensorManager`, `Superwall Android SDK`.
- **Apple Watch (`/ios/WatchApp`)**: `HKWorkoutSession` real-time step and heart rate streaming.
- **Web Simulator (`/web`)**: Standalone interactive simulator with Web Audio API emergency siren synthesizer.

---

## 2. Core Architectural Invariants

1. **Hardware Audio Overrides**:
   - **iOS**: Audio must use `AVAudioSession` category `.playback` with `[.duckOthers]` to bypass the hardware mute switch. Include synthetic 2400Hz–3200Hz siren wave generator as emergency fallback.
   - **Android**: Audio must run inside a `ForegroundService` with `MediaPlayer` set to `AudioAttributes.USAGE_ALARM` and `FLAG_AUDIBILITY_ENFORCED` to bypass Do Not Disturb and hardware volume down.
2. **Lock-In Disarm Protection**:
   - The active alarm screen disables back navigation and dismiss gestures.
   - Siren audio and vibration shockwaves cannot be silenced until the chosen challenge is verified.
3. **90-Second Snooze Penalty**:
   - Volume escalates to 100% over 90 seconds.
   - Once 90s expires, continuous heavy haptic vibrations / waveform shockwaves trigger and background pulses red.
4. **User-Selectable Disarm Challenges**:
   - Alarms store `chosenChallenge: ChallengeConfig` and `allowUserChoiceOnWake: Bool`.
   - Users can choose between Math, Shake, Barcode, Memory Matrix, Jigsaw, and Wearable Biometrics.
   - If `allowUserChoiceOnWake` is enabled, quick selector pills appear on the active alarm screen to switch disarms in real-time.
5. **Superwall Monetization Parity**:
   - Tier 1: **$4.99 / week** with **3-day free trial**.
   - Tier 2: **$199.99 one-time unlock**.
   - Gating: Free tier allows 1 active alarm and standard Math / Shake disarms. Barcode, Memory Matrix, Jigsaw, and Wearables require Pro.

---

## 3. Package & Target Conventions
- **Android Package**: `com.wideawake.app`
- **iOS Bundle Prefix**: `com.wideawake.*`
- **Root Project Name**: `WideAwake`

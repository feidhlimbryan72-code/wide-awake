import SwiftUI

/// High-urgency lock-in disarm screen.
/// Hides dismiss buttons and back navigation until the chosen challenge is solved.
/// Allows the user to choose or switch between disarm challenges in real-time.
public struct ActiveAlarmView: View {
    public let alarm: Alarm
    public let onDisarmed: () -> Void

    @ObservedObject var audioService = AudioPlayerService.shared
    @State private var activeChallenge: ChallengeConfig
    @State private var pulseAnimation: Bool = false
    @State private var showingChallengePicker: Bool = false

    public init(alarm: Alarm, onDisarmed: @escaping () -> Void) {
        self.alarm = alarm
        self.onDisarmed = onDisarmed
        _activeChallenge = State(initialValue: alarm.chosenChallenge)
    }

    public var body: some View {
        ZStack {
            // Background flashing urgency color
            if audioService.isPenaltyActive {
                Color.red.opacity(pulseAnimation ? 0.35 : 0.15)
                    .ignoresSafeArea()
                    .animation(Animation.easeInOut(duration: 0.5).repeatForever(autoreverses: true), value: pulseAnimation)
            } else {
                Color.black.ignoresSafeArea()
            }

            VStack(spacing: 16) {
                // Header Alert Bar
                VStack(spacing: 8) {
                    HStack(spacing: 8) {
                        Circle()
                            .fill(audioService.isPenaltyActive ? Color.red : Color.orange)
                            .frame(width: 10, height: 10)
                            .scaleEffect(pulseAnimation ? 1.4 : 1.0)

                        Text(audioService.isPenaltyActive ? "MAX PENALTY ESCALATION" : "ALARM ACTIVE")
                            .font(.system(size: 13, weight: .black, design: .monospaced))
                            .foregroundColor(audioService.isPenaltyActive ? .red : .orange)
                            .tracking(2)
                    }

                    Text(alarm.label.uppercased())
                        .font(.system(size: 26, weight: .heavy, design: .rounded))
                        .foregroundColor(.white)

                    // Penalty Countdown / Status
                    if !audioService.isPenaltyActive {
                        HStack(spacing: 4) {
                            Image(systemName: "timer")
                            Text("Penalty Escalation in \(audioService.penaltySecondsRemaining)s")
                        }
                        .font(.system(size: 12, weight: .semibold, design: .monospaced))
                        .foregroundColor(.gray)
                    } else {
                        Text("MAX VOLUME + HAPTICS ENGAGED")
                            .font(.system(size: 12, weight: .black, design: .monospaced))
                            .foregroundColor(.red)
                    }

                    // Volume level indicator
                    ProgressView(value: Double(audioService.currentVolume), total: 1.0)
                        .tint(audioService.isPenaltyActive ? .red : .orange)
                        .padding(.horizontal, 48)
                }
                .padding(.top, 20)

                // Quick Challenge Switcher Bar (User can choose which challenge to solve)
                if alarm.allowUserChoiceOnWake {
                    VStack(spacing: 6) {
                        Text("CHOOSE DISARM CHALLENGE")
                            .font(.system(size: 10, weight: .bold, design: .monospaced))
                            .foregroundColor(.gray)

                        HStack(spacing: 8) {
                            ChallengeChoicePill(
                                title: "Math",
                                icon: "function",
                                isSelected: activeChallenge.type == .math
                            ) {
                                activeChallenge = ChallengeConfig(type: .math, mathDifficulty: alarm.chosenChallenge.mathDifficulty, mathProblemCount: alarm.chosenChallenge.mathProblemCount)
                            }

                            ChallengeChoicePill(
                                title: "Shake",
                                icon: "iphone.radiowaves.left.and.right",
                                isSelected: activeChallenge.type == .shake
                            ) {
                                activeChallenge = ChallengeConfig(type: .shake, targetShakeCount: alarm.chosenChallenge.targetShakeCount)
                            }

                            ChallengeChoicePill(
                                title: "Barcode",
                                icon: "barcode.viewfinder",
                                isSelected: activeChallenge.type == .barcode
                            ) {
                                activeChallenge = ChallengeConfig(type: .barcode, targetBarcodePayload: alarm.chosenChallenge.targetBarcodePayload, targetBarcodeLabel: alarm.chosenChallenge.targetBarcodeLabel)
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                }

                Spacer()

                // Active Challenge View
                Group {
                    switch activeChallenge.type {
                    case .math:
                        MathChallengeView(config: activeChallenge) {
                            completeDisarm()
                        }
                    case .shake:
                        ShakeChallengeView(config: activeChallenge) {
                            completeDisarm()
                        }
                    case .barcode:
                        BarcodeScannerView(config: activeChallenge) {
                            completeDisarm()
                        }
                    default:
                        MathChallengeView(config: activeChallenge) {
                            completeDisarm()
                        }
                    }
                }
                .id(activeChallenge.type.id) // Forces fresh state on challenge switch

                Spacer()

                // Security Note
                Text("Audio will not silence until challenge is verified.")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(.gray.opacity(0.7))
                    .padding(.bottom, 16)
            }
        }
        .interactiveDismissDisabled(true)
        .navigationBarBackButtonHidden(true)
        .onAppear {
            pulseAnimation = true
            audioService.startAlarm(for: alarm)
        }
    }

    private func completeDisarm() {
        audioService.stopAlarm()
        onDisarmed()
    }
}

private struct ChallengeChoicePill: View {
    let title: String
    let icon: String
    let isSelected: Bool
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.system(size: 13))
                Text(title)
                    .font(.system(size: 12, weight: .bold))
            }
            .foregroundColor(isSelected ? .black : .white)
            .padding(.horizontal, 12)
            .padding(.vertical, 7)
            .background(isSelected ? Color.orange : Color(white: 0.16))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isSelected ? Color.orange : Color.gray.opacity(0.3), lineWidth: 1)
            )
        }
    }
}

import SwiftUI

/// Accelerometer-based shake challenge requiring physical exertion.
public struct ShakeChallengeView: View {
    public let config: ChallengeConfig
    public let onCompleted: () -> Void

    @StateObject private var tracker = MotionTrackerService()

    public init(config: ChallengeConfig, onCompleted: @escaping () -> Void) {
        self.config = config
        self.onCompleted = onCompleted
    }

    public var body: some View {
        VStack(spacing: 36) {
            Text("SHAKE VIGOROUSLY")
                .font(.system(size: 16, weight: .black, design: .monospaced))
                .foregroundColor(.yellow)
                .tracking(3)

            // Progress Circular Meter
            ZStack {
                Circle()
                    .stroke(Color(white: 0.15), lineWidth: 18)
                    .frame(width: 220, height: 220)

                Circle()
                    .trim(from: 0.0, to: CGFloat(tracker.progress))
                    .stroke(
                        LinearGradient(
                            colors: [.yellow, .orange, .red],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        style: StrokeStyle(lineWidth: 18, lineCap: .round)
                    )
                    .rotationEffect(.degrees(-90))
                    .frame(width: 220, height: 220)
                    .animation(.easeOut(duration: 0.1), value: tracker.progress)

                VStack(spacing: 6) {
                    Image(systemName: "iphone.radiowaves.left.and.right")
                        .font(.system(size: 42))
                        .foregroundColor(.yellow)

                    Text("\(tracker.currentShakeCount)")
                        .font(.system(size: 48, weight: .heavy, design: .rounded))
                        .foregroundColor(.white)

                    Text("OF \(tracker.targetShakeCount) SHAKES")
                        .font(.system(size: 12, weight: .bold, design: .monospaced))
                        .foregroundColor(.gray)
                }
            }

            Text("Micro-shakes or tilting do not count.\nMove your whole body to generate acceleration.")
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
        .onAppear {
            tracker.startTracking(targetCount: config.targetShakeCount)
        }
        .onDisappear {
            tracker.stopTracking()
        }
        .onChange(of: tracker.isCompleted) { completed in
            if completed {
                onCompleted()
            }
        }
    }
}

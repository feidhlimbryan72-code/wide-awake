import SwiftUI

/// Memory Matrix disarm challenge: Flashes an active pattern across a grid,
/// then requires the user to recall and tap the active tiles to silence the siren.
public struct MemoryMatrixView: View {
    public let config: ChallengeConfig
    public let onCompleted: () -> Void

    private let gridSize = 3 // 3x3 = 9 tiles
    @State private var activePattern: Set<Int> = []
    @State private var selectedTiles: Set<Int> = []
    @State private var isShowingPattern: Bool = true
    @State private var countdown: Int = 3
    @State private var isErrorShake: Bool = false

    public init(config: ChallengeConfig, onCompleted: @escaping () -> Void) {
        self.config = config
        self.onCompleted = onCompleted
    }

    public var body: some View {
        VStack(spacing: 24) {
            Text("MEMORY MATRIX")
                .font(.system(size: 16, weight: .black, design: .monospaced))
                .foregroundColor(.cyan)
                .tracking(3)

            if isShowingPattern {
                Text("MEMORIZE ACTIVE TILES (\(countdown)s)")
                    .font(.system(size: 14, weight: .bold, design: .monospaced))
                    .foregroundColor(.yellow)
            } else {
                Text("TAP ALL \(activePattern.count) MEMORIZED TILES")
                    .font(.system(size: 14, weight: .bold, design: .monospaced))
                    .foregroundColor(.white)
            }

            // 3x3 Grid
            VStack(spacing: 12) {
                ForEach(0..<gridSize, id: \.self) { row in
                    HStack(spacing: 12) {
                        ForEach(0..<gridSize, id: \.self) { col in
                            let index = row * gridSize + col
                            let isHighlighted = isShowingPattern ? activePattern.contains(index) : selectedTiles.contains(index)

                            Button(action: {
                                handleTileTap(index)
                            }) {
                                RoundedRectangle(cornerRadius: 14)
                                    .fill(isHighlighted ? Color.cyan : Color(white: 0.14))
                                    .frame(width: 80, height: 80)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 14)
                                            .stroke(isHighlighted ? Color.white : Color.gray.opacity(0.3), lineWidth: 2)
                                    )
                                    .shadow(color: isHighlighted ? Color.cyan.opacity(0.6) : Color.clear, radius: 8)
                            }
                            .disabled(isShowingPattern)
                        }
                    }
                }
            }
            .offset(x: isErrorShake ? -10 : 0)
            .animation(isErrorShake ? Animation.default.repeatCount(4, autoreverses: true).speed(4) : .default, value: isErrorShake)

            Text("Recalling visual spatial patterns quickly accelerates brain wakefulness.")
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
        .onAppear {
            startRound()
        }
    }

    private func startRound() {
        selectedTiles.removeAll()
        isShowingPattern = true
        countdown = 3

        // Pick 4 unique random tiles
        var pattern = Set<Int>()
        while pattern.count < 4 {
            pattern.insert(Int.random(in: 0..<9))
        }
        activePattern = pattern

        // Countdown timer
        Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { timer in
            if countdown > 1 {
                countdown -= 1
            } else {
                timer.invalidate()
                isShowingPattern = false
            }
        }
    }

    private func handleTileTap(_ index: Int) {
        guard !isShowingPattern else { return }

        if activePattern.contains(index) {
            selectedTiles.insert(index)
            let generator = UIImpactFeedbackGenerator(style: .medium)
            generator.impactOccurred()

            if selectedTiles == activePattern {
                let successGen = UINotificationFeedbackGenerator()
                successGen.notificationOccurred(.success)
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                    onCompleted()
                }
            }
        } else {
            // Error tap: flash and restart pattern
            isErrorShake = true
            let errorGen = UINotificationFeedbackGenerator()
            errorGen.notificationOccurred(.error)
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                isErrorShake = false
                startRound()
            }
        }
    }
}

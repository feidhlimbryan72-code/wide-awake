import SwiftUI

/// Jigsaw/Sliding tile puzzle requiring visual spatial alignment to disarm.
public struct JigsawChallengeView: View {
    public let config: ChallengeConfig
    public let onCompleted: () -> Void

    // 3x3 slider with numbers 1..8 and 0 representing empty tile
    @State private var board: [Int] = [1, 2, 3, 4, 5, 0, 7, 8, 6] // Scrambled near solution
    private let targetBoard: [Int] = [1, 2, 3, 4, 5, 6, 7, 8, 0]

    public init(config: ChallengeConfig, onCompleted: @escaping () -> Void) {
        self.config = config
        self.onCompleted = onCompleted
    }

    public var body: some View {
        VStack(spacing: 24) {
            Text("JIGSAW PUZZLE")
                .font(.system(size: 16, weight: .black, design: .monospaced))
                .foregroundColor(.purple)
                .tracking(3)

            Text("SLIDE TILES IN ORDER (1 TO 8)")
                .font(.system(size: 13, weight: .bold, design: .monospaced))
                .foregroundColor(.white)

            // 3x3 Tile Grid
            VStack(spacing: 8) {
                ForEach(0..<3, id: \.self) { row in
                    HStack(spacing: 8) {
                        ForEach(0..<3, id: \.self) { col in
                            let index = row * 3 + col
                            let value = board[index]

                            Button(action: {
                                slideTile(at: index)
                            }) {
                                ZStack {
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(value == 0 ? Color.clear : Color.purple.opacity(0.85))
                                        .frame(width: 80, height: 80)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .stroke(value == 0 ? Color.clear : Color.white.opacity(0.4), lineWidth: 1.5)
                                        )

                                    if value != 0 {
                                        Text("\(value)")
                                            .font(.system(size: 30, weight: .black, design: .rounded))
                                            .foregroundColor(.white)
                                    }
                                }
                            }
                            .disabled(value == 0)
                        }
                    }
                }
            }

            Text("Slide the numbered tiles into numerical order with empty slot at bottom-right.")
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
    }

    private func slideTile(at index: Int) {
        guard let emptyIndex = board.firstIndex(of: 0) else { return }

        let row = index / 3
        let col = index % 3
        let emptyRow = emptyIndex / 3
        let emptyCol = emptyIndex % 3

        let isAdjacent = (abs(row - emptyRow) == 1 && col == emptyCol) ||
                         (abs(col - emptyCol) == 1 && row == emptyRow)

        if isAdjacent {
            board.swapAt(index, emptyIndex)
            let generator = UIImpactFeedbackGenerator(style: .light)
            generator.impactOccurred()

            if board == targetBoard {
                let successGen = UINotificationFeedbackGenerator()
                successGen.notificationOccurred(.success)
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                    onCompleted()
                }
            }
        }
    }
}

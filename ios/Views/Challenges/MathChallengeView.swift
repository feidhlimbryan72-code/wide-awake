import SwiftUI

/// Math problem disarm view enforcing mental cognitive arousal.
public struct MathChallengeView: View {
    public let config: ChallengeConfig
    public let onCompleted: () -> Void

    @State private var currentProblemIndex: Int = 1
    @State private var numA: Int = 0
    @State private var numB: Int = 0
    @State private var operation: String = "+"
    @State private var expectedAnswer: Int = 0
    @State private var enteredInput: String = ""
    @State private var isErrorShake: Bool = false

    public init(config: ChallengeConfig, onCompleted: @escaping () -> Void) {
        self.config = config
        self.onCompleted = onCompleted
    }

    public var body: some View {
        VStack(spacing: 24) {
            // Problem progress
            Text("PROBLEM \(currentProblemIndex) OF \(config.mathProblemCount)")
                .font(.system(size: 14, weight: .bold, design: .monospaced))
                .foregroundColor(.orange)
                .tracking(2)

            // Problem Equation Display
            Text("\(numA) \(operation) \(numB) = ?")
                .font(.system(size: 48, weight: .black, design: .rounded))
                .foregroundColor(.white)
                .padding(.vertical, 8)

            // Input Display Field
            ZStack {
                RoundedRectangle(cornerRadius: 14)
                    .fill(Color(white: 0.12))
                    .frame(height: 64)
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(enteredInput.isEmpty ? Color.gray.opacity(0.3) : Color.orange, lineWidth: 2)
                    )

                Text(enteredInput.isEmpty ? "Tap numbers below" : enteredInput)
                    .font(.system(size: 32, weight: .bold, design: .monospaced))
                    .foregroundColor(enteredInput.isEmpty ? .gray : .white)
            }
            .padding(.horizontal, 24)
            .offset(x: isErrorShake ? -10 : 0)
            .animation(isErrorShake ? Animation.default.repeatCount(4, autoreverses: true).speed(4) : .default, value: isErrorShake)

            // Numeric Keypad
            VStack(spacing: 12) {
                ForEach([[1, 2, 3], [4, 5, 6], [7, 8, 9]], id: \.self) { row in
                    HStack(spacing: 12) {
                        ForEach(row, id: \.self) { digit in
                            KeypadButton(title: "\(digit)") {
                                appendDigit("\(digit)")
                            }
                        }
                    }
                }

                HStack(spacing: 12) {
                    KeypadButton(title: "DEL", color: Color(white: 0.2)) {
                        deleteLast()
                    }
                    KeypadButton(title: "0") {
                        appendDigit("0")
                    }
                    KeypadButton(title: "ENTER", color: .orange) {
                        submitAnswer()
                    }
                }
            }
            .padding(.horizontal, 24)
        }
        .onAppear {
            generateProblem()
        }
    }

    private func appendDigit(_ digit: String) {
        guard enteredInput.count < 6 else { return }
        enteredInput.append(digit)
    }

    private func deleteLast() {
        if !enteredInput.isEmpty {
            enteredInput.removeLast()
        }
    }

    private func submitAnswer() {
        guard let answer = Int(enteredInput) else { return }

        if answer == expectedAnswer {
            enteredInput = ""
            if currentProblemIndex >= config.mathProblemCount {
                onCompleted()
            } else {
                currentProblemIndex += 1
                generateProblem()
            }
        } else {
            // Incorrect: trigger shake and clear input
            isErrorShake = true
            let generator = UINotificationFeedbackGenerator()
            generator.notificationOccurred(.error)
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                self.isErrorShake = false
                self.enteredInput = ""
            }
        }
    }

    private func generateProblem() {
        switch config.mathDifficulty {
        case .easy:
            numA = Int.random(in: 12...49)
            numB = Int.random(in: 11...49)
            operation = "+"
            expectedAnswer = numA + numB
        case .medium:
            let isMultiplication = Bool.random()
            if isMultiplication {
                numA = Int.random(in: 6...14)
                numB = Int.random(in: 6...12)
                operation = "×"
                expectedAnswer = numA * numB
            } else {
                numA = Int.random(in: 45...99)
                numB = Int.random(in: 25...85)
                operation = "+"
                expectedAnswer = numA + numB
            }
        case .hard:
            numA = Int.random(in: 12...25)
            numB = Int.random(in: 12...22)
            operation = "×"
            expectedAnswer = numA * numB
        }
    }
}

private struct KeypadButton: View {
    let title: String
    var color: Color = Color(white: 0.16)
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 22, weight: .bold, design: .rounded))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 60)
                .background(color)
                .cornerRadius(14)
        }
    }
}

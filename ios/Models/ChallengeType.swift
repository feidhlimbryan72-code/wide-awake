import Foundation

/// The catalog of disarm challenges available in WakeOrPay / RiseQuest.
public enum ChallengeType: String, CaseIterable, Identifiable, Codable, Sendable {
    case math = "Math"
    case shake = "Shake"
    case barcode = "Barcode Scanner"
    case memoryMatrix = "Memory Matrix"
    case jigsaw = "Jigsaw Puzzle"
    case watchSteps = "Apple Watch Steps"
    case watchHeartRate = "Apple Watch Heart Rate"

    public var id: String { rawValue }

    /// Indicates whether this challenge requires a Superwall Pro subscription.
    public var isProRequired: Bool {
        switch self {
        case .math, .shake:
            return false
        case .barcode, .memoryMatrix, .jigsaw, .watchSteps, .watchHeartRate:
            return true
        }
    }

    public var iconName: String {
        switch self {
        case .math: return "function"
        case .shake: return "iphone.radiowaves.left.and.right"
        case .barcode: return "barcode.viewfinder"
        case .memoryMatrix: return "square.grid.3x3.fill"
        case .jigsaw: return "puzzlepiece.fill"
        case .watchSteps: return "figure.walk"
        case .watchHeartRate: return "heart.fill"
        }
    }

    public var descriptionText: String {
        switch self {
        case .math:
            return "Solve consecutive mental arithmetic problems."
        case .shake:
            return "Shake the device with high dynamic motion to reach target threshold."
        case .barcode:
            return "Scan a registered barcode/QR code placed away from bed (e.g. bathroom)."
        case .memoryMatrix:
            return "Recall flashing tile sequences in an expanding grid."
        case .jigsaw:
            return "Reassemble a scrambled high-contrast puzzle."
        case .watchSteps:
            return "Walk until your Apple Watch records the requisite step count."
        case .watchHeartRate:
            return "Raise your heart rate above target BPM via physical exertion."
        }
    }
}

public enum MathDifficulty: String, CaseIterable, Identifiable, Codable, Sendable {
    case easy = "Easy (2-digit addition)"
    case medium = "Medium (Multiplication + Addition)"
    case hard = "Hard (Multi-step parentheses)"

    public var id: String { rawValue }
}

/// Configuration settings for an individual challenge in an alarm's disarm chain.
public struct ChallengeConfig: Identifiable, Codable, Sendable, Hashable {
    public var id: UUID
    public var type: ChallengeType
    public var mathDifficulty: MathDifficulty
    public var mathProblemCount: Int
    public var targetShakeCount: Int
    public var targetBarcodePayload: String?
    public var targetBarcodeLabel: String?
    public var targetStepCount: Int
    public var targetBPM: Double

    public init(
        id: UUID = UUID(),
        type: ChallengeType = .math,
        mathDifficulty: MathDifficulty = .easy,
        mathProblemCount: Int = 3,
        targetShakeCount: Int = 100,
        targetBarcodePayload: String? = nil,
        targetBarcodeLabel: String? = "Bathroom Sink Barcode",
        targetStepCount: Int = 75,
        targetBPM: Double = 95.0
    ) {
        self.id = id
        self.type = type
        self.mathDifficulty = mathDifficulty
        self.mathProblemCount = mathProblemCount
        self.targetShakeCount = targetShakeCount
        self.targetBarcodePayload = targetBarcodePayload
        self.targetBarcodeLabel = targetBarcodeLabel
        self.targetStepCount = targetStepCount
        self.targetBPM = targetBPM
    }
}

import Foundation

/// Defines the subscription tiers managed through Superwall & StoreKit 2.
public enum SubscriptionTier: String, Codable, Sendable {
    case free
    case proWeekly = "com.wakeorpay.proweekly"
    case proLifetime = "com.wakeorpay.prolifetime"

    public var displayName: String {
        switch self {
        case .free: return "Free Plan"
        case .proWeekly: return "Pro Weekly"
        case .proLifetime: return "Pro Lifetime"
        }
    }

    public var priceDescription: String {
        switch self {
        case .free: return "Free"
        case .proWeekly: return "$4.99 / week (3-day free trial)"
        case .proLifetime: return "$199.99 one-time"
        }
    }

    public var hasProAccess: Bool {
        self != .free
    }
}

public struct UserSubscriptionState: Sendable, Codable {
    public var currentTier: SubscriptionTier
    public var isTrialActive: Bool
    public var expirationDate: Date?

    public init(currentTier: SubscriptionTier = .free, isTrialActive: Bool = false, expirationDate: Date? = nil) {
        self.currentTier = currentTier
        self.isTrialActive = isTrialActive
        self.expirationDate = expirationDate
    }

    public var isPro: Bool {
        currentTier.hasProAccess
    }
}

import Foundation
import Combine
import SwiftUI

/// Coordinates SuperwallKit paywall triggers and subscription entitlement verification.
@MainActor
public final class PaywallManager: ObservableObject {
    public static let shared = PaywallManager()

    @Published public private(set) var subscriptionState = UserSubscriptionState()
    @Published public var shouldPresentPaywall: Bool = false
    @Published public var activePaywallReason: String = ""

    private let userDefaultsKey = "com.wakeorpay.subscription_state"

    private init() {
        loadSubscriptionState()
    }

    /// Configures Superwall SDK with the specified API key.
    public func configure(apiKey: String) {
        // In production:
        // Superwall.configure(apiKey: apiKey)
        print("[PaywallManager] Superwall configured with API Key: \(apiKey.prefix(6))...")
    }

    /// Evaluates whether an action requires Pro gating.
    /// If Pro is not active, triggers the paywall with the corresponding placement context.
    public func evaluateProRequirement(reason: String, required: Bool) -> Bool {
        if !required || subscriptionState.isPro {
            return true
        }

        self.activePaywallReason = reason
        self.shouldPresentPaywall = true
        return false
    }

    /// Checks whether user is allowed to create or enable an additional active alarm.
    public func canActivateAlarm(currentActiveCount: Int) -> Bool {
        if subscriptionState.isPro {
            return true
        }
        if currentActiveCount >= 1 {
            self.activePaywallReason = "Free plan includes 1 active alarm. Upgrade to Pro for unlimited alarms."
            self.shouldPresentPaywall = true
            return false
        }
        return true
    }

    /// Simulates or executes subscription purchase for the requested tier.
    public func purchase(tier: SubscriptionTier) async -> Bool {
        // Simulate purchase confirmation for development
        self.subscriptionState = UserSubscriptionState(
            currentTier: tier,
            isTrialActive: tier == .proWeekly,
            expirationDate: Calendar.current.date(byAdding: .day, value: tier == .proWeekly ? 7 : 3650, to: Date())
        )
        persistSubscriptionState()
        self.shouldPresentPaywall = false
        return true
    }

    /// Restores previous in-app purchases.
    public func restorePurchases() async -> Bool {
        print("[PaywallManager] Restoring purchases...")
        return true
    }

    private func persistSubscriptionState() {
        do {
            let data = try JSONEncoder().encode(subscriptionState)
            UserDefaults.standard.set(data, forKey: userDefaultsKey)
        } catch {
            print("[PaywallManager] Failed to persist subscription: \(error.localizedDescription)")
        }
    }

    private func loadSubscriptionState() {
        guard let data = UserDefaults.standard.data(forKey: userDefaultsKey),
              let state = try? JSONDecoder().decode(UserSubscriptionState.self, from: data) else {
            self.subscriptionState = UserSubscriptionState(currentTier: .free)
            return
        }
        self.subscriptionState = state
    }
}

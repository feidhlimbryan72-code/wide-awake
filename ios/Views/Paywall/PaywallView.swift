import SwiftUI

/// Premium Superwall monetization paywall offering Weekly Free Trial and Lifetime tiers.
public struct PaywallView: View {
    @ObservedObject var paywallManager = PaywallManager.shared
    @Environment(\.dismiss) private var dismiss

    @State private var selectedTier: SubscriptionTier = .proWeekly
    @State private var isProcessing: Bool = false

    public init() {}

    public var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            VStack(spacing: 0) {
                // Top close button
                HStack {
                    Spacer()
                    Button(action: {
                        paywallManager.shouldPresentPaywall = false
                        dismiss()
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 28))
                            .foregroundColor(.gray.opacity(0.6))
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 16)
                }

                ScrollView(showsIndicators: false) {
                    VStack(spacing: 24) {
                        // Badge
                        HStack(spacing: 6) {
                            Image(systemName: "bolt.shield.fill")
                                .foregroundColor(.yellow)
                            Text("RISEQUEST UNLIMITED PRO")
                                .font(.system(size: 12, weight: .black, design: .monospaced))
                                .foregroundColor(.yellow)
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 6)
                        .background(Color.yellow.opacity(0.12))
                        .cornerRadius(20)

                        // Headline
                        Text("Never Over-Sleep Again.\nGuaranteed.")
                            .font(.system(size: 30, weight: .black, design: .rounded))
                            .foregroundColor(.white)
                            .multilineTextAlignment(.center)

                        if !paywallManager.activePaywallReason.isEmpty {
                            Text(paywallManager.activePaywallReason)
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(.orange)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 24)
                        }

                        // Feature Grid
                        VStack(alignment: .leading, spacing: 14) {
                            ProFeatureRow(icon: "bell.badge.fill", title: "Unlimited Alarms", description: "Schedule recurring wakeups for every day of the week.")
                            ProFeatureRow(icon: "barcode.viewfinder", title: "Location Anchors", description: "Force yourself out of bed to scan bathroom or coffee barcodes.")
                            ProFeatureRow(icon: "heart.fill", title: "Apple Watch Biometrics", description: "Silences only after achieving target step counts or BPM thresholds.")
                            ProFeatureRow(icon: "flame.fill", title: "90s Escalation Shockwaves", description: "High-voltage volume and haptics if you try to ignore the siren.")
                        }
                        .padding(20)
                        .background(Color(white: 0.1))
                        .cornerRadius(18)
                        .padding(.horizontal, 20)

                        // Subscription Offer Cards
                        VStack(spacing: 12) {
                            // Pro Weekly Tier (Recommended)
                            OfferCard(
                                title: "Pro Weekly",
                                subtitle: "3-Day Free Trial, then $4.99 / week",
                                badge: "MOST POPULAR",
                                isSelected: selectedTier == .proWeekly
                            ) {
                                selectedTier = .proWeekly
                            }

                            // Pro Lifetime Tier
                            OfferCard(
                                title: "Lifetime Access",
                                subtitle: "$199.99 one-time payment",
                                badge: "BEST VALUE",
                                isSelected: selectedTier == .proLifetime
                            ) {
                                selectedTier = .proLifetime
                            }
                        }
                        .padding(.horizontal, 20)

                        // Primary Action CTA
                        Button(action: {
                            Task {
                                isProcessing = true
                                _ = await paywallManager.purchase(tier: selectedTier)
                                isProcessing = false
                                dismiss()
                            }
                        }) {
                            HStack {
                                if isProcessing {
                                    ProgressView().tint(.black)
                                } else {
                                    Text(selectedTier == .proWeekly ? "START 3-DAY FREE TRIAL" : "UNLOCK LIFETIME ACCESS")
                                        .font(.system(size: 16, weight: .black, design: .rounded))
                                        .tracking(1)
                                }
                            }
                            .foregroundColor(.black)
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(
                                LinearGradient(colors: [Color.yellow, Color.orange], startPoint: .leading, endPoint: .trailing)
                            )
                            .cornerRadius(16)
                            .shadow(color: .orange.opacity(0.4), radius: 10, y: 4)
                        }
                        .padding(.horizontal, 20)
                        .disabled(isProcessing)

                        // Restore and Legal
                        HStack(spacing: 16) {
                            Button("Restore Purchases") {
                                Task { await paywallManager.restorePurchases() }
                            }
                            Text("•")
                            Button("Terms of Service") {}
                            Text("•")
                            Button("Privacy Policy") {}
                        }
                        .font(.system(size: 11, weight: .regular))
                        .foregroundColor(.gray)
                        .padding(.bottom, 24)
                    }
                }
            }
        }
    }
}

private struct ProFeatureRow: View {
    let icon: String
    let title: String
    let description: String

    var body: some View {
        HStack(alignment: .top, spacing: 14) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(.yellow)
                .frame(width: 26)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(.white)
                Text(description)
                    .font(.system(size: 13, weight: .regular))
                    .foregroundColor(.gray)
            }
        }
    }
}

private struct OfferCard: View {
    let title: String
    let subtitle: String
    let badge: String
    let isSelected: Bool
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 8) {
                        Text(title)
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                        Text(badge)
                            .font(.system(size: 9, weight: .black))
                            .foregroundColor(.black)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color.yellow)
                            .cornerRadius(4)
                    }
                    Text(subtitle)
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(.gray)
                }

                Spacer()

                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                    .font(.system(size: 22))
                    .foregroundColor(isSelected ? .yellow : .gray.opacity(0.5))
            }
            .padding(16)
            .background(Color(white: 0.12))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(isSelected ? Color.yellow : Color.clear, lineWidth: 2)
            )
            .cornerRadius(14)
        }
    }
}

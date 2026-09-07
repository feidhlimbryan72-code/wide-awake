package com.wakeorpay.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class SubscriptionTier(
    val productId: String,
    val title: String,
    val priceDescription: String
) {
    FREE("free", "Free", "Free"),
    PRO_WEEKLY("com.wakeorpay.proweekly", "Pro Weekly", "$4.99 / week (3-day free trial)"),
    PRO_LIFETIME("com.wakeorpay.prolifetime", "Lifetime Access", "$199.99 one-time");

    val isPro: Boolean
        get() = this != FREE
}

@Serializable
data class UserSubscriptionState(
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val isTrialActive: Boolean = false,
    val expiresAtEpochMs: Long? = null
) {
    val hasProAccess: Boolean
        get() = tier.isPro
}

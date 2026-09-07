package com.wakeorpay.app.service

import android.content.Context
import com.wakeorpay.app.model.SubscriptionTier
import com.wakeorpay.app.model.UserSubscriptionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Manages Superwall integration and subscription entitlement gating on Android.
 */
object PaywallManager {

    private const val PREFS_NAME = "wakeorpay_paywall_prefs"
    private const val KEY_SUBSCRIPTION = "subscription_state"

    private val _subscriptionState = MutableStateFlow(UserSubscriptionState())
    val subscriptionState: StateFlow<UserSubscriptionState> = _subscriptionState.asStateFlow()

    private val _shouldShowPaywall = MutableStateFlow(false)
    val shouldShowPaywall: StateFlow<Boolean> = _shouldShowPaywall.asStateFlow()

    private val _paywallReason = MutableStateFlow("")
    val paywallReason: StateFlow<String> = _paywallReason.asStateFlow()

    private var appContext: Context? = null

    fun init(context: Context, apiKey: String) {
        appContext = context.applicationContext
        loadSubscriptionState()
        // Superwall SDK configuration:
        // Superwall.configure(context, apiKey)
    }

    fun evaluateProRequirement(reason: String, required: Boolean): Boolean {
        if (!required || _subscriptionState.value.hasProAccess) {
            return true
        }
        _paywallReason.value = reason
        _shouldShowPaywall.value = true
        return false
    }

    fun canActivateAlarm(currentActiveCount: Int): Boolean {
        if (_subscriptionState.value.hasProAccess) {
            return true
        }
        if (currentActiveCount >= 1) {
            _paywallReason.value = "Free plan includes 1 active alarm. Upgrade to Pro for unlimited alarms."
            _shouldShowPaywall.value = true
            return false
        }
        return true
    }

    fun dismissPaywall() {
        _shouldShowPaywall.value = false
    }

    fun purchase(tier: SubscriptionTier) {
        val newState = UserSubscriptionState(
            tier = tier,
            isTrialActive = tier == SubscriptionTier.PRO_WEEKLY,
            expiresAtEpochMs = System.currentTimeMillis() + if (tier == SubscriptionTier.PRO_WEEKLY) 7L * 86400000L else 3650L * 86400000L
        )
        _subscriptionState.value = newState
        _shouldShowPaywall.value = false
        saveSubscriptionState(newState)
    }

    private fun saveSubscriptionState(state: UserSubscriptionState) {
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = Json.encodeToString(state)
            prefs.edit().putString(KEY_SUBSCRIPTION, json).apply()
        }
    }

    private fun loadSubscriptionState() {
        appContext?.let { ctx ->
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_SUBSCRIPTION, null)
            if (json != null) {
                try {
                    _subscriptionState.value = Json.decodeFromString<UserSubscriptionState>(json)
                } catch (e: Exception) {
                    _subscriptionState.value = UserSubscriptionState()
                }
            }
        }
    }
}

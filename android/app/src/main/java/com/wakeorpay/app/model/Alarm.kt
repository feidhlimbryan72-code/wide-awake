package com.wakeorpay.app.model

import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.UUID

@Serializable
data class AlarmSound(
    val id: String,
    val name: String,
    val resourceName: String? = null,
    val isPro: Boolean = false
) {
    companion object {
        val SIREN_AIR_RAID = AlarmSound("siren_air_raid", "Air Raid Siren", "siren_air_raid", false)
        val NUCLEAR_KLAXON = AlarmSound("nuclear_klaxon", "Nuclear Reactor Klaxon", "nuclear_klaxon", false)
        val MILITARY_REVEILLE = AlarmSound("military_reveille", "Aggressive Reveille", "military_reveille", true)
        val SYNTHETIC_SHOCK = AlarmSound("synthetic_shock", "Synthetic Piercing Tone", null, true)

        val ALL_SOUNDS = listOf(SIREN_AIR_RAID, NUCLEAR_KLAXON, MILITARY_REVEILLE, SYNTHETIC_SHOCK)
    }
}

@Serializable
data class Alarm(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int = 7,
    val minute: Int = 0,
    val label: String = "Wake Up",
    val isEnabled: Boolean = true,
    val repeatDays: Set<Int> = emptySet(), // DayOfWeek 1..7 (Monday=1, Sunday=7)
    val sound: AlarmSound = AlarmSound.SIREN_AIR_RAID,
    val volume: Float = 0.85f,
    val snoozeDurationMinutes: Int = 5,
    val isSnoozePenaltyEnabled: Boolean = true,
    val chosenChallenge: ChallengeConfig = ChallengeConfig(type = ChallengeType.MATH),
    val allowUserChoiceOnWake: Boolean = true,
    val challengeChain: List<ChallengeConfig> = listOf(ChallengeConfig(type = ChallengeType.MATH))
) {
    val requiresPro: Boolean
        get() = sound.isPro || chosenChallenge.type.isProRequired || challengeChain.any { it.type.isProRequired }

    val formattedTime: String
        get() {
            val period = if (hour < 12) "AM" else "PM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            return String.format("%02d:%02d %s", displayHour, minute, period)
        }

    val repeatSummary: String
        get() {
            if (repeatDays.isEmpty()) return "Never"
            if (repeatDays.size == 7) return "Every day"
            if (repeatDays == setOf(1, 2, 3, 4, 5)) return "Weekdays"
            if (repeatDays == setOf(6, 7)) return "Weekends"
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            return repeatDays.sorted().map { days[it - 1] }.joinToString(", ")
        }

    fun nextTriggerDateTime(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        val targetTime = LocalTime.of(hour, minute, 0)

        if (repeatDays.isEmpty()) {
            val candidateToday = LocalDateTime.of(now.toLocalDate(), targetTime)
            return if (candidateToday.isAfter(now)) candidateToday else candidateToday.plusDays(1)
        }

        return repeatDays.map { dayInt ->
            val dayOfWeek = DayOfWeek.of(dayInt)
            var candidate = now.with(TemporalAdjusters.nextOrSame(dayOfWeek)).with(targetTime)
            if (candidate.isBefore(now) || candidate.isEqual(now)) {
                candidate = candidate.plusWeeks(1)
            }
            candidate
        }.minByOrNull { it } ?: now.plusDays(1).with(targetTime)
    }
}

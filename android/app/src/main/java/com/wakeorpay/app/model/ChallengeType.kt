package com.wakeorpay.app.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class ChallengeType(val displayName: String, val isProRequired: Boolean) {
    MATH("Math", false),
    SHAKE("Shake", false),
    BARCODE("Barcode Scanner", true),
    MEMORY_MATRIX("Memory Matrix", true),
    JIGSAW("Jigsaw Puzzle", true),
    WATCH_STEPS("Watch Steps", true),
    WATCH_HEART_RATE("Watch Heart Rate", true)
}

@Serializable
enum class MathDifficulty(val label: String) {
    EASY("Easy (2-digit addition)"),
    MEDIUM("Medium (Multiplication + Addition)"),
    HARD("Hard (Multi-step calculations)")
}

@Serializable
data class ChallengeConfig(
    val id: String = UUID.randomUUID().toString(),
    val type: ChallengeType = ChallengeType.MATH,
    val mathDifficulty: MathDifficulty = MathDifficulty.EASY,
    val mathProblemCount: Int = 3,
    val targetShakeCount: Int = 100,
    val targetBarcodePayload: String? = null,
    val targetBarcodeLabel: String? = "Bathroom Sink Barcode",
    val targetStepCount: Int = 75,
    val targetBPM: Double = 95.0
)

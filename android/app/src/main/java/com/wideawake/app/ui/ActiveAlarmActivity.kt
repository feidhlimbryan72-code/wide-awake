package com.wideawake.app.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wideawake.app.model.Alarm
import com.wideawake.app.model.ChallengeConfig
import com.wideawake.app.model.ChallengeType
import com.wideawake.app.service.AlarmAudioService
import com.wideawake.app.ui.challenges.BarcodeScannerScreen
import com.wideawake.app.ui.challenges.MathChallengeScreen
import com.wideawake.app.ui.challenges.ShakeChallengeScreen
import com.wideawake.app.ui.theme.Black
import com.wideawake.app.ui.theme.DarkCard
import com.wideawake.app.ui.theme.PenaltyRed
import com.wideawake.app.ui.theme.PrimaryOrange
import com.wideawake.app.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

/**
 * Full-screen lock-in disarm Activity.
 * Runs directly on top of the lock screen and keyguard.
 * Prevents user back navigation until the chosen challenge is cleared.
 * Enables user to choose or switch between disarm challenges in real-time.
 */
class ActiveAlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen and display over keyguard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        // Intercept and disable back button during active alarm
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Intentionally no-op to lock the user into solving the challenge
            }
        })

        val alarmJson = intent.getStringExtra(EXTRA_ALARM_JSON)
        val alarm = if (alarmJson != null) {
            try { Json.decodeFromString<Alarm>(alarmJson) } catch (e: Exception) { Alarm() }
        } else {
            Alarm()
        }

        setContent {
            ActiveAlarmScreen(
                alarm = alarm,
                onDisarmed = {
                    stopAlarmService()
                    finish()
                }
            )
        }
    }

    private fun stopAlarmService() {
        val stopIntent = Intent(this, AlarmAudioService::class.java).apply {
            action = AlarmAudioService.ACTION_STOP_ALARM
        }
        startService(stopIntent)
    }

    companion object {
        const val EXTRA_ALARM_JSON = "extra_alarm_json"
    }
}

@Composable
private fun ActiveAlarmScreen(
    alarm: Alarm,
    onDisarmed: () -> Unit
) {
    var activeChallenge by remember { mutableStateOf(alarm.chosenChallenge) }
    var penaltySeconds by remember { mutableStateOf(90) }
    var isPenaltyActive by remember { mutableStateOf(false) }

    // Countdown for 90-second penalty escalation
    LaunchedEffect(Unit) {
        while (penaltySeconds > 0) {
            delay(1000)
            penaltySeconds--
        }
        isPenaltyActive = true
    }

    // Flash background red on penalty escalation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val backgroundColor = if (isPenaltyActive) {
        PenaltyRed.copy(alpha = pulseAlpha)
    } else {
        Black
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isPenaltyActive) PenaltyRed else PrimaryOrange, CircleShape)
                    )
                    Text(
                        text = if (isPenaltyActive) "MAX PENALTY ENGAGED" else "ALARM ACTIVE",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = if (isPenaltyActive) PenaltyRed else PrimaryOrange,
                        letterSpacing = 2.sp
                    )
                }

                Text(
                    text = alarm.label.uppercase(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = White
                )

                if (!isPenaltyActive) {
                    Text(
                        text = "Penalty escalation in ${penaltySeconds}s",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                } else {
                    Text(
                        text = "MAX VOLUME + INTENSE VIBRATIONS",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = PenaltyRed
                    )
                }

                LinearProgressIndicator(
                    progress = if (isPenaltyActive) 1f else (90 - penaltySeconds) / 90f,
                    color = if (isPenaltyActive) PenaltyRed else PrimaryOrange,
                    trackColor = Color(0xFF222222),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(6.dp)
                )
            }

            // Quick Challenge Switcher (allows user to choose which challenge to solve)
            if (alarm.allowUserChoiceOnWake) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "CHOOSE DISARM CHALLENGE",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Math", ChallengeType.MATH, ChallengeConfig(type = ChallengeType.MATH)),
                            Triple("Shake", ChallengeType.SHAKE, ChallengeConfig(type = ChallengeType.SHAKE)),
                            Triple("Barcode", ChallengeType.BARCODE, ChallengeConfig(type = ChallengeType.BARCODE))
                        ).forEach { (name, type, cfg) ->
                            val isSelected = activeChallenge.type == type
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) PrimaryOrange else DarkCard,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) PrimaryOrange else Color(0xFF333333),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { activeChallenge = cfg }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Black else White
                                )
                            }
                        }
                    }
                }
            }

            // Active Challenge Container
            key(activeChallenge.type) {
                when (activeChallenge.type) {
                    ChallengeType.MATH -> {
                        MathChallengeScreen(config = activeChallenge) {
                            onDisarmed()
                        }
                    }
                    ChallengeType.SHAKE -> {
                        ShakeChallengeScreen(config = activeChallenge) {
                            onDisarmed()
                        }
                    }
                    ChallengeType.BARCODE -> {
                        BarcodeScannerScreen(config = activeChallenge) {
                            onDisarmed()
                        }
                    }
                    else -> {
                        MathChallengeScreen(config = activeChallenge) {
                            onDisarmed()
                        }
                    }
                }
            }

            // Security Note
            Text(
                text = "Audio will not silence until challenge is solved.",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

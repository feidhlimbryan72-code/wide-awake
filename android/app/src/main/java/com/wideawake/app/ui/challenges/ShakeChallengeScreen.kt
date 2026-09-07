package com.wideawake.app.ui.challenges

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wideawake.app.model.ChallengeConfig
import com.wideawake.app.service.MotionSensorService
import com.wideawake.app.ui.theme.AccentYellow
import com.wideawake.app.ui.theme.PrimaryOrange
import com.wideawake.app.ui.theme.White

@Composable
fun ShakeChallengeScreen(
    config: ChallengeConfig,
    onCompleted: () -> Unit
) {
    val context = LocalContext.current
    val sensorService = remember { MotionSensorService(context) }

    val currentShakeCount by sensorService.shakeCount.collectAsState()
    val isCompleted by sensorService.isCompleted.collectAsState()

    val progress = (currentShakeCount.toFloat() / config.targetShakeCount.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "shakeProgress")

    LaunchedEffect(Unit) {
        sensorService.startTracking(config.targetShakeCount)
    }

    DisposableEffect(Unit) {
        onDispose {
            sensorService.stopTracking()
        }
    }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            onCompleted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Text(
            text = "SHAKE VIGOROUSLY",
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            color = AccentYellow,
            letterSpacing = 3.sp
        )

        // Circular progress ring
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 16.dp.toPx()
                // Background track
                drawCircle(
                    color = Color(0xFF222222),
                    style = Stroke(width = strokeWidth)
                )
                // Progress arc
                drawArc(
                    brush = Brush.sweepGradient(listOf(AccentYellow, PrimaryOrange, Color.Red)),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = null,
                    tint = AccentYellow,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "$currentShakeCount",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = White
                )
                Text(
                    text = "OF ${config.targetShakeCount} SHAKES",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }

        Text(
            text = "Micro-shakes or tilting do not register.\nMove your entire body to create dynamic acceleration.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

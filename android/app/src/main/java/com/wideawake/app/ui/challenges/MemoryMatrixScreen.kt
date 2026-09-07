package com.wideawake.app.ui.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wideawake.app.model.ChallengeConfig
import com.wideawake.app.ui.theme.DarkCard
import com.wideawake.app.ui.theme.White
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun MemoryMatrixScreen(
    config: ChallengeConfig,
    onCompleted: () -> Unit
) {
    var activePattern by remember { mutableStateOf(setOf<Int>()) }
    var selectedTiles by remember { mutableStateOf(setOf<Int>()) }
    var isShowingPattern by remember { mutableStateOf(true) }
    var countdown by remember { mutableStateOf(3) }
    var isError by remember { mutableStateOf(false) }

    fun startRound() {
        selectedTiles = emptySet()
        isShowingPattern = true
        countdown = 3
        val set = mutableSetOf<Int>()
        while (set.size < 4) {
            set.add(Random.nextInt(0, 9))
        }
        activePattern = set
    }

    LaunchedEffect(Unit) {
        startRound()
    }

    LaunchedEffect(isShowingPattern) {
        if (isShowingPattern) {
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            isShowingPattern = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "MEMORY MATRIX",
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            color = Color(0xFF00E5FF),
            letterSpacing = 2.sp
        )

        Text(
            text = if (isShowingPattern) "MEMORIZE ACTIVE TILES (${countdown}s)" else "TAP ALL 4 ACTIVE TILES",
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (isShowingPattern) Color(0xFFFFD600) else White
        )

        // 3x3 Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (row in 0 until 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        val isHighlighted = if (isShowingPattern) activePattern.contains(index) else selectedTiles.contains(index)

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    if (isHighlighted) Color(0xFF00E5FF) else DarkCard,
                                    RoundedCornerShape(14.dp)
                                )
                                .border(
                                    2.dp,
                                    if (isError) Color.Red else if (isHighlighted) Color.White else Color(0xFF333333),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = !isShowingPattern) {
                                    if (activePattern.contains(index)) {
                                        val newSet = selectedTiles + index
                                        selectedTiles = newSet
                                        if (newSet == activePattern) {
                                            onCompleted()
                                        }
                                    } else {
                                        isError = true
                                        startRound()
                                    }
                                }
                        )
                    }
                }
            }
        }

        Text(
            text = "Recalling spatial visual patterns quickly activates cognitive arousal.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

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
import kotlin.math.abs

@Composable
fun JigsawChallengeScreen(
    config: ChallengeConfig,
    onCompleted: () -> Unit
) {
    var board by remember { mutableStateOf(listOf(1, 2, 3, 4, 5, 0, 7, 8, 6)) }
    val targetBoard = listOf(1, 2, 3, 4, 5, 6, 7, 8, 0)

    fun slideTile(index: Int) {
        val emptyIndex = board.indexOf(0)
        if (emptyIndex == -1) return

        val row = index / 3
        val col = index % 3
        val emptyRow = emptyIndex / 3
        val emptyCol = emptyIndex % 3

        val isAdjacent = (abs(row - emptyRow) == 1 && col == emptyCol) ||
                         (abs(col - emptyCol) == 1 && row == emptyRow)

        if (isAdjacent) {
            val newBoard = board.toMutableList()
            newBoard[emptyIndex] = board[index]
            newBoard[index] = 0
            board = newBoard

            if (newBoard == targetBoard) {
                onCompleted()
            }
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
            text = "JIGSAW PUZZLE",
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            color = Color(0xFFD500F9),
            letterSpacing = 2.sp
        )

        Text(
            text = "SLIDE TILES IN ORDER (1 TO 8)",
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // 3x3 Tile Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in 0 until 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        val value = board[index]

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    if (value == 0) Color.Transparent else Color(0xFFD500F9).copy(alpha = 0.85f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.5.dp,
                                    if (value == 0) Color.Transparent else Color.White.copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = value != 0) {
                                    slideTile(index)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (value != 0) {
                                Text(
                                    text = "$value",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Slide the numbered tiles into numerical order with empty slot at bottom-right.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

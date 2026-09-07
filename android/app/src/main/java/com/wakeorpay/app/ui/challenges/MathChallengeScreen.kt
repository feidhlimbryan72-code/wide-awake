package com.wakeorpay.app.ui.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.wakeorpay.app.model.ChallengeConfig
import com.wakeorpay.app.model.MathDifficulty
import com.wakeorpay.app.ui.theme.DarkCard
import com.wakeorpay.app.ui.theme.PrimaryOrange
import com.wakeorpay.app.ui.theme.White
import kotlin.random.Random

@Composable
fun MathChallengeScreen(
    config: ChallengeConfig,
    onCompleted: () -> Unit
) {
    var problemIndex by remember { mutableStateOf(1) }
    var numA by remember { mutableStateOf(0) }
    var numB by remember { mutableStateOf(0) }
    var operation by remember { mutableStateOf("+") }
    var expectedAnswer by remember { mutableStateOf(0) }
    var enteredInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun generateProblem() {
        when (config.mathDifficulty) {
            MathDifficulty.EASY -> {
                numA = Random.nextInt(12, 50)
                numB = Random.nextInt(11, 50)
                operation = "+"
                expectedAnswer = numA + numB
            }
            MathDifficulty.MEDIUM -> {
                if (Random.nextBoolean()) {
                    numA = Random.nextInt(6, 15)
                    numB = Random.nextInt(6, 13)
                    operation = "×"
                    expectedAnswer = numA * numB
                } else {
                    numA = Random.nextInt(45, 100)
                    numB = Random.nextInt(25, 86)
                    operation = "+"
                    expectedAnswer = numA + numB
                }
            }
            MathDifficulty.HARD -> {
                numA = Random.nextInt(12, 26)
                numB = Random.nextInt(12, 23)
                operation = "×"
                expectedAnswer = numA * numB
            }
        }
    }

    LaunchedEffect(Unit) {
        generateProblem()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "PROBLEM $problemIndex OF ${config.mathProblemCount}",
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = PrimaryOrange,
            letterSpacing = 2.sp
        )

        Text(
            text = "$numA $operation $numB = ?",
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            color = White,
            textAlign = TextAlign.Center
        )

        // Input field box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(DarkCard, RoundedCornerShape(14.dp))
                .border(
                    width = 2.dp,
                    color = if (isError) Color.Red else if (enteredInput.isNotEmpty()) PrimaryOrange else Color.DarkGray,
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = enteredInput.ifEmpty { "Tap numbers below" },
                fontSize = 28.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (enteredInput.isEmpty()) Color.Gray else White
            )
        }

        // Numeric Keypad
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("DEL", "0", "ENTER")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { key ->
                        Button(
                            onClick = {
                                isError = false
                                when (key) {
                                    "DEL" -> {
                                        if (enteredInput.isNotEmpty()) {
                                            enteredInput = enteredInput.dropLast(1)
                                        }
                                    }
                                    "ENTER" -> {
                                        val answer = enteredInput.toIntOrNull()
                                        if (answer == expectedAnswer) {
                                            enteredInput = ""
                                            if (problemIndex >= config.mathProblemCount) {
                                                onCompleted()
                                            } else {
                                                problemIndex++
                                                generateProblem()
                                            }
                                        } else {
                                            isError = true
                                            enteredInput = ""
                                        }
                                    }
                                    else -> {
                                        if (enteredInput.length < 6) {
                                            enteredInput += key
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (key) {
                                    "ENTER" -> PrimaryOrange
                                    "DEL" -> Color(0xFF2C2C2C)
                                    else -> DarkCard
                                }
                            )
                        ) {
                            Text(
                                text = key,
                                fontSize = if (key.length > 1) 14.sp else 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                    }
                }
            }
        }
    }
}

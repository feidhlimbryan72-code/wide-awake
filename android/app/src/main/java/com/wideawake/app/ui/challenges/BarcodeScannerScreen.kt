package com.wideawake.app.ui.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.wideawake.app.ui.theme.LaserGreen
import com.wideawake.app.ui.theme.White

@Composable
fun BarcodeScannerScreen(
    config: ChallengeConfig,
    onCompleted: () -> Unit
) {
    var scannedCode by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "SCAN LOCATION ANCHOR",
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = LaserGreen,
            letterSpacing = 2.sp
        )

        Text(
            text = config.targetBarcodeLabel ?: "Registered Anchor Barcode",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = White,
            textAlign = TextAlign.Center
        )

        // Viewfinder box
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(DarkCard, RoundedCornerShape(16.dp))
                .border(2.dp, LaserGreen.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (scannedCode == null) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = LaserGreen.copy(alpha = 0.4f),
                    modifier = Modifier.size(96.dp)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = LaserGreen,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "VERIFIED: $scannedCode",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }
        }

        Text(
            text = "Walk to your designated anchor (bathroom or kitchen sink) to scan the barcode.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        // Testing & Emulator Simulator trigger
        Button(
            onClick = {
                scannedCode = config.targetBarcodePayload ?: "DEV_BARCODE_SINK"
                onCompleted()
            },
            colors = ButtonDefaults.buttonColors(containerColor = LaserGreen),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "Simulate Barcode Scan",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

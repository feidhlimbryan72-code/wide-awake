package com.wakeorpay.app.ui.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeorpay.app.model.SubscriptionTier
import com.wakeorpay.app.service.PaywallManager
import com.wakeorpay.app.ui.theme.*

@Composable
fun SuperwallPaywallScreen(
    reason: String = "",
    onDismiss: () -> Unit
) {
    var selectedTier by remember { mutableStateOf(SubscriptionTier.PRO_WEEKLY) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Dismiss button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }

            // Badge
            Row(
                modifier = Modifier
                    .background(AccentYellow.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = AccentYellow,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "WIDE AWAKE UNLIMITED PRO",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = AccentYellow
                )
            }

            // Headline
            Text(
                text = "Never Over-Sleep Again.\nGuaranteed.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = White,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            if (reason.isNotEmpty()) {
                Text(
                    text = reason,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryOrange,
                    textAlign = TextAlign.Center
                )
            }

            // Features Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(16.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeatureRow(Icons.Default.Alarm, "Unlimited Alarms", "Schedule multiple recurring wakeups throughout the week.")
                FeatureRow(Icons.Default.QrCodeScanner, "Location Anchors", "Force yourself to get up and scan bathroom barcodes.")
                FeatureRow(Icons.Default.DirectionsWalk, "Physical Step Milestones", "Silences only after achieving verified step targets.")
                FeatureRow(Icons.Default.VolumeUp, "90s Escalation Shockwaves", "High-voltage volume and haptics if disarm is ignored.")
            }

            // Subscription Tiers Selection
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TierCard(
                    title = "Pro Weekly",
                    subtitle = "3-Day Free Trial, then $4.99 / week",
                    badge = "MOST POPULAR",
                    isSelected = selectedTier == SubscriptionTier.PRO_WEEKLY,
                    onClick = { selectedTier = SubscriptionTier.PRO_WEEKLY }
                )

                TierCard(
                    title = "Lifetime Access",
                    subtitle = "$199.99 one-time payment",
                    badge = "BEST VALUE",
                    isSelected = selectedTier == SubscriptionTier.PRO_LIFETIME,
                    onClick = { selectedTier = SubscriptionTier.PRO_LIFETIME }
                )
            }

            // Action CTA Button
            Button(
                onClick = {
                    PaywallManager.purchase(selectedTier)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow)
            ) {
                Text(
                    text = if (selectedTier == SubscriptionTier.PRO_WEEKLY) "START 3-DAY FREE TRIAL" else "UNLOCK LIFETIME ACCESS",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Black,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "Cancel anytime in Google Play Store. Terms of Service & Privacy apply.",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentYellow,
            modifier = Modifier.size(22.dp)
        )
        Column {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = White)
            Text(text = description, fontSize = 12.sp, color = LightGray)
        }
    }
}

@Composable
private fun TierCard(
    title: String,
    subtitle: String,
    badge: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) AccentYellow else Color(0xFF333333),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Black,
                        modifier = Modifier
                            .background(AccentYellow, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(text = subtitle, fontSize = 13.sp, color = Color.Gray)
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = AccentYellow)
            )
        }
    }
}

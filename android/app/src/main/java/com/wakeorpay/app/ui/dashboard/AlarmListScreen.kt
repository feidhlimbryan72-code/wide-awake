package com.wakeorpay.app.ui.dashboard

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeorpay.app.model.Alarm
import com.wakeorpay.app.service.AlarmSchedulerReceiver
import com.wakeorpay.app.service.PaywallManager
import com.wakeorpay.app.ui.ActiveAlarmActivity
import com.wakeorpay.app.ui.paywall.SuperwallPaywallScreen
import com.wakeorpay.app.ui.theme.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen() {
    val context = LocalContext.current
    var alarms by remember { mutableStateOf(AlarmSchedulerReceiver.loadAlarms(context)) }
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }
    var isCreatingAlarm by remember { mutableStateOf(false) }

    val subscriptionState by PaywallManager.subscriptionState.collectAsState()
    val showPaywall by PaywallManager.shouldShowPaywall.collectAsState()
    val paywallReason by PaywallManager.paywallReason.collectAsState()

    fun updateAlarms(newList: List<Alarm>) {
        alarms = newList
        AlarmSchedulerReceiver.saveAlarms(context, newList)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wide Awake",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = White
                    )
                },
                actions = {
                    IconButton(onClick = {
                        val activeCount = alarms.count { it.isEnabled }
                        if (PaywallManager.canActivateAlarm(activeCount)) {
                            isCreatingAlarm = true
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Alarm", tint = PrimaryOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Black)
            )
        },
        containerColor = Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Pro Upgrade Pill (if free)
            if (!subscriptionState.hasProAccess) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(DarkCard, RoundedCornerShape(12.dp))
                        .border(1.dp, AccentYellow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable {
                            PaywallManager.evaluateProRequirement("Unlock unlimited alarms and advanced disarm challenges.", true)
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("👑", fontSize = 16.sp)
                            Text(
                                text = "UPGRADE TO PRO — 3 DAYS FREE",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Alarms Configured", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onToggle = { isEnabled ->
                                if (isEnabled) {
                                    val activeCount = alarms.count { it.isEnabled }
                                    if (!PaywallManager.canActivateAlarm(activeCount)) {
                                        return@AlarmCard
                                    }
                                }
                                val updated = alarms.map {
                                    if (it.id == alarm.id) it.copy(isEnabled = isEnabled) else it
                                }
                                updateAlarms(updated)
                                if (isEnabled) {
                                    AlarmSchedulerReceiver.scheduleAlarm(context, alarm.copy(isEnabled = true))
                                } else {
                                    AlarmSchedulerReceiver.cancelAlarm(context, alarm.id)
                                }
                            },
                            onClick = { editingAlarm = alarm },
                            onTestRing = {
                                val intent = Intent(context, ActiveAlarmActivity::class.java).apply {
                                    putExtra(ActiveAlarmActivity.EXTRA_ALARM_JSON, Json.encodeToString(alarm))
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }

        // Create Dialog
        if (isCreatingAlarm) {
            AlarmEditDialog(
                onDismiss = { isCreatingAlarm = false },
                onSave = { newAlarm ->
                    val updated = alarms + newAlarm
                    updateAlarms(updated)
                    if (newAlarm.isEnabled) {
                        AlarmSchedulerReceiver.scheduleAlarm(context, newAlarm)
                    }
                    isCreatingAlarm = false
                }
            )
        }

        // Edit Dialog
        editingAlarm?.let { alarm ->
            AlarmEditDialog(
                initialAlarm = alarm,
                onDismiss = { editingAlarm = null },
                onSave = { updatedAlarm ->
                    val updated = alarms.map { if (it.id == updatedAlarm.id) updatedAlarm else it }
                    updateAlarms(updated)
                    if (updatedAlarm.isEnabled) {
                        AlarmSchedulerReceiver.scheduleAlarm(context, updatedAlarm)
                    } else {
                        AlarmSchedulerReceiver.cancelAlarm(context, updatedAlarm.id)
                    }
                    editingAlarm = null
                }
            )
        }

        // Superwall Paywall Dialog
        if (showPaywall) {
            SuperwallPaywallScreen(
                reason = paywallReason,
                onDismiss = { PaywallManager.dismissPaywall() }
            )
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onTestRing: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = alarm.formattedTime,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (alarm.isEnabled) White else Color.Gray
                    )
                    if (alarm.requiresPro) {
                        Text("👑", fontSize = 14.sp)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = alarm.label, fontSize = 13.sp, color = if (alarm.isEnabled) PrimaryOrange else Color.Gray)
                    Text(text = "•", color = Color.Gray)
                    Text(text = alarm.repeatSummary, fontSize = 12.sp, color = Color.Gray)
                }

                // Chosen Challenge Tag
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alarm.chosenChallenge.type.displayName,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryOrange,
                        modifier = Modifier
                            .background(PrimaryOrange.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )

                    if (alarm.allowUserChoiceOnWake) {
                        Text(
                            text = "CHOICE ON WAKE",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier
                                .background(DarkCard, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryOrange)
                )

                Button(
                    onClick = onTestRing,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentYellow.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "TEST",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = AccentYellow
                    )
                }
            }
        }
    }
}

package com.wakeorpay.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wakeorpay.app.model.*
import com.wakeorpay.app.service.PaywallManager
import com.wakeorpay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditDialog(
    initialAlarm: Alarm? = null,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialAlarm?.hour ?: 7,
        initialMinute = initialAlarm?.minute ?: 0,
        is24Hour = false
    )

    var label by remember { mutableStateOf(initialAlarm?.label ?: "Wake Up") }
    var repeatDays by remember { mutableStateOf(initialAlarm?.repeatDays ?: emptySet()) }
    var selectedSound by remember { mutableStateOf(initialAlarm?.sound ?: AlarmSound.SIREN_AIR_RAID) }
    var isPenaltyEnabled by remember { mutableStateOf(initialAlarm?.isSnoozePenaltyEnabled ?: true) }
    var chosenChallenge by remember {
        mutableStateOf(initialAlarm?.chosenChallenge ?: ChallengeConfig(type = ChallengeType.MATH))
    }
    var allowUserChoiceOnWake by remember {
        mutableStateOf(initialAlarm?.allowUserChoiceOnWake ?: true)
    }

    var showSoundDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Text(
                        text = if (initialAlarm == null) "New Alarm" else "Edit Alarm",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    TextButton(onClick = {
                        val alarm = (initialAlarm ?: Alarm()).copy(
                            hour = timePickerState.hour,
                            minute = timePickerState.minute,
                            label = label,
                            repeatDays = repeatDays,
                            sound = selectedSound,
                            isSnoozePenaltyEnabled = isPenaltyEnabled,
                            chosenChallenge = chosenChallenge,
                            allowUserChoiceOnWake = allowUserChoiceOnWake,
                            challengeChain = listOf(chosenChallenge)
                        )
                        onSave(alarm)
                    }) {
                        Text("Save", color = PrimaryOrange, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Time Picker
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            TimePicker(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors(
                                    clockDialColor = DarkCard,
                                    selectorColor = PrimaryOrange,
                                    timeSelectorSelectedContainerColor = PrimaryOrange,
                                    timeSelectorUnselectedContainerColor = DarkCard,
                                    timeSelectorSelectedContentColor = Black,
                                    timeSelectorUnselectedContentColor = White
                                )
                            )
                        }
                    }

                    // Label input
                    item {
                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text("Alarm Label") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = White,
                                unfocusedTextColor = White
                            )
                        )
                    }

                    // Day of week repetition toggles
                    item {
                        Text("REPEAT DAYS", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        val days = listOf("M" to 1, "T" to 2, "W" to 3, "T" to 4, "F" to 5, "S" to 6, "S" to 7)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            days.forEach { (name, dayNum) ->
                                val isSelected = repeatDays.contains(dayNum)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            if (isSelected) PrimaryOrange else DarkCard,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            repeatDays = if (isSelected) repeatDays - dayNum else repeatDays + dayNum
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Black else White
                                    )
                                }
                            }
                        }
                    }

                    // Sound Selector
                    item {
                        Text("ALARM TONE", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkCard, RoundedCornerShape(10.dp))
                                .clickable { showSoundDropdown = true }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedSound.name, color = White, fontWeight = FontWeight.SemiBold)
                                if (selectedSound.isPro) {
                                    Text("PRO", color = AccentYellow, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            DropdownMenu(
                                expanded = showSoundDropdown,
                                onDismissRequest = { showSoundDropdown = false }
                            ) {
                                AlarmSound.ALL_SOUNDS.forEach { sound ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(sound.name)
                                                if (sound.isPro) Text("👑", color = AccentYellow)
                                            }
                                        },
                                        onClick = {
                                            if (sound.isPro && !PaywallManager.evaluateProRequirement("Pro sound", true)) {
                                                showSoundDropdown = false
                                                return@DropdownMenuItem
                                            }
                                            selectedSound = sound
                                            showSoundDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // CHOOSE DISARM CHALLENGE SECTION
                    item {
                        Text(
                            text = "CHOOSE DISARM CHALLENGE",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryOrange
                        )
                        Text(
                            text = "Select which challenge must be solved to turn off the alarm:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChallengeType.values().forEach { type ->
                                val isSelected = chosenChallenge.type == type
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkCard, RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) PrimaryOrange else Color(0xFF333333),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            if (type.isProRequired && !PaywallManager.evaluateProRequirement("Unlocking ${type.displayName} requires Pro", true)) {
                                                return@clickable
                                            }
                                            chosenChallenge = chosenChallenge.copy(type = type)
                                        }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = type.displayName,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = White
                                                )
                                                if (type.isProRequired) {
                                                    Text("👑", fontSize = 12.sp)
                                                }
                                            }
                                        }

                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                if (type.isProRequired && !PaywallManager.evaluateProRequirement("Unlocking ${type.displayName} requires Pro", true)) {
                                                    return@RadioButton
                                                }
                                                chosenChallenge = chosenChallenge.copy(type = type)
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryOrange)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // CUSTOMIZE CHOSEN CHALLENGE PARAMETERS
                    item {
                        Text(
                            text = "CUSTOMIZE ${chosenChallenge.type.displayName.uppercase()} SETTINGS",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkCard, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            when (chosenChallenge.type) {
                                ChallengeType.MATH -> {
                                    Text("Difficulty: ${chosenChallenge.mathDifficulty.label}", color = White, fontSize = 13.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        MathDifficulty.values().forEach { diff ->
                                            val isDiffSelected = chosenChallenge.mathDifficulty == diff
                                            Button(
                                                onClick = { chosenChallenge = chosenChallenge.copy(mathDifficulty = diff) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isDiffSelected) PrimaryOrange else Color(0xFF2C2C2C)
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = diff.name,
                                                    fontSize = 11.sp,
                                                    color = if (isDiffSelected) Black else White
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Problem Count", color = White, fontSize = 14.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf(1, 3, 5).forEach { count ->
                                                val isCountSelected = chosenChallenge.mathProblemCount == count
                                                Button(
                                                    onClick = { chosenChallenge = chosenChallenge.copy(mathProblemCount = count) },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isCountSelected) PrimaryOrange else Color(0xFF2C2C2C)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("$count", color = if (isCountSelected) Black else White)
                                                }
                                            }
                                        }
                                    }
                                }

                                ChallengeType.SHAKE -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Required Shakes", color = White, fontSize = 14.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf(25, 50, 100, 200).forEach { shakes ->
                                                val isSelected = chosenChallenge.targetShakeCount == shakes
                                                Button(
                                                    onClick = { chosenChallenge = chosenChallenge.copy(targetShakeCount = shakes) },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isSelected) PrimaryOrange else Color(0xFF2C2C2C)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("$shakes", color = if (isSelected) Black else White, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }

                                ChallengeType.BARCODE -> {
                                    OutlinedTextField(
                                        value = chosenChallenge.targetBarcodeLabel ?: "Bathroom Sink Barcode",
                                        onValueChange = { chosenChallenge = chosenChallenge.copy(targetBarcodeLabel = it) },
                                        label = { Text("Anchor Location Label") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PrimaryOrange,
                                            unfocusedBorderColor = Color.DarkGray,
                                            focusedTextColor = White,
                                            unfocusedTextColor = White
                                        )
                                    )
                                }

                                else -> {
                                    Text("Standard challenge parameters active.", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // WAKE-UP OPTIONS
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkCard, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Allow Choosing On Wake-Up", color = White, fontWeight = FontWeight.Bold)
                                    Text("Shows a selector to pick which challenge to solve when alarm rings", color = LightGray, fontSize = 12.sp)
                                }
                                Switch(
                                    checked = allowUserChoiceOnWake,
                                    onCheckedChange = { allowUserChoiceOnWake = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryOrange)
                                )
                            }

                            Divider(color = Color(0xFF333333))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("90-Second Snooze Penalty", color = White, fontWeight = FontWeight.Bold)
                                    Text("Ramps to max volume and triggers heavy vibrations", color = LightGray, fontSize = 12.sp)
                                }
                                Switch(
                                    checked = isPenaltyEnabled,
                                    onCheckedChange = { isPenaltyEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PrimaryOrange)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SafetyRulesManager
import com.example.model.SafetyRules
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SafetyRulesScreen() {
    val context = LocalContext.current
    val rules by SafetyRulesManager.rules.collectAsState()

    // Local state for editable thresholds
    var tempHigh by remember(rules) { mutableStateOf(rules.tempHighCritical.toString()) }
    var tempLow by remember(rules) { mutableStateOf(rules.tempLowWarning.toString()) }
    var presHigh by remember(rules) { mutableStateOf(rules.pressureHighWarning.toString()) }
    var presLow by remember(rules) { mutableStateOf(rules.pressureLowWarning.toString()) }
    var humHigh by remember(rules) { mutableStateOf(rules.humidityHighWarning.toString()) }
    var humLow by remember(rules) { mutableStateOf(rules.humidityLowWarning.toString()) }
    var fallEnabled by remember(rules) { mutableStateOf(rules.fallDetectionEnabled) }
    var immobilityTimeout by remember(rules) { mutableStateOf(rules.immobilityTimeoutSec.toString()) }
    var sosBuzzer by remember(rules) { mutableStateOf(rules.sosBuzzerEnabled) }

    var hasUnsavedChanges by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val primaryColor = if (isDark) CyanNeon else CommandLightPrimary
    val hazardColor = if (isDark) HazardCrimson else CommandLightRed
    val amberColor = if (isDark) SafetyAmber else CommandLightSecondary
    val emeraldColor = if (isDark) EmeraldOnline else CommandLightEmerald

    val lastUpdatedFormatted = remember(rules.lastUpdated) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.getDefault())
        sdf.format(Date(rules.lastUpdated))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Safety Rules Header Banner
        item {
            GlassCard(
                borderColor = primaryColor.copy(alpha = 0.5f),
                backgroundColor = if (isDark) CommandSurfaceDark.copy(alpha = 0.9f) else Color.White,
                contentPadding = PaddingValues(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(primaryColor.copy(alpha = 0.15f))
                                    .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = "Safety Rules",
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "SAFETY RULES & THRESHOLDS",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else TextDark,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "OSHA & Industrial Safety Threshold Configurator",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = emeraldColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "ADMIN LEVEL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = emeraldColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Last synced with telemetry engine: $lastUpdatedFormatted",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Action Buttons (Save & Reset)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val th = tempHigh.toDoubleOrNull() ?: rules.tempHighCritical
                        val tl = tempLow.toDoubleOrNull() ?: rules.tempLowWarning
                        val ph = presHigh.toDoubleOrNull() ?: rules.pressureHighWarning
                        val pl = presLow.toDoubleOrNull() ?: rules.pressureLowWarning
                        val hh = humHigh.toDoubleOrNull() ?: rules.humidityHighWarning
                        val hl = humLow.toDoubleOrNull() ?: rules.humidityLowWarning
                        val itSec = immobilityTimeout.toIntOrNull() ?: rules.immobilityTimeoutSec

                        SafetyRulesManager.updateRules(
                            rules.copy(
                                tempHighCritical = th,
                                tempLowWarning = tl,
                                pressureHighWarning = ph,
                                pressureLowWarning = pl,
                                humidityHighWarning = hh,
                                humidityLowWarning = hl,
                                fallDetectionEnabled = fallEnabled,
                                immobilityTimeoutSec = itSec,
                                sosBuzzerEnabled = sosBuzzer
                            )
                        )
                        hasUnsavedChanges = false
                        Toast.makeText(context, "Safety rules updated and applied to live telemetry!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1.2f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply Rules", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val osha = SafetyRulesManager.resetToOshaDefaults()
                        tempHigh = osha.tempHighCritical.toString()
                        tempLow = osha.tempLowWarning.toString()
                        presHigh = osha.pressureHighWarning.toString()
                        presLow = osha.pressureLowWarning.toString()
                        humHigh = osha.humidityHighWarning.toString()
                        humLow = osha.humidityLowWarning.toString()
                        fallEnabled = osha.fallDetectionEnabled
                        immobilityTimeout = osha.immobilityTimeoutSec.toString()
                        sosBuzzer = osha.sosBuzzerEnabled
                        hasUnsavedChanges = false
                        Toast.makeText(context, "Thresholds reset to standard OSHA limits", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(0.9f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                ) {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("OSHA Default", fontSize = 11.sp)
                }
            }
        }

        // Section 3: Temperature Rules
        item {
            ThresholdCategoryCard(
                title = "TEMPERATURE ALERT THRESHOLDS",
                icon = Icons.Default.Thermostat,
                accentColor = hazardColor,
                description = "Define upper heat exhaustion limits and low ambient temperature warnings."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tempHigh,
                            onValueChange = {
                                tempHigh = it
                                hasUnsavedChanges = true
                            },
                            label = { Text("High Critical Limit", fontSize = 11.sp) },
                            suffix = { Text("°C", color = hazardColor, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = tempLow,
                            onValueChange = {
                                tempLow = it
                                hasUnsavedChanges = true
                            },
                            label = { Text("Low Cold Warning", fontSize = 11.sp) },
                            suffix = { Text("°C", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Text(
                        text = "Standard guideline: Heat alert triggers above $tempHigh°C, cold exposure triggers below $tempLow°C.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 4: Atmospheric Pressure Rules
        item {
            ThresholdCategoryCard(
                title = "PRESSURE (BAROMETRIC) THRESHOLDS",
                icon = Icons.Default.Speed,
                accentColor = amberColor,
                description = "Monitors sudden barometric pressure changes, hyperbaric chambers, or depressurization."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = presHigh,
                            onValueChange = {
                                presHigh = it
                                hasUnsavedChanges = true
                            },
                            label = { Text("Hyperbaric High", fontSize = 11.sp) },
                            suffix = { Text("hPa", color = amberColor, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = presLow,
                            onValueChange = {
                                presLow = it
                                hasUnsavedChanges = true
                            },
                            label = { Text("Depressurization Low", fontSize = 11.sp) },
                            suffix = { Text("hPa", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Text(
                        text = "Nominal atmospheric pressure is ~1013.25 hPa. High alert > $presHigh hPa, Low alert < $presLow hPa.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 5: Humidity Rules
        item {
            ThresholdCategoryCard(
                title = "RELATIVE HUMIDITY THRESHOLDS",
                icon = Icons.Default.WaterDrop,
                accentColor = primaryColor,
                description = "Monitors condensing moisture hazards and dangerous static discharge dryness."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = humHigh,
                            onValueChange = {
                                humHigh = it
                                hasUnsavedChanges = true
                            },
                            label = { Text("Condensing High", fontSize = 11.sp) },
                            suffix = { Text("%", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = humLow,
                            onValueChange = {
                                humLow = it
                                hasUnsavedChanges = true
                            },
                            label = { Text("Static Dryness Low", fontSize = 11.sp) },
                            suffix = { Text("%", color = amberColor, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Section 6: Motion & Fall Alert Rules
        item {
            ThresholdCategoryCard(
                title = "MOTION & IMPACT SAFETY ENFORCEMENT",
                icon = Icons.Default.DirectionsRun,
                accentColor = emeraldColor,
                description = "IMU accelerometer fall detection, zero-motion timeout, and emergency buzzer behavior."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Fall detection toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Automated Fall Detection",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else TextDark
                            )
                            Text(
                                text = "Instantly triggers emergency dispatcher alert upon sudden deceleration/impact",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = fallEnabled,
                            onCheckedChange = {
                                fallEnabled = it
                                hasUnsavedChanges = true
                            }
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Immobility Timeout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Man-Down Immobility Timeout",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else TextDark
                            )
                            Text(
                                text = "Trigger alert if worker remains stationary with zero motion detected",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedTextField(
                            value = immobilityTimeout,
                            onValueChange = {
                                immobilityTimeout = it
                                hasUnsavedChanges = true
                            },
                            suffix = { Text("sec", fontSize = 12.sp) },
                            modifier = Modifier.width(90.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // SOS Vest Hardware Buzzer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hardware Emergency Buzzer & Strobe",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else TextDark
                            )
                            Text(
                                text = "Sound physical vest alarm and beacon LEDs when SOS button is pressed",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = sosBuzzer,
                            onCheckedChange = {
                                sosBuzzer = it
                                hasUnsavedChanges = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThresholdCategoryCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    description: String,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark

    GlassCard(
        borderColor = accentColor.copy(alpha = 0.4f),
        backgroundColor = if (isDark) CommandSurfaceDark.copy(alpha = 0.85f) else Color.White,
        contentPadding = PaddingValues(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(16.dp))
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = description,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            content()
        }
    }
}

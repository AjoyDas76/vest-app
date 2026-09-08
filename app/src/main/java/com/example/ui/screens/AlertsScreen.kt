package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manager.VestTelemetryManager
import com.example.model.AlertSeverity
import com.example.model.HazardAlert
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlertsScreen() {
    val activeAlerts by VestTelemetryManager.activeAlerts.collectAsState()
    val vestData by VestTelemetryManager.vestData.collectAsState()
    val context = LocalContext.current
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }

    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val emeraldColor = if (isDark) EmeraldOnline else CommandLightEmerald
    val hazardColor = if (isDark) HazardCrimson else CommandLightRed
    val amberColor = if (isDark) SafetyAmber else CommandLightSecondary
    val primaryAccent = if (isDark) CyanNeon else CommandLightPrimary

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Overview Status Banner
        item {
            val hasCritical = activeAlerts.any { it.severity == AlertSeverity.CRITICAL }
            val bannerBorder = if (!vestData.isOnline) MaterialTheme.colorScheme.outline else if (activeAlerts.isEmpty()) emeraldColor else if (hasCritical) hazardColor else amberColor
            val bannerBg = if (!vestData.isOnline) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else if (activeAlerts.isEmpty()) emeraldColor.copy(alpha = if (isDark) 0.1f else 0.08f) else if (hasCritical) hazardColor.copy(alpha = if (isDark) 0.12f else 0.08f) else amberColor.copy(alpha = if (isDark) 0.12f else 0.08f)

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = bannerBorder,
                backgroundColor = bannerBg
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(bannerBorder.copy(alpha = if (isDark) 0.2f else 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (!vestData.isOnline) Icons.Default.CloudOff else if (activeAlerts.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = bannerBorder,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (!vestData.isOnline) "DEVICE OFFLINE" else if (activeAlerts.isEmpty()) "ALL SENSORS NORMAL" else "ACTIVE HAZARD ALERTS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = bannerBorder
                            )
                            Text(
                                text = if (!vestData.isOnline) "No live telemetry received in 5s (---)" else if (activeAlerts.isEmpty()) "Safe parameters maintained" else "${activeAlerts.size} threshold breaches detected",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = bannerBorder,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (!vestData.isOnline) "OFFLINE" else "${activeAlerts.size} ALERTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // Active Hazards List
        item {
            Text(
                text = "EVALUATED HAZARD LIST",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        if (activeAlerts.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = MaterialTheme.colorScheme.outline
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = emeraldColor,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No Active Hazard Alarms",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Temperature (15-45°C), Humidity (20-90%), Pressure (950-1050 hPa), IMU & SOS are safe.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        } else {
            items(activeAlerts, key = { it.id }) { alert ->
                HazardAlertDetailCard(alert = alert, timeFormat = timeFormat)
            }
        }

        // Emergency Simulation & Live Testing Controls
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "SAFETY SIMULATION & HARDWARE TESTING",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = MaterialTheme.colorScheme.outline
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Vest Hardware Test Triggers",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Trigger alarms manually to test Android notification dispatch and audio warning response.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Simulate Fall
                        Button(
                            onClick = {
                                VestTelemetryManager.simulateFallAlert()
                                Toast.makeText(context, "Simulated Fall Event Triggered!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = hazardColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AccessibilityNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fall Impact", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Simulate SOS
                        Button(
                            onClick = {
                                VestTelemetryManager.simulateSosAlert()
                                Toast.makeText(context, "Simulated SOS Alarm Triggered!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = amberColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Emergency, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SOS Panic", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Clear Active Hazards
                    OutlinedButton(
                        onClick = {
                            VestTelemetryManager.clearActiveAlerts()
                            Toast.makeText(context, "Hazards cleared. Sensors reset to safe baseline.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = primaryAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Safe Baseline", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun HazardAlertDetailCard(alert: HazardAlert, timeFormat: SimpleDateFormat) {
    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val isCritical = alert.severity == AlertSeverity.CRITICAL
    val accentColor = if (isCritical) {
        if (isDark) HazardCrimson else CommandLightRed
    } else {
        if (isDark) SafetyAmber else CommandLightSecondary
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = accentColor,
        backgroundColor = accentColor.copy(alpha = if (isDark) 0.08f else 0.05f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Text(
                        text = alert.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = accentColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isCritical) "CRITICAL" else "WARNING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = alert.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider(color = accentColor.copy(alpha = 0.3f), thickness = 0.8.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reading: ${alert.triggeredValue} (Safe: ${alert.safeLimit})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
                Text(
                    text = timeFormat.format(Date(alert.timestamp)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

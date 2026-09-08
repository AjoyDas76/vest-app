package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.manager.VestTelemetryManager
import com.example.model.EventSnapshot
import com.example.model.HazardAlert
import com.example.model.VestData
import com.example.model.WorkerProfile
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    profile: WorkerProfile = WorkerProfile()
) {
    val vestData by VestTelemetryManager.vestData.collectAsState()
    val activeAlerts by VestTelemetryManager.activeAlerts.collectAsState()
    val eventHistory by VestTelemetryManager.eventHistory.collectAsState()

    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. WORKER STATUS Card (Cyan to Golden Amber Neon Gradient Border)
        item {
            WorkerStatusCard(
                vestData = vestData,
                profile = profile,
                isDark = isDark
            )
        }

        // 2. 2x2 Telemetry Grid (Temperature, Humidity, Pressure, Active Alerts)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Row 1: Temperature & Humidity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val tempStr = if (vestData.isOnline) "${String.format(Locale.US, "%.1f", vestData.environment.temperature)}°C" else "--"
                    FuturisticMetricCard(
                        title = "TEMPERATURE",
                        value = tempStr,
                        icon = Icons.Default.DeviceThermostat,
                        borderColor = if (isDark) Color(0xFFFF385C) else Color(0xFFDC2626),
                        iconColor = if (isDark) Color(0xFFFF385C) else Color(0xFFDC2626),
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )

                    val humStr = if (vestData.isOnline) "${String.format(Locale.US, "%.1f", vestData.environment.humidity)}%" else "--"
                    FuturisticMetricCard(
                        title = "HUMIDITY",
                        value = humStr,
                        icon = Icons.Default.WaterDrop,
                        borderColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7),
                        iconColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7),
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Pressure & Active Alerts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val presStr = if (vestData.isOnline) "${String.format(Locale.US, "%.1f", vestData.environment.pressure)} hPa" else "--"
                    FuturisticMetricCard(
                        title = "PRESSURE",
                        value = presStr,
                        icon = Icons.Default.AvTimer,
                        borderColor = if (isDark) Color(0xFFA855F7) else Color(0xFF7C3AED),
                        iconColor = if (isDark) Color(0xFFA855F7) else Color(0xFF7C3AED),
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )

                    val alertsCountStr = if (vestData.isOnline) "${activeAlerts.size}" else "--"
                    FuturisticMetricCard(
                        title = "ACTIVE ALERTS",
                        value = alertsCountStr,
                        icon = Icons.Default.Notifications,
                        borderColor = if (isDark) Color(0xFFFF4560) else Color(0xFFDC2626),
                        iconColor = if (isDark) Color(0xFFFF4560) else Color(0xFFDC2626),
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. MOTION STATE Card (Neon Amber Border)
        item {
            FuturisticMotionCard(
                motionState = if (vestData.isOnline) vestData.status.motion_state else "--",
                isDark = isDark
            )
        }

        // 4. ACTIVE ALERTS Container (Bottom Section matching mockup)
        item {
            FuturisticAlertsSection(
                alerts = activeAlerts,
                isDark = isDark
            )
        }

        // 5. Sensor Diagnostics & System Link Card
        item {
            SystemDiagnosticsCard(vestData = vestData, profile = profile, isDark = isDark)
        }

        // 6. Device Event History Buffer (Latest snapshots)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "EVENT HISTORY LOG",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7),
                            letterSpacing = 1.sp
                        )
                        if (!vestData.isOnline) {
                            Surface(
                                color = if (isDark) Color(0x33FF5252) else Color(0x22DC2626),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFFFF5252).copy(alpha = 0.6f) else Color(0xFFDC2626).copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "OFFLINE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFFF5252) else Color(0xFFDC2626),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (!vestData.isOnline) "Device offline - live stream paused" else "Latest snapshots (device local buffer)",
                        fontSize = 11.sp,
                        color = if (!vestData.isOnline) {
                            if (isDark) Color(0xFFFF5252).copy(alpha = 0.85f) else Color(0xFFDC2626).copy(alpha = 0.85f)
                        } else {
                            if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        }
                    )
                }

                Surface(
                    color = if (!vestData.isOnline) {
                        if (isDark) Color(0x22FF5252) else Color(0x1ADC2626)
                    } else {
                        if (isDark) Color(0x3300E5FF) else Color(0x1F0284C7)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (!vestData.isOnline) {
                            if (isDark) Color(0xFFFF5252).copy(alpha = 0.4f) else Color(0xFFDC2626).copy(alpha = 0.4f)
                        } else {
                            if (isDark) Color(0xFF00E5FF).copy(alpha = 0.4f) else Color(0xFF0284C7).copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Text(
                        text = if (!vestData.isOnline && eventHistory.isEmpty()) "OFFLINE" else "${eventHistory.size}/10",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!vestData.isOnline) {
                            if (isDark) Color(0xFFFF5252) else Color(0xFFDC2626)
                        } else {
                            if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (!vestData.isOnline) {
            item {
                Surface(
                    color = if (isDark) Color(0x2BFF5252) else Color(0x1ADC2626),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFFFF5252).copy(alpha = 0.5f) else Color(0xFFDC2626).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFFFF5252) else Color(0xFFDC2626))
                        )
                        Text(
                            text = "STATUS: OFFLINE — Waiting for device telemetry...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFF5252) else Color(0xFFDC2626)
                        )
                    }
                }
            }
        }

        if (eventHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0x22051224) else Color(0xCCFFFFFF))
                        .border(
                            1.dp,
                            if (!vestData.isOnline) {
                                if (isDark) Color(0xFFFF5252).copy(alpha = 0.35f) else Color(0xFFDC2626).copy(alpha = 0.35f)
                            } else {
                                if (isDark) Color(0xFF00E5FF).copy(alpha = 0.25f) else Color(0xFF0284C7).copy(alpha = 0.3f)
                            },
                            RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (!vestData.isOnline) Icons.Default.CloudOff else Icons.Default.Sensors,
                            contentDescription = null,
                            tint = if (!vestData.isOnline) {
                                if (isDark) Color(0xFFFF5252).copy(alpha = 0.8f) else Color(0xFFDC2626).copy(alpha = 0.8f)
                            } else {
                                if (isDark) Color(0xFF00E5FF).copy(alpha = 0.6f) else Color(0xFF0284C7).copy(alpha = 0.8f)
                            },
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = if (!vestData.isOnline) "Device Offline (---)" else "Awaiting Telemetry Stream...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (!vestData.isOnline) {
                                if (isDark) Color(0xFFFF5252) else Color(0xFFDC2626)
                            } else {
                                if (isDark) Color.White else Color(0xFF0F172A)
                            }
                        )
                        Text(
                            text = if (!vestData.isOnline) "No live telemetry packets received. System is currently offline." else "Packets arriving via LoRa Gateway node 'worker1' will be logged here",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }
            }
        } else {
            items(eventHistory, key = { it.id }) { snapshot ->
                EventSnapshotRow(snapshot = snapshot, isDark = isDark)
            }
        }
    }
}

// ---------------------------------------------------------
// WORKER STATUS Card (Matching exact user mockup design)
// ---------------------------------------------------------
@Composable
fun WorkerStatusCard(
    vestData: VestData,
    profile: WorkerProfile,
    isDark: Boolean = true
) {
    // Frosted Glass multi-stop gradient with high luminosity top highlight and deep dark smoked body
    val glassBgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x35FFFFFF),  // Frosty crystal rim highlight
                Color(0xEE0B1524),  // Deep smoked dark-slate glass for sharp text
                Color(0xF5060D17)   // Deep solid-translucent base
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFAFFFFFF),
                Color(0xF0F1F5F9),
                Color(0xE8E2E8F0)
            )
        )
    }

    val headerTitleColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)

    val borderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF00E5FF),
                Color(0xFFFFA000),
                Color(0xFFFF9100)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF0284C7),
                Color(0xFFEA580C),
                Color(0xFFD97706)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(glassBgGradient)
            .border(1.8.dp, borderBrush, RoundedCornerShape(26.dp))
    ) {
        // Inner Glass Rim Highlight (1px inside bright bevel)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isDark) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.75f),
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Diagonal Glass Refraction Sheen across card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.12f else 0.30f),
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.04f else 0.15f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(700f, 700f)
                    )
                )
        )

        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Text(
                text = "WORKER STATUS",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = headerTitleColor,
                letterSpacing = 1.4.sp
            )

            WorkerStatusRow(
                label = "ID",
                value = profile.workerId.ifBlank { "VST-001" },
                isDark = isDark
            )
            WorkerStatusRow(
                label = "NAME",
                value = profile.workerName.ifBlank { "Rahim Uddin" },
                isDark = isDark
            )
            WorkerStatusRow(
                label = "VEST ID",
                value = if (profile.batchId.isNotBlank()) profile.batchId else "SV-2026-014",
                isDark = isDark
            )
            WorkerStatusRow(
                label = "STATUS",
                value = if (vestData.isOnline) "Active" else "Inactive",
                valueColor = if (vestData.isOnline) {
                    if (isDark) Color(0xFF00E676) else Color(0xFF059669)
                } else {
                    if (isDark) Color(0xFFFF5252) else Color(0xFFDC2626)
                },
                isDark = isDark
            )
            WorkerStatusRow(
                label = "SHIFT",
                value = profile.shift.ifBlank { "Day (08:00 - 20:00)" },
                isDark = isDark
            )
        }
    }
}

@Composable
private fun WorkerStatusRow(
    label: String,
    value: String,
    valueColor: Color? = null,
    isDark: Boolean = true
) {
    val defaultValColor = if (isDark) Color.White else Color(0xFF0F172A)
    val actualValColor = valueColor ?: defaultValColor
    val labelColor = if (isDark) Color(0xFF8FA0B5) else Color(0xFF64748B)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
            modifier = Modifier.weight(0.38f)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = actualValColor,
            modifier = Modifier.weight(0.62f)
        )
    }
}

// ---------------------------------------------------------
// Futuristic Metric Card (Circular Icon + Title + Big Value)
// ---------------------------------------------------------
@Composable
fun FuturisticMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    borderColor: Color,
    iconColor: Color = borderColor,
    watermark: String? = null,
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Distinctive Frosted Crystal Glass gradient with deep smoked base for crisp text
    val glassBgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x35FFFFFF),  // Frosty crystal top highlight
                Color(0xEE0B1524),  // Deep smoked dark-slate glass
                Color(0xF5060D17)   // Deep solid-translucent base
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFAFFFFFF),
                Color(0xF0F1F5F9),
                Color(0xE8E2E8F0)
            )
        )
    }

    val iconBg = if (isDark) Color(0x33000000) else Color(0x14000000)
    val titleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val valueColor = if (isDark) Color.White else Color(0xFF0F172A)
    val watermarkColor = if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 150.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(glassBgGradient)
            .border(1.8.dp, borderColor, RoundedCornerShape(26.dp))
    ) {
        // Specular glass shine / inner highlight on top edge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isDark) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.70f),
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Diagonal light refraction sheen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.10f else 0.25f),
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.04f else 0.12f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(450f, 450f)
                    )
                )
        )

        // Watermark if present
        if (watermark != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = watermark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = watermarkColor,
                    lineHeight = 13.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(watermarkColor)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circular Icon with neon/accent border
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg)
                    .border(1.4.dp, iconColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                letterSpacing = 1.sp
            )

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

// ---------------------------------------------------------
// MOTION STATE Card (Full Width with Amber Neon Border)
// ---------------------------------------------------------
@Composable
fun FuturisticMotionCard(
    motionState: String,
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    val displayState = if (motionState.isBlank() || motionState == "--" || motionState.equals("offline", ignoreCase = true)) "--" else motionState.uppercase()
    val glassBgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x35FFFFFF),  // Frosty crystal top highlight
                Color(0xEE0B1524),  // Deep smoked dark-slate glass
                Color(0xF5060D17)   // Deep solid-translucent base
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFAFFFFFF),
                Color(0xF0F1F5F9),
                Color(0xE8E2E8F0)
            )
        )
    }

    val iconBg = if (isDark) Color(0x33000000) else Color(0x14000000)
    val titleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val valueColor = if (isDark) Color.White else Color(0xFF0F172A)
    val amberBorder = if (isDark) Color(0xFFFFA000) else Color(0xFFEA580C)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 132.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(glassBgGradient)
            .border(1.8.dp, amberBorder, RoundedCornerShape(26.dp))
    ) {
        // Specular glass shine / inner highlight on top edge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isDark) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.70f),
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Diagonal light refraction sheen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.10f else 0.25f),
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.04f else 0.12f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(500f, 500f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg)
                    .border(1.4.dp, amberBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = "Motion State",
                    tint = amberBorder,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = "MOTION STATE",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                letterSpacing = 1.sp
            )

            Text(
                text = displayState,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

// ---------------------------------------------------------
// ACTIVE ALERTS Container (Matching Bottom of Screenshot)
// ---------------------------------------------------------
@Composable
fun FuturisticAlertsSection(
    alerts: List<HazardAlert>,
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    val glassBgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x35FFFFFF),  // Frosty crystal top highlight
                Color(0xEE0B1524),  // Deep smoked dark-slate glass
                Color(0xF5060D17)   // Deep solid-translucent base
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFAFFFFFF),
                Color(0xF0F1F5F9),
                Color(0xE8E2E8F0)
            )
        )
    }
    val cardBorder = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.55f) else Color(0xFF0284C7).copy(alpha = 0.6f)
    val titleColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)
    val noAlertsColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(glassBgGradient)
            .border(1.6.dp, cardBorder, RoundedCornerShape(26.dp))
    ) {
        // Specular glass shine on top edge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isDark) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.70f),
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Diagonal light refraction sheen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.10f else 0.25f),
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.04f else 0.12f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(500f, 500f)
                    )
                )
        )

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ACTIVE ALERTS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF334B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${alerts.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (alerts.isEmpty()) {
                Text(
                    text = "No active hazardous alerts",
                    fontSize = 12.sp,
                    color = noAlertsColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                alerts.forEach { alert ->
                    val rowBg = if (isDark) Color.Black.copy(alpha = 0.35f) else Color(0xFFFFF1F2)
                    val alertTitleColor = if (isDark) Color.White else Color(0xFF991B1B)
                    val alertDescColor = if (isDark) Color(0xFF94A3B8) else Color(0xFFB91C1C)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(rowBg)
                            .border(1.dp, Color(0xFFFF334B).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = alert.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = alertTitleColor
                            )
                            Text(
                                text = alert.description,
                                fontSize = 11.sp,
                                color = alertDescColor
                            )
                        }

                        Surface(
                            color = Color(0xFFFF334B),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = alert.triggeredValue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// LoRa & System Diagnostics Card (No Battery Monitoring)
// ---------------------------------------------------------
@Composable
fun SystemDiagnosticsCard(
    vestData: VestData,
    profile: WorkerProfile,
    isDark: Boolean = true
) {
    val isFall = vestData.alerts.fall_detected
    val isSos = vestData.alerts.sos_active
    val glassBgGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x35FFFFFF),  // Frosty crystal top highlight
                Color(0xEE0B1524),  // Deep smoked dark-slate glass
                Color(0xF5060D17)   // Deep solid-translucent base
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFAFFFFFF),
                Color(0xF0F1F5F9),
                Color(0xE8E2E8F0)
            )
        )
    }
    val cardBorder = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.45f) else Color(0xFF0284C7).copy(alpha = 0.5f)
    val titleColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)
    val subtextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val workerNameColor = if (isDark) Color.White else Color(0xFF0F172A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(glassBgGradient)
            .border(1.4.dp, cardBorder, RoundedCornerShape(26.dp))
    ) {
        // Specular glass shine on top edge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isDark) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.70f),
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Diagonal light refraction sheen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.10f else 0.25f),
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.04f else 0.12f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(500f, 500f)
                    )
                )
        )

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "VEST DIAGNOSTICS & SYSTEM LINK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                letterSpacing = 0.8.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LoRa Link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val linkColor = if (vestData.isOnline) {
                        if (isDark) Color(0xFF00E676) else Color(0xFF059669)
                    } else {
                        if (isDark) Color(0xFFFF5252) else Color(0xFFDC2626)
                    }

                    Icon(
                        imageVector = Icons.Default.CellTower,
                        contentDescription = "LoRa Node",
                        tint = linkColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = if (vestData.isOnline) "LoRa Link OK" else "Lost Link",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = linkColor
                        )
                        Text(
                            text = "Gateway Node: ${profile.loraNode}",
                            fontSize = 10.sp,
                            color = subtextColor
                        )
                    }
                }

                // Monitored Worker Identity
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = profile.workerName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = workerNameColor
                        )
                        Text(
                            text = "Worker ID: ${profile.workerId}",
                            fontSize = 10.sp,
                            color = subtextColor
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = "Worker",
                        tint = titleColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Fall and SOS status pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val fallBg = if (!vestData.isOnline) {
                    if (isDark) Color(0x1A94A3B8) else Color(0x1A64748B)
                } else if (isFall) {
                    Color(0x33FF334B)
                } else {
                    if (isDark) Color(0x1F00E676) else Color(0x1F059669)
                }
                val fallColor = if (!vestData.isOnline) {
                    if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                } else if (isFall) {
                    Color(0xFFFF334B)
                } else {
                    if (isDark) Color(0xFF00E676) else Color(0xFF059669)
                }

                Surface(
                    color = fallBg,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, fallColor.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessibilityNew,
                            contentDescription = "Fall",
                            tint = fallColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (!vestData.isOnline) "FALL: ---" else if (isFall) "FALL DETECTED" else "FALL: NORMAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = fallColor
                        )
                    }
                }

                val sosBg = if (!vestData.isOnline) {
                    if (isDark) Color(0x1A94A3B8) else Color(0x1A64748B)
                } else if (isSos) {
                    Color(0x33FF334B)
                } else {
                    if (isDark) Color(0x1F00E676) else Color(0x1F059669)
                }
                val sosColor = if (!vestData.isOnline) {
                    if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                } else if (isSos) {
                    Color(0xFFFF334B)
                } else {
                    if (isDark) Color(0xFF00E676) else Color(0xFF059669)
                }

                Surface(
                    color = sosBg,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, sosColor.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Emergency,
                            contentDescription = "SOS",
                            tint = sosColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (!vestData.isOnline) "SOS: ---" else if (isSos) "SOS ACTIVE" else "SOS: IDLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = sosColor
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// Event Snapshot Row
// ---------------------------------------------------------
@Composable
fun EventSnapshotRow(
    snapshot: EventSnapshot,
    isDark: Boolean = true
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    val isOffline = !snapshot.isOnline || snapshot.motion_state.equals("OFFLINE", ignoreCase = true)
    val hasAlert = snapshot.fall_detected || snapshot.sos_active || snapshot.activeAlerts.isNotEmpty()

    val borderColor = if (isOffline) Color(0xFFFF5252).copy(alpha = 0.7f)
        else if (hasAlert) Color(0xFFFF334B).copy(alpha = 0.7f)
        else if (isDark) Color(0xFF00E5FF).copy(alpha = 0.3f)
        else Color(0xFF0284C7).copy(alpha = 0.35f)

    val bgColor = if (isOffline) Color(0x2BFF5252)
        else if (hasAlert) Color(0x33FF334B)
        else if (isDark) Color(0x22051224)
        else Color(0xCCFFFFFF)

    val timeColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val normalTagColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)
    val normalTagBg = if (isDark) Color(0x3300E5FF) else Color(0x1F0284C7)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOffline) Color(0xFFFF5252)
                                else if (hasAlert) Color(0xFFFF334B)
                                else normalTagColor
                            )
                    )
                    Text(
                        text = timeFormat.format(Date(snapshot.timestamp)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = timeColor
                    )
                }

                Surface(
                    color = if (isOffline) Color(0xFFFF5252).copy(alpha = 0.25f)
                        else if (hasAlert) Color(0xFFFF334B).copy(alpha = 0.25f)
                        else normalTagBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isOffline) "OFFLINE" else snapshot.motion_state.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOffline) Color(0xFFFF5252)
                            else if (hasAlert) Color(0xFFFF334B)
                            else normalTagColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (isOffline) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Temp: ---",
                        fontSize = 11.sp,
                        color = subtextColor
                    )
                    Text(
                        text = "Hum: ---",
                        fontSize = 11.sp,
                        color = subtextColor
                    )
                    Text(
                        text = "Pres: ---",
                        fontSize = 11.sp,
                        color = subtextColor
                    )
                }

                Text(
                    text = "📡 Device Offline (Telemetry connection paused)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF5252)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Temp: ${String.format(Locale.US, "%.1f°C", snapshot.temperature)}",
                        fontSize = 11.sp,
                        color = subtextColor
                    )
                    Text(
                        text = "Hum: ${String.format(Locale.US, "%.1f%%", snapshot.humidity)}",
                        fontSize = 11.sp,
                        color = subtextColor
                    )
                    Text(
                        text = "Pres: ${String.format(Locale.US, "%.0f hPa", snapshot.pressure)}",
                        fontSize = 11.sp,
                        color = subtextColor
                    )
                }

                if (hasAlert && snapshot.activeAlerts.isNotEmpty()) {
                    Text(
                        text = "⚠️ Alert: ${snapshot.activeAlerts.joinToString(", ")}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF334B)
                    )
                }
            }
        }
    }
}

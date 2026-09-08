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
import com.example.data.PrefsManager
import com.example.model.WorkerProfile
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(
    profile: WorkerProfile = WorkerProfile(),
    onThemeToggle: (isDark: Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager.getInstance(context) }

    var isDarkTheme by remember { mutableStateOf(prefs.isDarkMode) }
    var isAudioAlerts by remember { mutableStateOf(prefs.isAudioAlertsEnabled) }
    var isNotifications by remember { mutableStateOf(prefs.isNotificationsEnabled) }

    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val primaryAccent = if (isDark) CyanNeon else CommandLightPrimary
    val amberAccent = if (isDark) SafetyAmber else CommandLightSecondary
    val emeraldAccent = if (isDark) EmeraldOnline else CommandLightEmerald
    val hazardAccent = if (isDark) HazardCrimson else CommandLightRed

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section 1: System Preferences (3 Toggles)
        item {
            Text(
                text = "CONSOLE & ALERT PREFERENCES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        // Toggle 1: Theme (Dark / Light)
        item {
            SettingToggleCard(
                title = "Console Theme",
                subtitle = if (isDarkTheme) "Dark Industrial Mode (High-contrast command)" else "Light Inspection Mode",
                icon = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                iconColor = primaryAccent,
                isChecked = isDarkTheme,
                onCheckedChange = { checked ->
                    isDarkTheme = checked
                    prefs.isDarkMode = checked
                    onThemeToggle(checked)
                }
            )
        }

        // Toggle 2: Audio Alert
        item {
            SettingToggleCard(
                title = "Audio Siren & Haptics",
                subtitle = "Emit high-priority acoustic tone and vibration upon fall or SOS detection",
                icon = if (isAudioAlerts) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                iconColor = amberAccent,
                isChecked = isAudioAlerts,
                onCheckedChange = { checked ->
                    isAudioAlerts = checked
                    prefs.isAudioAlertsEnabled = checked
                    Toast.makeText(
                        context,
                        if (checked) "Hazard siren and haptics enabled" else "Audio alerts muted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        // Toggle 3: Notification
        item {
            SettingToggleCard(
                title = "System Push Notifications",
                subtitle = "Deliver Android system notifications for critical sensor limit violations",
                icon = if (isNotifications) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                iconColor = emeraldAccent,
                isChecked = isNotifications,
                onCheckedChange = { checked ->
                    isNotifications = checked
                    prefs.isNotificationsEnabled = checked
                    Toast.makeText(
                        context,
                        if (checked) "System notifications enabled" else "Notifications disabled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        // Section 2: Hardware & Network Diagnostics
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "HARDWARE & LORA GATEWAY DIAGNOSTICS",
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
                    DiagnosticRow(label = "Hardware Platform", value = "ESP32 + IMU + LoRa (SX1276)")
                    DiagnosticRow(label = "Frequency Band", value = profile.loraFrequency)
                    DiagnosticRow(label = "Monitored Node", value = profile.loraNode)
                    DiagnosticRow(label = "Watchdog Timeout", value = "6.0 Seconds (Offline watchdog)")
                    DiagnosticRow(label = "RTDB Link", value = "worker-safety-vest-92b97")
                    DiagnosticRow(label = "Operator Account", value = prefs.userEmail.ifEmpty { "operator@vestcommand.internal" })
                }
            }
        }

        // Section 3: Logout Action
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    try {
                        FirebaseAuth.getInstance().signOut()
                    } catch (_: Exception) {}
                    prefs.clearSession()
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = hazardAccent.copy(alpha = if (isDark) 0.2f else 0.12f),
                    contentColor = hazardAccent
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, hazardAccent)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Log Out", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LOGOUT OPERATOR CONSOLE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
fun SettingToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = MaterialTheme.colorScheme.outline
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = if (isDark) 0.15f else 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.padding(end = 8.dp)) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = if (isDark) CyanNeon else MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

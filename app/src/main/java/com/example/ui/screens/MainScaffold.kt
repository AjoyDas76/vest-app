package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.WorkerProfileRepository
import com.example.manager.VestTelemetryManager
import com.example.model.WorkerProfile
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class NavDestination(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    LIVE_CHART("Live Chart", Icons.Default.ShowChart),
    MAP_TRACKING("Map Tracking", Icons.Default.Map),
    WORKER_PROFILES("Worker Profiles", Icons.Default.Badge),
    SAFETY_RULES("Safety Rules", Icons.Default.Gavel),
    ALERTS("Alerts", Icons.Default.Warning),
    REPORT("Report", Icons.Default.Assessment),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    profile: WorkerProfile = WorkerProfile(),
    onThemeToggle: (isDark: Boolean) -> Unit,
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(NavDestination.DASHBOARD) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val vestData by VestTelemetryManager.vestData.collectAsState()
    val activeAlerts by VestTelemetryManager.activeAlerts.collectAsState()
    val dynamicWorker by WorkerProfileRepository.activeWorker.collectAsState()
    val currentProfile = dynamicWorker

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .width(310.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Drawer Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_logo),
                                    contentDescription = "Logo",
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                )
                            }

                            Column {
                                Text(
                                    text = "VEST COMMAND",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "LoRa Safety Hub • ESP32",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Monitored Worker Profile Badge in Drawer
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (vestData.isOnline) {
                                                if (isDark) EmeraldOnline else CommandLightEmerald
                                            } else {
                                                if (isDark) OfflineRed else CommandLightRed
                                            }
                                        )
                                )
                                Column {
                                    Text(
                                        text = "${currentProfile.workerName} (${currentProfile.workerId})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Vest: ${currentProfile.assignedVestId} • Node: ${currentProfile.loraNode} • ${if (vestData.isOnline) "Active" else "Offline"}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        // Navigation Items List
                        NavDestination.values().forEach { destination ->
                            val isSelected = currentScreen == destination
                            val alertCount = if (destination == NavDestination.ALERTS) activeAlerts.size else 0

                            val highlightBrush = if (isDark) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        CyanNeon.copy(alpha = 0.25f),
                                        TealAccent.copy(alpha = 0.15f)
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFE0F2FE),
                                        Color(0xFFF0FDF4)
                                    )
                                )
                            }
                            val highlightBorder = if (isDark) CyanNeon.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)

                            val itemModifier = if (isSelected) {
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(highlightBrush)
                                    .border(1.dp, highlightBorder, RoundedCornerShape(12.dp))
                            } else {
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                            }

                            Row(
                                modifier = itemModifier
                                    .clickable {
                                        currentScreen = destination
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    val iconTint = if (isSelected) {
                                        if (isDark) CyanNeon else MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    val textColor = if (isSelected) {
                                        if (isDark) Color.White else MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }

                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.title,
                                        tint = iconTint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = destination.title,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = textColor
                                    )
                                }

                                if (alertCount > 0) {
                                    Surface(
                                        color = if (isDark) HazardCrimson else CommandLightRed,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "$alertCount",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Drawer Footer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Divider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "Smart Industrial Safety Vest v1.8",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ESP32 LoRa Transceiver • RTDB Link",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    ) {
        val isDark = MaterialTheme.colorScheme.background == CommandNavyDark

        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image of Smart Safety Vest and Helmet clearly visible
            Image(
                painter = painterResource(id = R.drawable.img_app_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = if (isDark) 0.72f else 0.75f
            )

            // Balanced industrial gradient overlay ensuring background image details are rich and visible
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color(0x8A050B14),
                                    Color(0x60081220),
                                    Color(0x9E03060B)
                                )
                            } else {
                                // Crisp, translucent frosted industrial light theme overlay
                                listOf(
                                    Color(0xA6F1F5F9),
                                    Color(0x80E2E8F0),
                                    Color(0xB3F8FAFC)
                                )
                            }
                        )
                    )
            )

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    // Floating Pill Top Bar ("VEST COMMAND") matching design reference
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        val topBarBg = if (isDark) Color(0xCC061224) else Color(0xCCEBF4FC)
                        val topBarBorder = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.55f) else Color(0xFF0284C7).copy(alpha = 0.8f)
                        val topBarTitleColor = if (isDark) Color.White else Color(0xFF0F172A)
                        val topBarIconTint = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            color = topBarBg,
                            border = androidx.compose.foundation.BorderStroke(1.4.dp, topBarBorder),
                            shadowElevation = if (isDark) 0.dp else 4.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Open Drawer Menu",
                                        tint = topBarIconTint
                                    )
                                }

                                Text(
                                    text = "VEST COMMAND",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.6.sp,
                                    color = topBarTitleColor
                                )

                                // Online status pill with wifi icon
                                OnlineStatusPill(
                                    isOnline = vestData.isOnline
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Ambient HUD decorations only on Dashboard for background sci-fi vibe without overlaying forms/lists
                    if (currentScreen == NavDestination.DASHBOARD) {
                        // Ambient HUD category icons on the left edge (faint)
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HudCategoryBadge(icon = Icons.Default.Shield, label = "SAFETY", alpha = 0.35f)
                            HudCategoryBadge(icon = Icons.Default.Favorite, label = "HEALTH", alpha = 0.35f)
                            HudCategoryBadge(icon = Icons.Default.LocationOn, label = "TRACKING", alpha = 0.35f)
                            HudCategoryBadge(icon = Icons.Default.Notifications, label = "ALERTS", alpha = 0.35f)
                            HudCategoryBadge(icon = Icons.Default.Sensors, label = "CONNECTIVITY", alpha = 0.35f)
                        }

                        // Top-right dot matrix
                        DotMatrixHud(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 16.dp)
                        )

                        // Bottom-left radar
                        RadarReticleHud(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 12.dp, bottom = 24.dp)
                                .size(60.dp)
                        )

                        // Bottom-right telemetry lines
                        TelemetryLinesHud(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 12.dp, bottom = 12.dp)
                                .size(width = 80.dp, height = 24.dp)
                        )
                    }

                    // Foreground Screens
                    when (currentScreen) {
                        NavDestination.DASHBOARD -> DashboardScreen(profile = currentProfile)
                        NavDestination.LIVE_CHART -> LiveChartScreen()
                        NavDestination.MAP_TRACKING -> MapTrackingScreen(profile = currentProfile)
                        NavDestination.WORKER_PROFILES -> WorkerProfilesScreen(
                            onWorkerSelected = {
                                // Worker switched
                            }
                        )
                        NavDestination.SAFETY_RULES -> SafetyRulesScreen()
                        NavDestination.ALERTS -> AlertsScreen()
                        NavDestination.REPORT -> ReportScreen(profile = currentProfile)
                        NavDestination.SETTINGS -> SettingsScreen(
                            profile = currentProfile,
                            onThemeToggle = onThemeToggle,
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}

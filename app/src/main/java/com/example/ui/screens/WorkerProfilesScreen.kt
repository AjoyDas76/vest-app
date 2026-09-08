package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.data.WorkerProfileRepository
import com.example.manager.VestTelemetryManager
import com.example.model.WorkerProfile
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfilesScreen(
    onWorkerSelected: (WorkerProfile) -> Unit = {}
) {
    val context = LocalContext.current
    val workers by WorkerProfileRepository.workers.collectAsState()
    val activeWorker by WorkerProfileRepository.activeWorker.collectAsState()
    val vestData by VestTelemetryManager.vestData.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedWorkerForDetail by remember { mutableStateOf<WorkerProfile?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var workerToEdit by remember { mutableStateOf<WorkerProfile?>(null) }

    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val primaryColor = if (isDark) CyanNeon else CommandLightPrimary
    val emeraldColor = if (isDark) EmeraldOnline else CommandLightEmerald
    val hazardColor = if (isDark) HazardCrimson else CommandLightRed
    val amberColor = if (isDark) SafetyAmber else CommandLightSecondary

    val filteredWorkers = remember(workers, searchQuery) {
        if (searchQuery.isBlank()) workers
        else workers.filter {
            it.workerName.contains(searchQuery, ignoreCase = true) ||
                    it.workerId.contains(searchQuery, ignoreCase = true) ||
                    it.assignedVestId.contains(searchQuery, ignoreCase = true) ||
                    it.department.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Header Banner & Quick Incident Call
        item {
            GlassCard(
                borderColor = primaryColor.copy(alpha = 0.5f),
                backgroundColor = if (isDark) CommandSurfaceDark.copy(alpha = 0.9f) else Color.White,
                contentPadding = PaddingValues(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(primaryColor.copy(alpha = 0.15f))
                                    .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = "Worker Profiles",
                                    tint = primaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "WORKER PROFILES & DISPATCH",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else TextDark,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "${workers.size} Vests Active • Quick Emergency Contacts",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Add Worker Profile Button
                        IconButton(
                            onClick = {
                                workerToEdit = WorkerProfile(
                                    workerId = "WKR-0${workers.size + 1}",
                                    workerName = "",
                                    phone = "",
                                    email = "",
                                    bloodGroup = "O+",
                                    assignedVestId = "VEST-LoRa-0${workers.size + 1}",
                                    loraNode = "worker${workers.size + 1}",
                                    emergencyContactName = "",
                                    emergencyContactPhone = "",
                                    emergencyRelation = "Emergency Contact",
                                    secondaryContactName = "",
                                    secondaryContactPhone = "",
                                    medicalNotes = "",
                                    department = "Field Operations",
                                    shift = "Day Shift"
                                )
                                showEditDialog = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(primaryColor.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Worker",
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Search worker name, ID, or vest ID...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = if (isDark) CommandSurfaceContainerDark else CommandLightSurfaceVariant,
                            unfocusedContainerColor = if (isDark) CommandSurfaceContainerDark else CommandLightSurfaceVariant
                        )
                    )
                }
            }
        }

        // 2. Incident Quick Dispatch Bar (Currently active monitored worker)
        item {
            GlassCard(
                borderColor = if (activeWorker.isActive && vestData.isOnline) emeraldColor.copy(alpha = 0.85f) else primaryColor.copy(alpha = 0.7f),
                backgroundColor = if (isDark) CommandSurfaceDark.copy(alpha = 0.98f) else Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header: Status indicator + Hardware ID badge + Quick Switch
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
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(if (vestData.isOnline) emeraldColor else hazardColor)
                            )
                            Text(
                                text = if (vestData.isOnline) "LIVE TELEMETRY ACTIVE" else "TELEMETRY STANDBY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (vestData.isOnline) emeraldColor else hazardColor,
                                letterSpacing = 0.6.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = primaryColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Sensors, contentDescription = null, tint = primaryColor, modifier = Modifier.size(13.dp))
                                Text(
                                    text = activeWorker.assignedVestId,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }
                        }
                    }

                    // Worker Info Banner with direct calling actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Worker initial avatar
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.18f))
                                    .border(1.5.dp, primaryColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeWorker.workerName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = primaryColor
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = activeWorker.workerName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else TextDark
                                )
                                Text(
                                    text = "${activeWorker.workerId} • ${activeWorker.department}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Direct Action Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (activeWorker.phone.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${activeWorker.phone}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(primaryColor.copy(alpha = 0.16f))
                                        .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call Worker",
                                        tint = primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (activeWorker.emergencyContactPhone.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${activeWorker.emergencyContactPhone}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(hazardColor.copy(alpha = 0.18f))
                                        .border(1.dp, hazardColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Emergency,
                                        contentDescription = "Call Emergency Contact",
                                        tint = hazardColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Live Telemetry Metric Strip (Temp, Heart Rate, Motion, Battery)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) CommandSurfaceContainerDark else CommandLightSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("BODY TEMP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (vestData.isOnline) String.format(Locale.US, "%.1f°C", vestData.environment.temperature) else "--",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (vestData.environment.temperature > 38.0) hazardColor else primaryColor
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) CommandSurfaceContainerDark else CommandLightSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("MOTION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (vestData.isOnline) vestData.status.motion_state else "STANDBY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (vestData.alerts.fall_detected) hazardColor else emeraldColor
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) CommandSurfaceContainerDark else CommandLightSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("BLOOD / SHIFT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${activeWorker.bloodGroup} • ${activeWorker.shift.take(3)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) CommandSurfaceContainerDark else CommandLightSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("BATTERY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (vestData.isOnline) "${vestData.battery.percentage}%" else "--",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (vestData.battery.percentage < 20) hazardColor else emeraldColor
                                )
                            }
                        }
                    }

                    // Compact Emergency Contact Strip with 2-Column Info
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) CommandSurfaceContainerDark else CommandLightSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, hazardColor.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(hazardColor.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MedicalServices,
                                        contentDescription = null,
                                        tint = hazardColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        text = "EMERGENCY CONTACT (${activeWorker.emergencyRelation.uppercase()})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = hazardColor,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = activeWorker.emergencyContactName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color.White else TextDark
                                    )
                                }
                            }

                            // Emergency contact phone pill
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = primaryColor.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, primaryColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = activeWorker.emergencyContactPhone,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "ASSIGNED VEST PERSONNEL (${filteredWorkers.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        // Worker Profiles List
        items(filteredWorkers, key = { it.workerId }) { worker ->
            val isCurrentMonitored = worker.workerId == activeWorker.workerId
            WorkerCard(
                worker = worker,
                isCurrentMonitored = isCurrentMonitored,
                onSelectAsActive = {
                    WorkerProfileRepository.setActiveWorker(worker.workerId)
                    onWorkerSelected(worker)
                    Toast.makeText(context, "Active vest switched to ${worker.assignedVestId} (${worker.workerName})", Toast.LENGTH_SHORT).show()
                },
                onViewDetails = { selectedWorkerForDetail = worker },
                onEdit = {
                    workerToEdit = worker
                    showEditDialog = true
                },
                onDelete = {
                    if (workers.size > 1) {
                        WorkerProfileRepository.deleteWorker(worker.workerId)
                        Toast.makeText(context, "Worker profile removed", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "At least one worker profile must exist", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    // Detail Modal Dialog
    selectedWorkerForDetail?.let { worker ->
        AlertDialog(
            onDismissRequest = { selectedWorkerForDetail = null },
            confirmButton = {
                TextButton(onClick = { selectedWorkerForDetail = null }) {
                    Text("Close", color = primaryColor, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContactPage, contentDescription = null, tint = primaryColor)
                    Text(worker.workerName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DetailRow(label = "Vest Hardware ID", value = worker.assignedVestId, highlight = true)
                    DetailRow(label = "Worker ID", value = worker.workerId)
                    DetailRow(label = "Department", value = worker.department)
                    DetailRow(label = "Assigned Shift", value = worker.shift)
                    DetailRow(label = "Blood Group", value = worker.bloodGroup)
                    DetailRow(label = "Primary Phone", value = worker.phone)
                    DetailRow(label = "Email Address", value = worker.email)
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    DetailRow(label = "Emergency Contact", value = "${worker.emergencyContactName} (${worker.emergencyRelation})")
                    DetailRow(label = "Emergency Phone", value = worker.emergencyContactPhone, highlight = true)
                    DetailRow(label = "Secondary Contact", value = "${worker.secondaryContactName} • ${worker.secondaryContactPhone}")
                    if (worker.medicalNotes.isNotBlank()) {
                        DetailRow(label = "Medical / Hazard Notes", value = worker.medicalNotes)
                    }
                }
            },
            containerColor = if (isDark) CommandSurfaceDark else Color.White
        )
    }

    // Edit/Add Dialog
    if (showEditDialog && workerToEdit != null) {
        WorkerEditDialog(
            initialWorker = workerToEdit!!,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                WorkerProfileRepository.saveOrUpdateWorker(updated)
                showEditDialog = false
                Toast.makeText(context, "Worker profile saved successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun WorkerCard(
    worker: WorkerProfile,
    isCurrentMonitored: Boolean,
    onSelectAsActive: () -> Unit,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val primaryColor = if (isDark) CyanNeon else CommandLightPrimary
    val emeraldColor = if (isDark) EmeraldOnline else CommandLightEmerald
    val hazardColor = if (isDark) HazardCrimson else CommandLightRed

    GlassCard(
        borderColor = if (isCurrentMonitored) primaryColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        backgroundColor = if (isDark) CommandSurfaceDark.copy(alpha = 0.95f) else Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header: Avatar + Worker Name & Department + Action Icons (Edit / Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isCurrentMonitored) primaryColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.5.dp,
                                if (isCurrentMonitored) primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = worker.workerName.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isCurrentMonitored) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = worker.workerName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else TextDark
                            )
                            if (isCurrentMonitored) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = emeraldColor.copy(alpha = 0.18f),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, emeraldColor.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "LIVE ACTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = emeraldColor,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${worker.workerId} • ${worker.department}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Edit Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = primaryColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // 2. Hardware Badges Flow (Assigned Vest, LoRa Node, Blood Group, Shift)
            OptInFlowBadges(worker = worker, primaryColor = primaryColor, isDark = isDark)

            // 3. Emergency Contact & Direct Call Dispatch Strip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDark) CommandSurfaceContainerDark else CommandLightSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = hazardColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Emergency (${worker.emergencyRelation})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else TextDark
                            )
                        }
                        Text(
                            text = "${worker.emergencyContactName} • ${worker.emergencyContactPhone}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryColor
                        )
                    }

                    // Direct Call Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (worker.phone.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(primaryColor.copy(alpha = 0.15f))
                                    .border(0.8.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call Worker",
                                    tint = primaryColor,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        if (worker.emergencyContactPhone.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.emergencyContactPhone}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(hazardColor.copy(alpha = 0.18f))
                                    .border(0.8.dp, hazardColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = "Emergency Call",
                                    tint = hazardColor,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Bottom Action Bar: Select for Telemetry & View Profile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isCurrentMonitored) {
                    OutlinedButton(
                        onClick = onSelectAsActive,
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                    ) {
                        Icon(imageVector = Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Monitor Live", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(emeraldColor)
                        )
                        Text(
                            text = "Streaming to Telemetry Console",
                            fontSize = 11.sp,
                            color = emeraldColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                TextButton(
                    onClick = onViewDetails,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("Full Profile →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptInFlowBadges(
    worker: WorkerProfile,
    primaryColor: Color,
    isDark: Boolean
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Vest ID Tag
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = primaryColor.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = worker.assignedVestId,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
        }

        // Blood Group Tag
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isDark) CommandSurfaceContainerDark else CommandLightSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Text(
                text = "Blood: ${worker.bloodGroup}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Shift Tag
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isDark) CommandSurfaceContainerDark else CommandLightSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Text(
                text = worker.shift,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // LoRa Node Tag
        if (worker.loraNode.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDark) CommandSurfaceContainerDark else CommandLightSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Text(
                    text = "LoRa: ${worker.loraNode}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, highlight: Boolean = false) {
    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifBlank { "Not Specified" },
            fontSize = 13.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) (if (isDark) CyanNeon else CommandLightPrimary) else (if (isDark) Color.White else TextDark)
        )
    }
}

@Composable
fun WorkerEditDialog(
    initialWorker: WorkerProfile,
    onDismiss: () -> Unit,
    onSave: (WorkerProfile) -> Unit
) {
    var name by remember { mutableStateOf(initialWorker.workerName) }
    var workerId by remember { mutableStateOf(initialWorker.workerId) }
    var vestId by remember { mutableStateOf(initialWorker.assignedVestId) }
    var phone by remember { mutableStateOf(initialWorker.phone) }
    var email by remember { mutableStateOf(initialWorker.email) }
    var bloodGroup by remember { mutableStateOf(initialWorker.bloodGroup) }
    var department by remember { mutableStateOf(initialWorker.department) }
    var shift by remember { mutableStateOf(initialWorker.shift) }
    var emergencyName by remember { mutableStateOf(initialWorker.emergencyContactName) }
    var emergencyPhone by remember { mutableStateOf(initialWorker.emergencyContactPhone) }
    var emergencyRelation by remember { mutableStateOf(initialWorker.emergencyRelation) }
    var medicalNotes by remember { mutableStateOf(initialWorker.medicalNotes) }

    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val primaryColor = if (isDark) CyanNeon else CommandLightPrimary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialWorker.workerName.isBlank()) "Add Worker Profile" else "Edit Worker Profile",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Worker Full Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = workerId,
                            onValueChange = { workerId = it },
                            label = { Text("Worker ID *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = vestId,
                            onValueChange = { vestId = it },
                            label = { Text("Assigned Vest ID *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = { bloodGroup = it },
                            label = { Text("Blood Grp") },
                            modifier = Modifier.weight(0.8f),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department / Plant Location") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Text(
                        text = "EMERGENCY DISPATCH CONTACT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        letterSpacing = 0.8.sp
                    )
                }
                item {
                    OutlinedTextField(
                        value = emergencyName,
                        onValueChange = { emergencyName = it },
                        label = { Text("Emergency Contact Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = emergencyPhone,
                            onValueChange = { emergencyPhone = it },
                            label = { Text("Emergency Phone *") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = emergencyRelation,
                            onValueChange = { emergencyRelation = it },
                            label = { Text("Relation") },
                            modifier = Modifier.weight(0.8f),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = medicalNotes,
                        onValueChange = { medicalNotes = it },
                        label = { Text("Medical Conditions / Hazard Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && workerId.isNotBlank() && vestId.isNotBlank()) {
                        onSave(
                            initialWorker.copy(
                                workerName = name.trim(),
                                workerId = workerId.trim(),
                                assignedVestId = vestId.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                bloodGroup = bloodGroup.trim(),
                                department = department.trim(),
                                shift = shift.trim(),
                                emergencyContactName = emergencyName.trim(),
                                emergencyContactPhone = emergencyPhone.trim(),
                                emergencyRelation = emergencyRelation.trim(),
                                medicalNotes = medicalNotes.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Save Profile", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = if (isDark) CommandSurfaceDark else Color.White
    )
}

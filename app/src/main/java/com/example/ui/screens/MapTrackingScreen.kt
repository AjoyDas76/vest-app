package com.example.ui.screens

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.manager.VestTelemetryManager
import com.example.model.WorkerProfile
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MapTrackingScreen(
    profile: WorkerProfile = WorkerProfile()
) {
    val vestData by VestTelemetryManager.vestData.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var googleMapInstance by remember { mutableStateOf<GoogleMap?>(null) }
    var workerMarker by remember { mutableStateOf<Marker?>(null) }
    // User requested Satellite view: default to HYBRID (satellite imagery with road/label overlays)
    var currentMapType by remember { mutableStateOf(GoogleMap.MAP_TYPE_HYBRID) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }

    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val primaryAccent = if (isDark) CyanNeon else MaterialTheme.colorScheme.primary
    val onlineColor = if (isDark) EmeraldOnline else CommandLightEmerald
    val offlineColor = if (isDark) OfflineRed else CommandLightRed

    val workerLatLng = remember(vestData.gps.latitude, vestData.gps.longitude) {
        val lat = if (vestData.gps.latitude != 0.0) vestData.gps.latitude else 23.8103
        val lng = if (vestData.gps.longitude != 0.0) vestData.gps.longitude else 90.4125
        LatLng(lat, lng)
    }

    // Sync map type when changed
    LaunchedEffect(currentMapType, googleMapInstance) {
        googleMapInstance?.mapType = currentMapType
    }

    // Update marker whenever position changes
    LaunchedEffect(workerLatLng, googleMapInstance) {
        val map = googleMapInstance ?: return@LaunchedEffect
        if (workerMarker == null) {
            val markerOptions = MarkerOptions()
                .position(workerLatLng)
                .title("${profile.workerName} (${profile.workerId})")
                .snippet("Status: ${vestData.status.motion_state}")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            workerMarker = map.addMarker(markerOptions)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(workerLatLng, 16f))
        } else {
            workerMarker?.position = workerLatLng
            workerMarker?.snippet = "Status: ${vestData.status.motion_state}"
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Google Map View
        val mapView = remember { MapView(context) }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    getMapAsync { map ->
                        googleMapInstance = map
                        map.uiSettings.isZoomControlsEnabled = false
                        map.uiSettings.isCompassEnabled = true
                        map.uiSettings.isMyLocationButtonEnabled = false
                        map.mapType = currentMapType

                        val markerOptions = MarkerOptions()
                            .position(workerLatLng)
                            .title("${profile.workerName} (${profile.workerId})")
                            .snippet("Status: ${vestData.status.motion_state}")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        workerMarker = map.addMarker(markerOptions)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(workerLatLng, 16f))
                    }
                }
            }
        )

        // 2. Map Control Strip at Top-End (Satellite toggle + Center on Worker)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Satellite / Hybrid Mode Toggle Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) CommandSurfaceDark.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, primaryAccent.copy(alpha = 0.6f)),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Satellite/Hybrid button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (currentMapType == GoogleMap.MAP_TYPE_HYBRID || currentMapType == GoogleMap.MAP_TYPE_SATELLITE) {
                            primaryAccent
                        } else {
                            Color.Transparent
                        },
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        IconButton(
                            onClick = {
                                currentMapType = if (currentMapType == GoogleMap.MAP_TYPE_HYBRID) {
                                    GoogleMap.MAP_TYPE_SATELLITE
                                } else {
                                    GoogleMap.MAP_TYPE_HYBRID
                                }
                                googleMapInstance?.mapType = currentMapType
                            },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Satellite,
                                    contentDescription = "Satellite View",
                                    tint = if (currentMapType == GoogleMap.MAP_TYPE_HYBRID || currentMapType == GoogleMap.MAP_TYPE_SATELLITE) {
                                        Color.Black
                                    } else {
                                        primaryAccent
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (currentMapType == GoogleMap.MAP_TYPE_SATELLITE) "Pure Satellite" else "Satellite",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentMapType == GoogleMap.MAP_TYPE_HYBRID || currentMapType == GoogleMap.MAP_TYPE_SATELLITE) {
                                        Color.Black
                                    } else {
                                        if (isDark) Color.White else TextDark
                                    }
                                )
                            }
                        }
                    }

                    // Standard Street Map button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (currentMapType == GoogleMap.MAP_TYPE_NORMAL) {
                            primaryAccent
                        } else {
                            Color.Transparent
                        },
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        IconButton(
                            onClick = {
                                currentMapType = GoogleMap.MAP_TYPE_NORMAL
                                googleMapInstance?.mapType = currentMapType
                            },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Street View",
                                    tint = if (currentMapType == GoogleMap.MAP_TYPE_NORMAL) {
                                        Color.Black
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Street",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentMapType == GoogleMap.MAP_TYPE_NORMAL) {
                                        Color.Black
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Center On Worker Floating Action Button
            FloatingActionButton(
                onClick = {
                    googleMapInstance?.animateCamera(CameraUpdateFactory.newLatLngZoom(workerLatLng, 17f))
                },
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(12.dp),
                containerColor = primaryAccent,
                contentColor = if (isDark) Color(0xFF002930) else MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Center on Worker", modifier = Modifier.size(22.dp))
            }
        }

        // 3. Coordinate HUD Card at Bottom
        GlassCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            borderColor = MaterialTheme.colorScheme.outline
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (vestData.isOnline) onlineColor else offlineColor)
                        )
                        Text(
                            text = "GPS TELEMETRY HUD",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Fix: ${timeFormat.format(Date(if (vestData.lastPacketTime > 0) vestData.lastPacketTime else System.currentTimeMillis()))}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LATITUDE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (vestData.isOnline) String.format(Locale.US, "%.6f° N", workerLatLng.latitude) else "---",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LONGITUDE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (vestData.isOnline) String.format(Locale.US, "%.6f° E", workerLatLng.longitude) else "---",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), thickness = 0.8.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Motion: ${if (vestData.isOnline) vestData.status.motion_state else "---"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(
                        onClick = {
                            googleMapInstance?.animateCamera(CameraUpdateFactory.newLatLngZoom(workerLatLng, 18f))
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Focus Target", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

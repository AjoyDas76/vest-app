package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun OnlineStatusPill(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val activeColor = Color(0xFF00E676)
    val inactiveColor = Color(0xFFFF5252)
    val statusColor = if (isOnline) activeColor else inactiveColor
    val pillBg = if (isOnline) Color(0x1F00E676) else Color(0x1FFF5252)
    val pillBorder = statusColor
    val statusText = if (isOnline) "ONLINE" else "OFFLINE"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(pillBg)
            .border(1.2.dp, pillBorder, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
            contentDescription = statusText,
            tint = statusColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = statusText,
            color = statusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
fun TelemetryMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconColor: Color,
    statusSubtitle: String,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val warningColor = if (isDark) HazardCrimson else CommandLightRed
    val borderColor = if (isWarning) warningColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    val bgCard = if (isWarning) {
        if (isDark) MaterialTheme.colorScheme.error.copy(alpha = 0.22f) else Color(0x33DC2626)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgCard)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.6.sp
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = if (isDark) 0.15f else 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isWarning) warningColor else MaterialTheme.colorScheme.onSurface
                )
                if (value != "---" && unit.isNotBlank()) {
                    Text(
                        text = unit,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            Text(
                text = statusSubtitle,
                fontSize = 11.sp,
                fontWeight = if (isWarning) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isWarning) warningColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Reusable Futuristic HUD Graphic Components

@Composable
fun HudCategoryBadge(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    alpha: Float = 0.85f
) {
    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val surfaceColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0x66FFFFFF)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.18f) else Color(0x400284C7)
    val iconColor = if (isDark) Color.White.copy(alpha = alpha) else Color(0xFF0369A1).copy(alpha = alpha)
    val textColor = if (isDark) Color.White.copy(alpha = alpha * 0.8f) else Color(0xFF0C4A6E).copy(alpha = alpha * 0.9f)

    Surface(
        modifier = modifier
            .width(50.dp)
            .height(52.dp),
        shape = RoundedCornerShape(8.dp),
        color = surfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 6.5.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = 0.4.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DotMatrixHud(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val dotColor = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.35f) else Color(0xFF0284C7).copy(alpha = 0.35f)
    Canvas(modifier = modifier) {
        val spacing = 8.dp.toPx()
        val radius = 1.6.dp.toPx()
        val rows = 5
        val cols = 6
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                drawCircle(
                    color = dotColor,
                    radius = radius,
                    center = Offset(c * spacing, r * spacing)
                )
            }
        }
    }
}

@Composable
fun RadarReticleHud(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val radarColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF0284C7)
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension / 2
        drawCircle(
            color = radarColor.copy(alpha = 0.25f),
            radius = maxRadius,
            style = Stroke(width = 1.2.dp.toPx())
        )
        drawCircle(
            color = radarColor.copy(alpha = 0.35f),
            radius = maxRadius * 0.55f,
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = radarColor.copy(alpha = 0.6f),
            radius = 2.5.dp.toPx()
        )
        drawLine(
            color = radarColor.copy(alpha = 0.2f),
            start = Offset(0f, center.y),
            end = Offset(size.width, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = radarColor.copy(alpha = 0.2f),
            start = Offset(center.x, 0f),
            end = Offset(center.x, size.height),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
fun CircuitLinesHud(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val color = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.28f) else Color(0xFF0284C7).copy(alpha = 0.32f)
    Canvas(modifier = modifier) {
        val stroke = 1.dp.toPx()
        drawLine(color, Offset(0f, 10.dp.toPx()), Offset(30.dp.toPx(), 10.dp.toPx()), stroke)
        drawLine(color, Offset(30.dp.toPx(), 10.dp.toPx()), Offset(45.dp.toPx(), 22.dp.toPx()), stroke)
        drawLine(color, Offset(45.dp.toPx(), 22.dp.toPx()), Offset(85.dp.toPx(), 22.dp.toPx()), stroke)
        drawCircle(color, 2.dp.toPx(), Offset(85.dp.toPx(), 22.dp.toPx()))
    }
}

@Composable
fun TelemetryLinesHud(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark
    val color = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.28f) else Color(0xFF0284C7).copy(alpha = 0.32f)
    Canvas(modifier = modifier) {
        val stroke = 1.dp.toPx()
        drawLine(color, Offset(0f, size.height - 12.dp.toPx()), Offset(size.width - 25.dp.toPx(), size.height - 12.dp.toPx()), stroke)
        drawLine(color, Offset(20.dp.toPx(), size.height - 4.dp.toPx()), Offset(size.width, size.height - 4.dp.toPx()), stroke)
    }
}

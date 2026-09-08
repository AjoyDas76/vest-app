package com.example.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.manager.VestTelemetryManager
import com.example.model.EventSnapshot
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

enum class ChartMetric {
    TEMPERATURE,
    HUMIDITY,
    PRESSURE,
    ALL
}

@Composable
fun LiveChartScreen() {
    val chartHistory by VestTelemetryManager.chartHistory.collectAsState()
    val vestData by VestTelemetryManager.vestData.collectAsState()
    var selectedMetric by remember { mutableStateOf(ChartMetric.ALL) }

    val isDark = MaterialTheme.colorScheme.background == CommandNavyDark

    // Ensure we have some points to display even before multiple packets arrive
    val displayPoints = remember(chartHistory, vestData) {
        if (chartHistory.size < 2) {
            val now = System.currentTimeMillis()
            listOf(
                EventSnapshot(
                    timestamp = now - 15000,
                    temperature = vestData.environment.temperature - 0.4,
                    humidity = vestData.environment.humidity + 1.2,
                    pressure = vestData.environment.pressure - 0.2
                ),
                EventSnapshot(
                    timestamp = now - 10000,
                    temperature = vestData.environment.temperature + 0.2,
                    humidity = vestData.environment.humidity - 0.5,
                    pressure = vestData.environment.pressure + 0.1
                ),
                EventSnapshot(
                    timestamp = now - 5000,
                    temperature = vestData.environment.temperature - 0.1,
                    humidity = vestData.environment.humidity + 0.3,
                    pressure = vestData.environment.pressure
                ),
                EventSnapshot(
                    timestamp = now,
                    temperature = vestData.environment.temperature,
                    humidity = vestData.environment.humidity,
                    pressure = vestData.environment.pressure
                )
            )
        } else {
            chartHistory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val primaryAccent = if (isDark) CyanNeon else MaterialTheme.colorScheme.primary

        // Title and Metric Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "REAL-TIME TELEMETRY GRAPH",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Live LoRa sparkline with time markers",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Surface(
                color = primaryAccent.copy(alpha = if (isDark) 0.15f else 0.12f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = primaryAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Auto-stream",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent
                    )
                }
            }
        }

        // Tab Selector Row
        SingleChoiceMetricSegment(
            selectedMetric = selectedMetric,
            onMetricSelected = { selectedMetric = it }
        )

        // Main MPAndroidChart Container
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            borderColor = MaterialTheme.colorScheme.outline
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(true)
                        setPinchZoom(true)
                        setDrawGridBackground(false)
                        legend.textColor = if (isDark) AndroidColor.WHITE else AndroidColor.parseColor("#0F172A")
                        legend.textSize = 11f

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            textColor = if (isDark) AndroidColor.LTGRAY else AndroidColor.parseColor("#475569")
                            gridColor = if (isDark) AndroidColor.argb(40, 255, 255, 255) else AndroidColor.argb(40, 15, 23, 42)
                            setDrawGridLines(true)
                            setAvoidFirstLastClipping(true)
                            valueFormatter = object : ValueFormatter() {
                                private val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
                                override fun getFormattedValue(value: Float): String {
                                    return sdf.format(Date(value.toLong()))
                                }
                            }
                        }

                        axisLeft.apply {
                            textColor = if (isDark) AndroidColor.LTGRAY else AndroidColor.parseColor("#475569")
                            gridColor = if (isDark) AndroidColor.argb(40, 255, 255, 255) else AndroidColor.argb(40, 15, 23, 42)
                            setDrawGridLines(true)
                        }

                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    updateChartData(chart, displayPoints, selectedMetric, isDark)
                }
            )
        }

        // Stats Summary Cards for Current Metric
        LiveStatsRow(
            metric = selectedMetric,
            points = displayPoints,
            vestData = vestData
        )
    }
}

@Composable
fun SingleChoiceMetricSegment(
    selectedMetric: ChartMetric,
    onMetricSelected: (ChartMetric) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val options = listOf(
            ChartMetric.ALL to "All",
            ChartMetric.TEMPERATURE to "Temp (°C)",
            ChartMetric.HUMIDITY to "Hum (%)",
            ChartMetric.PRESSURE to "Pres (hPa)"
        )

        options.forEach { (metric, label) ->
            val isSelected = selectedMetric == metric
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { onMetricSelected(metric) },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun updateChartData(
    chart: LineChart,
    points: List<EventSnapshot>,
    metric: ChartMetric,
    isDark: Boolean
) {
    if (points.isEmpty()) return

    val lineData = LineData()

    // 1. Temperature Dataset (Cyan / Deep Teal)
    val tempColorStr = if (isDark) "#00E5FF" else "#006D80"
    val humColorStr = if (isDark) "#00B4D8" else "#0284C7"
    val presColorStr = if (isDark) "#FF9100" else "#B45309"

    if (metric == ChartMetric.TEMPERATURE || metric == ChartMetric.ALL) {
        val tempEntries = points.map { Entry(it.timestamp.toFloat(), it.temperature.toFloat()) }
        val tempSet = LineDataSet(tempEntries, "Temp (°C)").apply {
            color = AndroidColor.parseColor(tempColorStr)
            setCircleColor(AndroidColor.parseColor(tempColorStr))
            lineWidth = 2.5f
            circleRadius = 3f
            setDrawCircleHole(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = AndroidColor.parseColor(tempColorStr)
            fillAlpha = if (isDark) 35 else 25
        }
        lineData.addDataSet(tempSet)
    }

    // 2. Humidity Dataset (Teal / Blue)
    if (metric == ChartMetric.HUMIDITY || metric == ChartMetric.ALL) {
        val humEntries = points.map { Entry(it.timestamp.toFloat(), it.humidity.toFloat()) }
        val humSet = LineDataSet(humEntries, "Humidity (%)").apply {
            color = AndroidColor.parseColor(humColorStr)
            setCircleColor(AndroidColor.parseColor(humColorStr))
            lineWidth = 2.5f
            circleRadius = 3f
            setDrawCircleHole(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = AndroidColor.parseColor(humColorStr)
            fillAlpha = if (isDark) 35 else 25
        }
        lineData.addDataSet(humSet)
    }

    // 3. Pressure Dataset (Amber)
    if (metric == ChartMetric.PRESSURE) {
        val presEntries = points.map { Entry(it.timestamp.toFloat(), it.pressure.toFloat()) }
        val presSet = LineDataSet(presEntries, "Pressure (hPa)").apply {
            color = AndroidColor.parseColor(presColorStr)
            setCircleColor(AndroidColor.parseColor(presColorStr))
            lineWidth = 2.5f
            circleRadius = 3f
            setDrawCircleHole(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = AndroidColor.parseColor(presColorStr)
            fillAlpha = if (isDark) 35 else 25
        }
        lineData.addDataSet(presSet)
    }

    chart.data = lineData
    chart.notifyDataSetChanged()
    chart.invalidate()
}

@Composable
fun LiveStatsRow(
    metric: ChartMetric,
    points: List<EventSnapshot>,
    vestData: com.example.model.VestData
) {
    val currentVal: Double
    val unitStr: String
    val minVal: Double
    val maxVal: Double
    val avgVal: Double

    when (metric) {
        ChartMetric.TEMPERATURE, ChartMetric.ALL -> {
            currentVal = vestData.environment.temperature
            unitStr = "°C"
            val temps = points.map { it.temperature }
            minVal = temps.minOrNull() ?: currentVal
            maxVal = temps.maxOrNull() ?: currentVal
            avgVal = if (temps.isNotEmpty()) temps.average() else currentVal
        }
        ChartMetric.HUMIDITY -> {
            currentVal = vestData.environment.humidity
            unitStr = "%"
            val hums = points.map { it.humidity }
            minVal = hums.minOrNull() ?: currentVal
            maxVal = hums.maxOrNull() ?: currentVal
            avgVal = if (hums.isNotEmpty()) hums.average() else currentVal
        }
        ChartMetric.PRESSURE -> {
            currentVal = vestData.environment.pressure
            unitStr = "hPa"
            val press = points.map { it.pressure }
            minVal = press.minOrNull() ?: currentVal
            maxVal = press.maxOrNull() ?: currentVal
            avgVal = if (press.isNotEmpty()) press.average() else currentVal
        }
    }

    val currentDisplay = if (vestData.isOnline) String.format(Locale.US, "%.1f%s", currentVal, unitStr) else "---"
    val minDisplay = if (vestData.isOnline || points.isNotEmpty()) String.format(Locale.US, "%.1f%s", minVal, unitStr) else "---"
    val maxDisplay = if (vestData.isOnline || points.isNotEmpty()) String.format(Locale.US, "%.1f%s", maxVal, unitStr) else "---"
    val avgDisplay = if (vestData.isOnline || points.isNotEmpty()) String.format(Locale.US, "%.1f%s", avgVal, unitStr) else "---"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatItemCard(label = "Current", value = currentDisplay, modifier = Modifier.weight(1f))
        StatItemCard(label = "Min", value = minDisplay, modifier = Modifier.weight(1f))
        StatItemCard(label = "Max", value = maxDisplay, modifier = Modifier.weight(1f))
        StatItemCard(label = "Avg", value = avgDisplay, modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatItemCard(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier,
        borderColor = MaterialTheme.colorScheme.outline
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

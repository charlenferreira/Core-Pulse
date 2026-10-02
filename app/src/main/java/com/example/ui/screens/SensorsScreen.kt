package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SensorItem
import com.example.ui.CorePulseViewModel
import com.example.ui.components.InfoRow
import com.example.ui.components.ScreenHeader
import com.example.ui.theme.CyanPulse
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.NeonPurple

@Composable
fun SensorsScreen(
    viewModel: CorePulseViewModel
) {
    val context = LocalContext.current
    val sensors by viewModel.sensors.collectAsState()
    val displayInfo by viewModel.displayInfo.collectAsState()

    var showDeadPixelTest by remember { mutableStateOf(false) }
    var deadPixelColorIndex by remember { mutableIntStateOf(0) }
    val deadPixelColors = remember {
        listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black, Color.Yellow, Color.Cyan, Color.Magenta)
    }

    var liveSensorType by remember { mutableIntStateOf(Sensor.TYPE_ACCELEROMETER) }
    var sensorValues by remember { mutableStateOf(floatArrayOf(0f, 0f, 0f)) }

    // Sensor listener lifecycle
    DisposableEffect(liveSensorType) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val targetSensor = sensorManager?.getDefaultSensor(liveSensorType)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let {
                    if (it.isNotEmpty()) {
                        sensorValues = it.clone()
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (targetSensor != null) {
            sensorManager.registerListener(listener, targetSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Dead Pixel Full-Screen Test Mode
    if (showDeadPixelTest) {
        BackHandler {
            showDeadPixelTest = false
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(deadPixelColors[deadPixelColorIndex])
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (deadPixelColorIndex < deadPixelColors.size - 1) {
                                deadPixelColorIndex++
                            } else {
                                showDeadPixelTest = false
                                deadPixelColorIndex = 0
                            }
                        }
                    )
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier.padding(24.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "Tap screen to cycle colors (${deadPixelColorIndex + 1}/${deadPixelColors.size}) • Back to exit",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ScreenHeader(
                title = "Sensors & Display",
                subtitle = "${sensors.size} Hardware Sensors Detected",
                statusText = "SENSORS ACTIVE",
                onRefresh = { viewModel.refreshAllMetrics() }
            )
        }

        // Display Specs Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Tv, contentDescription = null, tint = CyanPulse, modifier = Modifier.size(20.dp))
                            Text(
                                text = "DISPLAY METRICS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = CyanPulse
                            )
                        }

                        Button(
                            onClick = {
                                deadPixelColorIndex = 0
                                showDeadPixelTest = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPulse.copy(alpha = 0.15f),
                                contentColor = CyanPulse
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Pixel Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    InfoRow("Resolution", "${displayInfo.widthPx} x ${displayInfo.heightPx} px", isMonospace = true)
                    InfoRow("Refresh Rate", "${"%.0f".format(displayInfo.refreshRateHz)} Hz", isMonospace = true)
                    InfoRow("Screen Density", "${displayInfo.densityDpi} DPI (${"%.1f".format(displayInfo.densityScale)}x)", isMonospace = true)
                    InfoRow("HDR Display Support", if (displayInfo.isHdrSupported) "Yes (High Dynamic Range)" else "SDR (Standard)")
                    InfoRow("Wide Color Gamut", if (displayInfo.isWideColorGamut) "Yes (DCI-P3)" else "sRGB")
                }
            }
        }

        // Live Sensor Monitor Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INTERACTIVE SENSOR LAB",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = EmeraldPulse
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Sensor Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = liveSensorType == Sensor.TYPE_ACCELEROMETER,
                            onClick = { liveSensorType = Sensor.TYPE_ACCELEROMETER },
                            label = { Text("Accelerometer", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = liveSensorType == Sensor.TYPE_GYROSCOPE,
                            onClick = { liveSensorType = Sensor.TYPE_GYROSCOPE },
                            label = { Text("Gyroscope", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = liveSensorType == Sensor.TYPE_LIGHT,
                            onClick = { liveSensorType = Sensor.TYPE_LIGHT },
                            label = { Text("Light", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (liveSensorType == Sensor.TYPE_ACCELEROMETER) {
                        // Accelerometer Spirit Bubble Level Canvas
                        val x = sensorValues.getOrElse(0) { 0f }
                        val y = sensorValues.getOrElse(1) { 0f }
                        val z = sensorValues.getOrElse(2) { 0f }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Bubble Canvas
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val radius = size.width / 2f - 4.dp.toPx()

                                    // Outer ring
                                    drawCircle(
                                        color = EmeraldPulse.copy(alpha = 0.3f),
                                        radius = radius,
                                        center = center,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                    // Target crosshairs
                                    drawLine(
                                        color = EmeraldPulse.copy(alpha = 0.2f),
                                        start = Offset(center.x - radius, center.y),
                                        end = Offset(center.x + radius, center.y),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                    drawLine(
                                        color = EmeraldPulse.copy(alpha = 0.2f),
                                        start = Offset(center.x, center.y - radius),
                                        end = Offset(center.x, center.y + radius),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                    // Center inner ring
                                    drawCircle(
                                        color = EmeraldPulse.copy(alpha = 0.4f),
                                        radius = 12.dp.toPx(),
                                        center = center,
                                        style = Stroke(width = 1.dp.toPx())
                                    )

                                    // Moving Spirit Bubble (x and y mapped to circle bounds)
                                    val maxShift = radius - 16.dp.toPx()
                                    val bubbleX = center.x - (x / 9.8f * maxShift).coerceIn(-maxShift, maxShift)
                                    val bubbleY = center.y + (y / 9.8f * maxShift).coerceIn(-maxShift, maxShift)

                                    drawCircle(
                                        color = EmeraldPulse,
                                        radius = 10.dp.toPx(),
                                        center = Offset(bubbleX, bubbleY)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                Text(
                                    text = "3-AXIS ACCELERATION",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPulse
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "X: ${"%.3f".format(x)} m/s²",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Y: ${"%.3f".format(y)} m/s²",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Z: ${"%.3f".format(z)} m/s²",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else if (liveSensorType == Sensor.TYPE_LIGHT) {
                        val lux = sensorValues.getOrElse(0) { 0f }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "AMBIENT ILLUMINANCE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPulse
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${"%.1f".format(lux)} LUX",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = EmeraldPulse, modifier = Modifier.size(48.dp))
                        }
                    } else {
                        val gx = sensorValues.getOrElse(0) { 0f }
                        val gy = sensorValues.getOrElse(1) { 0f }
                        val gz = sensorValues.getOrElse(2) { 0f }
                        Column {
                            Text(
                                text = "ANGULAR ROTATION VELOCITY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPulse
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Pitch (X): ${"%.3f".format(gx)} rad/s", fontFamily = FontFamily.Monospace)
                            Text("Roll (Y): ${"%.3f".format(gy)} rad/s", fontFamily = FontFamily.Monospace)
                            Text("Yaw (Z): ${"%.3f".format(gz)} rad/s", fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Full Sensor Inventory Header
        item {
            Text(
                text = "DETECTED SENSOR CATALOG (${sensors.size})",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(sensors) { sensor ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sensor.typeName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "${sensor.powerMa} mA",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                ),
                                color = CyanPulse
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sensor.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Vendor: ${sensor.vendor} • Max Range: ${sensor.maxRange}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

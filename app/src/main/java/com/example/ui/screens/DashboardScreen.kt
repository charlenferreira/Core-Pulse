package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CorePulseViewModel
import com.example.ui.components.PulseGauge
import com.example.ui.components.PulseLineChart
import com.example.ui.components.ScreenHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: CorePulseViewModel,
    onNavigateToCpu: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToBattery: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onNavigateToBenchmark: () -> Unit
) {
    val context = LocalContext.current
    val cpuInfo by viewModel.cpuInfo.collectAsState()
    val cpuHistory by viewModel.cpuHistory.collectAsState()
    val memoryInfo by viewModel.memoryInfo.collectAsState()
    val storageInfo by viewModel.storageInfo.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val networkInfo by viewModel.networkInfo.collectAsState()
    val deviceSpecs by viewModel.deviceSpecs.collectAsState()
    val isOptimizingRam by viewModel.isOptimizingRam.collectAsState()
    val ramFreed by viewModel.ramFreedBytes.collectAsState()

    LaunchedEffect(ramFreed) {
        ramFreed?.let { bytes ->
            val mb = bytes / (1024 * 1024)
            Toast.makeText(context, "RAM Optimized: ~${mb}MB reclaimed", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ScreenHeader(
                title = "Core Pulse",
                subtitle = "${deviceSpecs.brand} ${deviceSpecs.model} • Android ${deviceSpecs.androidVersion}",
                statusText = if (batteryInfo.isPowerSaveMode) "POWER SAVE MODE" else "SYSTEM ACTIVE",
                statusColor = if (batteryInfo.isPowerSaveMode) AmberWarning else EmeraldPulse,
                onRefresh = { viewModel.refreshAllMetrics() },
                onShare = {
                    val report = viewModel.getDiagnosticReport()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Core Pulse Report", report))
                    Toast.makeText(context, "Diagnostic report copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // System Vital Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, CyanPulse.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SYSTEM STATUS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = CyanPulse
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hardware Pulse Optimal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Uptime: ${deviceSpecs.uptimeFormatted} • ${cpuInfo.coreCount} Cores",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { viewModel.optimizeRam() },
                        enabled = !isOptimizingRam,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPulse,
                            contentColor = DarkObsidian
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isOptimizingRam) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = DarkObsidian
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Optimize", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Circular Vitals Gauge Row
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CORE GAUGES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        PulseGauge(
                            percent = cpuInfo.overallUsagePercent,
                            title = "CPU",
                            subtitle = "${cpuInfo.coreCount} Cores",
                            size = 100.dp,
                            strokeWidth = 8.dp,
                            primaryColor = CyanPulse
                        )

                        val usedRamGb = "%.1f".format(memoryInfo.usedRamBytes / (1024.0 * 1024.0 * 1024.0))
                        val totalRamGb = "%.1f".format(memoryInfo.totalRamBytes / (1024.0 * 1024.0 * 1024.0))
                        PulseGauge(
                            percent = memoryInfo.usedPercent,
                            title = "RAM",
                            subtitle = "$usedRamGb / ${totalRamGb}G",
                            size = 100.dp,
                            strokeWidth = 8.dp,
                            primaryColor = EmeraldPulse
                        )

                        PulseGauge(
                            percent = batteryInfo.percentage.toFloat(),
                            title = "BATTERY",
                            subtitle = batteryInfo.status,
                            size = 100.dp,
                            strokeWidth = 8.dp,
                            primaryColor = if (batteryInfo.percentage <= 20) CoralAlert else NeonPurple
                        )
                    }
                }
            }
        }

        // Real-Time CPU Activity Sparkline Card
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
                    PulseLineChart(
                        dataPoints = cpuHistory,
                        lineColor = CyanPulse,
                        fillColor = CyanPulse.copy(alpha = 0.15f),
                        label = "REAL-TIME CPU WORKLOAD FREQUENCY"
                    )
                }
            }
        }

        // System Component Cards
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // RAM Deep Card
                val usedRamMb = memoryInfo.usedRamBytes / (1024 * 1024)
                val totalRamMb = memoryInfo.totalRamBytes / (1024 * 1024)
                StatCard(
                    title = "RAM Memory",
                    value = "${"%.1f".format(memoryInfo.usedPercent)}% Used",
                    subtitle = "$usedRamMb MB of $totalRamMb MB Total",
                    icon = Icons.Default.Memory,
                    progress = memoryInfo.usedPercent / 100f,
                    accentColor = EmeraldPulse,
                    trailingBadge = if (memoryInfo.isLowMemory) "LOW" else "NORMAL"
                )

                // Storage Card
                val usedStorageGb = "%.1f".format(storageInfo.internalUsedBytes / (1024.0 * 1024.0 * 1024.0))
                val totalStorageGb = "%.1f".format(storageInfo.internalTotalBytes / (1024.0 * 1024.0 * 1024.0))
                StatCard(
                    title = "Internal Storage",
                    value = "$usedStorageGb GB / $totalStorageGb GB",
                    subtitle = "${"%.1f".format(storageInfo.internalUsedPercent)}% Occupied • Cache: ~${storageInfo.appCacheSizeBytes / 1024} KB",
                    icon = Icons.Default.Storage,
                    progress = storageInfo.internalUsedPercent / 100f,
                    accentColor = ElectricBlue,
                    trailingBadge = "${"%.0f".format(100f - storageInfo.internalUsedPercent)}% FREE"
                )

                // Battery Card
                StatCard(
                    title = "Battery & Thermal",
                    value = "${batteryInfo.percentage}% • ${batteryInfo.status}",
                    subtitle = "Temp: ${"%.1f".format(batteryInfo.temperatureCelsius)}°C (${"%.1f".format(batteryInfo.temperatureFahrenheit)}°F) • ${batteryInfo.voltageMv} mV • ${batteryInfo.technology}",
                    icon = Icons.Default.BatteryChargingFull,
                    progress = batteryInfo.percentage / 100f,
                    accentColor = if (batteryInfo.percentage <= 20) CoralAlert else AmberWarning,
                    trailingBadge = batteryInfo.health
                )

                // Network Card
                StatCard(
                    title = "Network & Link",
                    value = if (networkInfo.isConnected) networkInfo.connectionType else "Disconnected",
                    subtitle = "IPv4: ${networkInfo.ipv4Address} • Interface: ${networkInfo.interfaceName}",
                    icon = Icons.Default.Wifi,
                    accentColor = CyanPulse,
                    trailingBadge = if (networkInfo.isConnected) "ONLINE" else "OFFLINE"
                )
            }
        }

        // Quick Navigation Tiles
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "DIAGNOSTIC SUITES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToCpu,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CyanPulse.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = CyanPulse, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CPU Spec", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToBenchmark,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, EmeraldPulse.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = EmeraldPulse, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Benchmark", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CorePulseViewModel
import com.example.ui.components.InfoRow
import com.example.ui.components.PulseGauge
import com.example.ui.components.ScreenHeader
import com.example.ui.theme.CyanPulse
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldPulse

@Composable
fun MemoryStorageScreen(
    viewModel: CorePulseViewModel
) {
    val context = LocalContext.current
    val memoryInfo by viewModel.memoryInfo.collectAsState()
    val storageInfo by viewModel.storageInfo.collectAsState()
    val isOptimizingRam by viewModel.isOptimizingRam.collectAsState()
    val ramFreed by viewModel.ramFreedBytes.collectAsState()
    val isCleaningCache by viewModel.isCleaningCache.collectAsState()
    val cacheCleaned by viewModel.cacheCleanedBytes.collectAsState()

    LaunchedEffect(cacheCleaned) {
        cacheCleaned?.let { bytes ->
            val kb = bytes / 1024
            Toast.makeText(context, "App Cache Cleaned: ~$kb KB freed", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ScreenHeader(
                title = "Memory & Storage",
                subtitle = "RAM Allocation & File System Metrics",
                statusText = if (memoryInfo.isLowMemory) "LOW MEMORY" else "MEMORY HEALTHY",
                onRefresh = { viewModel.refreshAllMetrics() }
            )
        }

        // RAM Section Header & Gauge
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
                        PulseGauge(
                            percent = memoryInfo.usedPercent,
                            title = "RAM USED",
                            subtitle = "${"%.1f".format(memoryInfo.usedPercent)}%",
                            size = 120.dp,
                            strokeWidth = 10.dp,
                            primaryColor = EmeraldPulse
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        ) {
                            Text(
                                text = "SYSTEM RAM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = EmeraldPulse
                            )
                            val totalGb = "%.2f".format(memoryInfo.totalRamBytes / (1024.0 * 1024.0 * 1024.0))
                            val usedGb = "%.2f".format(memoryInfo.usedRamBytes / (1024.0 * 1024.0 * 1024.0))
                            val freeGb = "%.2f".format(memoryInfo.availableRamBytes / (1024.0 * 1024.0 * 1024.0))

                            Text(
                                text = "$usedGb GB / $totalGb GB",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Available: $freeGb GB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.optimizeRam() },
                                enabled = !isOptimizingRam,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPulse,
                                    contentColor = DarkObsidian
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isOptimizingRam) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = DarkObsidian
                                    )
                                } else {
                                    Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Trim Memory Cache", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    val threshMb = memoryInfo.thresholdBytes / (1024 * 1024)
                    val jvmAllocMb = memoryInfo.jvmAllocatedBytes / (1024 * 1024)
                    val jvmMaxMb = memoryInfo.jvmMaxBytes / (1024 * 1024)

                    InfoRow("RAM Low Memory State", if (memoryInfo.isLowMemory) "YES (CRITICAL)" else "No (Normal)")
                    InfoRow("Kernel Low Memory Threshold", "$threshMb MB", isMonospace = true)
                    InfoRow("App JVM Heap Allocated", "$jvmAllocMb MB", isMonospace = true)
                    InfoRow("App JVM Max Heap Limit", "$jvmMaxMb MB", isMonospace = true)
                }
            }
        }

        // Storage Deep Dive Card
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
                            Icon(Icons.Default.Storage, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                            Text(
                                text = "INTERNAL STORAGE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = ElectricBlue
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ElectricBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${"%.1f".format(storageInfo.internalUsedPercent)}% FULL",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = ElectricBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val usedGb = "%.2f".format(storageInfo.internalUsedBytes / (1024.0 * 1024.0 * 1024.0))
                    val totalGb = "%.2f".format(storageInfo.internalTotalBytes / (1024.0 * 1024.0 * 1024.0))
                    val freeGb = "%.2f".format(storageInfo.internalFreeBytes / (1024.0 * 1024.0 * 1024.0))

                    Text(
                        text = "$usedGb GB Used of $totalGb GB",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$freeGb GB Available Free Disk Space",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { storageInfo.internalUsedPercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = ElectricBlue,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // App Cache Cleaner Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "App Temporary Cache",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val cacheKb = storageInfo.appCacheSizeBytes / 1024
                            Text(
                                text = "$cacheKb KB occupied",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.cleanCache() },
                            enabled = !isCleaningCache,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.6f))
                        ) {
                            if (isCleaningCache) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clean Cache", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            }
                        }
                    }

                    if (storageInfo.externalTotalBytes != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val extTotalGb = "%.2f".format(storageInfo.externalTotalBytes!! / (1024.0 * 1024.0 * 1024.0))
                        val extFreeGb = "%.2f".format((storageInfo.externalFreeBytes ?: 0L) / (1024.0 * 1024.0 * 1024.0))
                        InfoRow("External SD Card Total", "$extTotalGb GB", isMonospace = true)
                        InfoRow("External SD Card Free", "$extFreeGb GB", isMonospace = true)
                    }
                }
            }
        }
    }
}

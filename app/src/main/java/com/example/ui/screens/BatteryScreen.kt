package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CorePulseViewModel
import com.example.ui.components.InfoRow
import com.example.ui.components.PulseGauge
import com.example.ui.components.ScreenHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.NeonPurple

@Composable
fun BatteryScreen(
    viewModel: CorePulseViewModel
) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ScreenHeader(
                title = "Power & Thermals",
                subtitle = "${batteryInfo.technology} • ${batteryInfo.pluggedSource}",
                statusText = if (batteryInfo.status == "Charging") "CHARGING POWER" else "DISCHARGING",
                statusColor = if (batteryInfo.status == "Charging") EmeraldPulse else AmberWarning,
                onRefresh = { viewModel.refreshAllMetrics() }
            )
        }

        // Main Battery Gauge Card
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val gaugeColor = when {
                            batteryInfo.percentage <= 20 -> CoralAlert
                            batteryInfo.percentage <= 45 -> AmberWarning
                            else -> EmeraldPulse
                        }

                        PulseGauge(
                            percent = batteryInfo.percentage.toFloat(),
                            title = "CHARGE",
                            subtitle = batteryInfo.status,
                            size = 120.dp,
                            strokeWidth = 10.dp,
                            primaryColor = gaugeColor
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        ) {
                            Text(
                                text = "BATTERY HEALTH",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = EmeraldPulse
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = batteryInfo.health,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Source: ${batteryInfo.pluggedSource}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Power Save: ${if (batteryInfo.isPowerSaveMode) "Active" else "Disabled"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (batteryInfo.isPowerSaveMode) AmberWarning else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    InfoRow("Battery Status", batteryInfo.status)
                    InfoRow("Power Source", batteryInfo.pluggedSource)
                    InfoRow("Battery Voltage", "${batteryInfo.voltageMv} mV", isMonospace = true)
                    InfoRow(
                        "Thermal Temperature",
                        "${"%.1f".format(batteryInfo.temperatureCelsius)}°C / ${"%.1f".format(batteryInfo.temperatureFahrenheit)}°F",
                        isMonospace = true,
                        valueColor = if (batteryInfo.temperatureCelsius > 42f) CoralAlert else MaterialTheme.colorScheme.onSurface
                    )
                    InfoRow("Cell Chemistry / Tech", batteryInfo.technology, isMonospace = true)
                    InfoRow("Power Save State", if (batteryInfo.isPowerSaveMode) "Active (Restricted)" else "Normal Profile")
                    InfoRow("Interactive Screen State", if (batteryInfo.isInteractive) "Awake (Screen On)" else "Standby")
                }
            }
        }

        // Thermal Alert & Tips Card
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = EmeraldPulse, modifier = Modifier.size(20.dp))
                        Text(
                            text = "SMART BATTERY ADVICE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = EmeraldPulse
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• To extend lithium cell lifespan, avoid letting charge drop below 15%.\n" +
                               "• Keep thermal profile under 40°C during fast charging cycles.\n" +
                               "• Lithium cells retain optimal capacity when maintained between 20% and 80% charge state.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
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
import com.example.ui.components.ScreenHeader
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CyanPulse
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.EmeraldPulse

@Composable
fun NetworkScreen(
    viewModel: CorePulseViewModel
) {
    val networkInfo by viewModel.networkInfo.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ScreenHeader(
                title = "Network Pulse",
                subtitle = "${networkInfo.connectionType} • ${networkInfo.interfaceName}",
                statusText = if (networkInfo.isConnected) "LINK ONLINE" else "DISCONNECTED",
                statusColor = if (networkInfo.isConnected) EmeraldPulse else CoralAlert,
                onRefresh = { viewModel.refreshAllMetrics() }
            )
        }

        // Live Ping Test Diagnostic Card
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
                            Icon(Icons.Default.Speed, contentDescription = null, tint = CyanPulse, modifier = Modifier.size(22.dp))
                            Text(
                                text = "DNS SOCKET PING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = CyanPulse
                            )
                        }

                        Button(
                            onClick = { viewModel.runPingLatencyTest() },
                            enabled = !networkInfo.isPinging,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPulse,
                                contentColor = DarkObsidian
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (networkInfo.isPinging) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = DarkObsidian
                                )
                            } else {
                                Text("Ping Test", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val pingMs = networkInfo.pingLatencyMs
                    val pingStatus = when {
                        pingMs == null -> "Not tested"
                        pingMs < 40 -> "Optimal (< 40ms)"
                        pingMs < 100 -> "Good (< 100ms)"
                        pingMs < 200 -> "Moderate"
                        else -> "High Latency"
                    }
                    val pingColor = when {
                        pingMs == null -> MaterialTheme.colorScheme.onSurfaceVariant
                        pingMs < 60 -> EmeraldPulse
                        pingMs < 120 -> CyanPulse
                        pingMs < 200 -> AmberWarning
                        else -> CoralAlert
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (pingMs != null) "$pingMs ms" else "-- ms",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = pingColor
                            )
                            Text(
                                text = "Target: Cloudflare DNS / Google DNS",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = pingColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, pingColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = pingStatus,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = pingColor
                            )
                        }
                    }
                }
            }
        }

        // Connection Parameters Card
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
                        text = "LINK CAPABILITIES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val downMbps = "%.1f".format(networkInfo.downstreamBandwidthKbps / 1000.0)
                    val upMbps = "%.1f".format(networkInfo.upstreamBandwidthKbps / 1000.0)

                    InfoRow("Connection Mode", networkInfo.connectionType)
                    InfoRow("Downlink Link Bandwidth", "$downMbps Mbps", isMonospace = true)
                    InfoRow("Uplink Link Bandwidth", "$upMbps Mbps", isMonospace = true)
                    InfoRow("Local IPv4 Address", networkInfo.ipv4Address, isMonospace = true)
                    InfoRow("Local IPv6 Address", networkInfo.ipv6Address, isMonospace = true)
                    InfoRow("Active Interface", networkInfo.interfaceName, isMonospace = true)
                }
            }
        }
    }
}

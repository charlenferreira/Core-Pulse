package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CorePulseViewModel
import com.example.ui.components.InfoRow
import com.example.ui.components.ScreenHeader
import com.example.ui.theme.CyanPulse
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.EmeraldPulse

@Composable
fun DeviceSpecsScreen(
    viewModel: CorePulseViewModel
) {
    val context = LocalContext.current
    val specs by viewModel.deviceSpecs.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ScreenHeader(
                title = "Specifications",
                subtitle = "${specs.manufacturer} ${specs.model} • Android ${specs.androidVersion}",
                statusText = "HARDWARE VERIFIED",
                onRefresh = { viewModel.refreshAllMetrics() }
            )
        }

        // Export Report Actions
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SYSTEM REPORT EXPORT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = CyanPulse
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generate a standardized Markdown hardware specification and diagnostic log.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val report = viewModel.getDiagnosticReport()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Core Pulse Diagnostic", report))
                                Toast.makeText(context, "Full system report copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanPulse,
                                contentColor = DarkObsidian
                            )
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Report", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val report = viewModel.getDiagnosticReport()
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, report)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Core Pulse Diagnostic Report")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, EmeraldPulse.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = EmeraldPulse, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Log", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Hardware Identity Card
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
                        text = "DEVICE IDENTIFICATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    InfoRow("Manufacturer", specs.manufacturer)
                    InfoRow("Model", specs.model, isMonospace = true)
                    InfoRow("Brand", specs.brand)
                    InfoRow("Product", specs.product, isMonospace = true)
                    InfoRow("Device Code", specs.device, isMonospace = true)
                    InfoRow("Board", specs.board, isMonospace = true)
                    InfoRow("Hardware Platform", specs.hardware, isMonospace = true)
                    InfoRow("Build ID", specs.buildId, isMonospace = true)
                }
            }
        }

        // Android OS & Kernel Card
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
                        text = "OPERATING SYSTEM & KERNEL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = EmeraldPulse
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    InfoRow("Android Version", "Android ${specs.androidVersion}")
                    InfoRow("API Level", "SDK ${specs.apiLevel}", isMonospace = true)
                    InfoRow("Security Patch", specs.securityPatch, isMonospace = true)
                    InfoRow("Kernel Version", specs.kernelVersion, isMonospace = true)
                    InfoRow("System Elapsed Uptime", specs.uptimeFormatted, isMonospace = true)
                }
            }
        }
    }
}

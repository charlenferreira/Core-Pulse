package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.*
import com.example.ui.theme.CyanPulse
import com.example.ui.theme.DarkObsidian

enum class AppDestination(val label: String, val icon: ImageVector) {
    DASHBOARD("Pulse", Icons.Default.Dashboard),
    CPU("CPU", Icons.Default.Memory),
    MEMORY("RAM & Disk", Icons.Default.Storage),
    BATTERY("Battery", Icons.Default.BatteryChargingFull),
    SENSORS("Sensors", Icons.Default.Sensors),
    NETWORK("Network", Icons.Default.Wifi),
    BENCHMARK("Bench", Icons.Default.Speed),
    SPECS("Specs", Icons.Default.Info)
}

@Composable
fun MainScreen(viewModel: CorePulseViewModel) {
    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }

    // Custom BackHandler: If on a sub-screen, go back to Dashboard
    if (currentDestination != AppDestination.DASHBOARD) {
        BackHandler {
            currentDestination = AppDestination.DASHBOARD
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp

        if (isWideScreen) {
            // Adaptive Navigation Rail for Tablets / Foldables
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    AppDestination.values().forEach { dest ->
                        NavigationRailItem(
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            selected = currentDestination == dest,
                            onClick = { currentDestination = dest },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = DarkObsidian,
                                indicatorColor = CyanPulse
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    ScreenContent(
                        destination = currentDestination,
                        viewModel = viewModel,
                        onNavigate = { currentDestination = it }
                    )
                }
            }
        } else {
            // Standard Handheld Phone Layout with Bottom NavigationBar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        // 5 primary navigation tabs
                        val primaryTabs = listOf(
                            AppDestination.DASHBOARD,
                            AppDestination.CPU,
                            AppDestination.MEMORY,
                            AppDestination.SENSORS,
                            AppDestination.BENCHMARK
                        )

                        primaryTabs.forEach { dest ->
                            val isSelected = currentDestination == dest
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.label,
                                        modifier = Modifier.testTag("nav_item_${dest.name.lowercase()}")
                                    )
                                },
                                label = { Text(dest.label) },
                                selected = isSelected,
                                onClick = { currentDestination = dest },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DarkObsidian,
                                    indicatorColor = CyanPulse,
                                    selectedTextColor = CyanPulse,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ScreenContent(
                        destination = currentDestination,
                        viewModel = viewModel,
                        onNavigate = { currentDestination = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    destination: AppDestination,
    viewModel: CorePulseViewModel,
    onNavigate: (AppDestination) -> Unit
) {
    when (destination) {
        AppDestination.DASHBOARD -> DashboardScreen(
            viewModel = viewModel,
            onNavigateToCpu = { onNavigate(AppDestination.CPU) },
            onNavigateToMemory = { onNavigate(AppDestination.MEMORY) },
            onNavigateToBattery = { onNavigate(AppDestination.BATTERY) },
            onNavigateToNetwork = { onNavigate(AppDestination.NETWORK) },
            onNavigateToBenchmark = { onNavigate(AppDestination.BENCHMARK) }
        )
        AppDestination.CPU -> CpuScreen(viewModel = viewModel)
        AppDestination.MEMORY -> MemoryStorageScreen(viewModel = viewModel)
        AppDestination.BATTERY -> BatteryScreen(viewModel = viewModel)
        AppDestination.SENSORS -> SensorsScreen(viewModel = viewModel)
        AppDestination.NETWORK -> NetworkScreen(viewModel = viewModel)
        AppDestination.BENCHMARK -> BenchmarkScreen(viewModel = viewModel)
        AppDestination.SPECS -> DeviceSpecsScreen(viewModel = viewModel)
    }
}

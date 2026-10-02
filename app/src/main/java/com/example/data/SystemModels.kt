package com.example.data

data class CpuInfo(
    val coreCount: Int,
    val architecture: String,
    val supportedAbis: List<String>,
    val hardwareName: String,
    val bogomips: String,
    val features: List<String>,
    val coreFrequenciesMhz: List<Int>,
    val overallUsagePercent: Float,
    val cpuGovernor: String
)

data class MemoryInfoData(
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val usedRamBytes: Long,
    val usedPercent: Float,
    val isLowMemory: Boolean,
    val thresholdBytes: Long,
    val jvmAllocatedBytes: Long,
    val jvmMaxBytes: Long,
    val jvmFreeBytes: Long
)

data class StorageInfo(
    val internalTotalBytes: Long,
    val internalFreeBytes: Long,
    val internalUsedBytes: Long,
    val internalUsedPercent: Float,
    val appCacheSizeBytes: Long,
    val externalTotalBytes: Long?,
    val externalFreeBytes: Long?
)

data class BatteryInfo(
    val percentage: Int,
    val status: String,
    val pluggedSource: String,
    val health: String,
    val voltageMv: Int,
    val temperatureCelsius: Float,
    val temperatureFahrenheit: Float,
    val technology: String,
    val isPowerSaveMode: Boolean,
    val isInteractive: Boolean
)

data class NetworkInfoData(
    val isConnected: Boolean,
    val connectionType: String,
    val downstreamBandwidthKbps: Int,
    val upstreamBandwidthKbps: Int,
    val ipv4Address: String,
    val ipv6Address: String,
    val interfaceName: String,
    val pingLatencyMs: Long?,
    val isPinging: Boolean = false
)

data class DisplayInfo(
    val widthPx: Int,
    val heightPx: Int,
    val refreshRateHz: Float,
    val densityDpi: Int,
    val densityScale: Float,
    val isHdrSupported: Boolean,
    val isWideColorGamut: Boolean
)

data class SensorItem(
    val name: String,
    val vendor: String,
    val type: Int,
    val typeName: String,
    val powerMa: Float,
    val resolution: Float,
    val maxRange: Float
)

data class DeviceSpecs(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val device: String,
    val product: String,
    val board: String,
    val hardware: String,
    val buildId: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val kernelVersion: String,
    val uptimeFormatted: String
)

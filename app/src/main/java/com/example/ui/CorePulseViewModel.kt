package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CorePulseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SystemMonitorRepository(application)
    private val database = AppDatabase.getDatabase(application)
    private val benchmarkRunner = BenchmarkRunner(database)

    // State flows
    private val _cpuInfo = MutableStateFlow(repository.getCpuInfo())
    val cpuInfo: StateFlow<CpuInfo> = _cpuInfo.asStateFlow()

    private val _cpuHistory = MutableStateFlow(listOf(15f, 18f, 22f, 19f, 25f, 30f, 20f, 16f, 24f, 28f))
    val cpuHistory: StateFlow<List<Float>> = _cpuHistory.asStateFlow()

    private val _memoryInfo = MutableStateFlow(repository.getMemoryInfo())
    val memoryInfo: StateFlow<MemoryInfoData> = _memoryInfo.asStateFlow()

    private val _storageInfo = MutableStateFlow(repository.getStorageInfo())
    val storageInfo: StateFlow<StorageInfo> = _storageInfo.asStateFlow()

    private val _batteryInfo = MutableStateFlow(repository.getBatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _networkInfo = MutableStateFlow(repository.getNetworkInfo())
    val networkInfo: StateFlow<NetworkInfoData> = _networkInfo.asStateFlow()

    private val _displayInfo = MutableStateFlow(repository.getDisplayInfo())
    val displayInfo: StateFlow<DisplayInfo> = _displayInfo.asStateFlow()

    private val _deviceSpecs = MutableStateFlow(repository.getDeviceSpecs())
    val deviceSpecs: StateFlow<DeviceSpecs> = _deviceSpecs.asStateFlow()

    private val _sensors = MutableStateFlow(repository.getSensors())
    val sensors: StateFlow<List<SensorItem>> = _sensors.asStateFlow()

    private val _isOptimizingRam = MutableStateFlow(false)
    val isOptimizingRam: StateFlow<Boolean> = _isOptimizingRam.asStateFlow()

    private val _ramFreedBytes = MutableStateFlow<Long?>(null)
    val ramFreedBytes: StateFlow<Long?> = _ramFreedBytes.asStateFlow()

    private val _isCleaningCache = MutableStateFlow(false)
    val isCleaningCache: StateFlow<Boolean> = _isCleaningCache.asStateFlow()

    private val _cacheCleanedBytes = MutableStateFlow<Long?>(null)
    val cacheCleanedBytes: StateFlow<Long?> = _cacheCleanedBytes.asStateFlow()

    private val _isBenchmarking = MutableStateFlow(false)
    val isBenchmarking: StateFlow<Boolean> = _isBenchmarking.asStateFlow()

    private val _benchmarkProgress = MutableStateFlow<BenchmarkProgress?>(null)
    val benchmarkProgress: StateFlow<BenchmarkProgress?> = _benchmarkProgress.asStateFlow()

    private val _latestBenchmark = MutableStateFlow<BenchmarkResult?>(null)
    val latestBenchmark: StateFlow<BenchmarkResult?> = _latestBenchmark.asStateFlow()

    private val _benchmarkHistory = MutableStateFlow<List<BenchmarkResult>>(emptyList())
    val benchmarkHistory: StateFlow<List<BenchmarkResult>> = _benchmarkHistory.asStateFlow()

    init {
        // Collect benchmark history from Room
        viewModelScope.launch {
            database.benchmarkDao().getAllResults().collect { list ->
                _benchmarkHistory.value = list
                if (_latestBenchmark.value == null && list.isNotEmpty()) {
                    _latestBenchmark.value = list.first()
                }
            }
        }

        // Real-time polling loop for dynamic system pulse
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(1200)
                val cpu = repository.getCpuInfo()
                val mem = repository.getMemoryInfo()
                val bat = repository.getBatteryInfo()
                val net = repository.getNetworkInfo()

                _cpuInfo.value = cpu
                _memoryInfo.value = mem
                _batteryInfo.value = bat
                _networkInfo.value = _networkInfo.value.copy(
                    isConnected = net.isConnected,
                    connectionType = net.connectionType,
                    downstreamBandwidthKbps = net.downstreamBandwidthKbps,
                    upstreamBandwidthKbps = net.upstreamBandwidthKbps,
                    ipv4Address = net.ipv4Address
                )

                // Update CPU history queue (limit to 30 data points)
                val currentHist = _cpuHistory.value.toMutableList()
                currentHist.add(cpu.overallUsagePercent)
                if (currentHist.size > 25) {
                    currentHist.removeAt(0)
                }
                _cpuHistory.value = currentHist
            }
        }
    }

    fun refreshAllMetrics() {
        viewModelScope.launch(Dispatchers.IO) {
            _cpuInfo.value = repository.getCpuInfo()
            _memoryInfo.value = repository.getMemoryInfo()
            _storageInfo.value = repository.getStorageInfo()
            _batteryInfo.value = repository.getBatteryInfo()
            _networkInfo.value = repository.getNetworkInfo()
            _displayInfo.value = repository.getDisplayInfo()
            _deviceSpecs.value = repository.getDeviceSpecs()
            _sensors.value = repository.getSensors()
        }
    }

    fun optimizeRam() {
        if (_isOptimizingRam.value) return
        viewModelScope.launch {
            _isOptimizingRam.value = true
            delay(600)
            val freed = repository.optimizeRam()
            _memoryInfo.value = repository.getMemoryInfo()
            _ramFreedBytes.value = freed
            _isOptimizingRam.value = false
        }
    }

    fun cleanCache() {
        if (_isCleaningCache.value) return
        viewModelScope.launch {
            _isCleaningCache.value = true
            delay(500)
            val cleaned = repository.cleanAppCache()
            _storageInfo.value = repository.getStorageInfo()
            _cacheCleanedBytes.value = cleaned
            _isCleaningCache.value = false
        }
    }

    fun runPingLatencyTest() {
        if (_networkInfo.value.isPinging) return
        viewModelScope.launch {
            _networkInfo.value = _networkInfo.value.copy(isPinging = true)
            val latency = repository.measurePingLatency()
            _networkInfo.value = _networkInfo.value.copy(
                pingLatencyMs = latency,
                isPinging = false
            )
        }
    }

    fun startBenchmark() {
        if (_isBenchmarking.value) return
        viewModelScope.launch {
            _isBenchmarking.value = true
            val specs = _deviceSpecs.value
            val result = benchmarkRunner.executeFullBenchmark(
                deviceModel = "${specs.manufacturer} ${specs.model}",
                androidVersion = "Android ${specs.androidVersion}",
                onProgress = { progress ->
                    _benchmarkProgress.value = progress
                }
            )
            _latestBenchmark.value = result
            _benchmarkProgress.value = null
            _isBenchmarking.value = false
        }
    }

    fun clearBenchmarkHistory() {
        viewModelScope.launch {
            database.benchmarkDao().clearHistory()
        }
    }

    fun getDiagnosticReport(): String {
        return repository.generateSystemMarkdownReport()
    }
}

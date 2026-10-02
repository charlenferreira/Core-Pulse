package com.example.data

import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import kotlin.random.Random

data class BenchmarkProgress(
    val stage: String,
    val progressPercent: Float,
    val currentMetric: String
)

class BenchmarkRunner(private val database: AppDatabase) {

    suspend fun executeFullBenchmark(
        deviceModel: String,
        androidVersion: String,
        onProgress: (BenchmarkProgress) -> Unit
    ): BenchmarkResult = withContext(Dispatchers.Default) {
        val startTime = SystemClock.elapsedRealtime()

        // Phase 1: Single Core Test (Primes & Hashing)
        onProgress(BenchmarkProgress("Single-Core Stress Test", 0.15f, "Testing single-thread integer & cryptographic throughput..."))
        val singleCoreDuration = measureSingleCorePerformance()
        // Lower duration = higher score
        val singleCoreScore = (50_000.0 / singleCoreDuration.coerceAtLeast(10L)).toInt().coerceIn(300, 5000)

        // Phase 2: Multi Core Parallel Test
        onProgress(BenchmarkProgress("Multi-Core Parallel Matrix", 0.50f, "Stressing all logical cores with parallel mathematical workloads..."))
        val coreCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val multiCoreDuration = measureMultiCorePerformance(coreCount)
        val multiCoreScore = (singleCoreScore * (coreCount * 0.82) * (1200.0 / multiCoreDuration.coerceAtLeast(10L))).toInt().coerceIn(600, 25000)

        // Phase 3: Memory Throughput
        onProgress(BenchmarkProgress("Memory Throughput (RAM)", 0.85f, "Benchmarking sequential read/write throughput..."))
        val memorySpeedMbPerSec = measureMemoryThroughput()

        onProgress(BenchmarkProgress("Finalizing Core Pulse Score", 1.0f, "Computing hardware index..."))

        val totalDuration = SystemClock.elapsedRealtime() - startTime

        val result = BenchmarkResult(
            timestamp = System.currentTimeMillis(),
            singleCoreScore = singleCoreScore,
            multiCoreScore = multiCoreScore,
            memoryThroughputMbPerSec = memorySpeedMbPerSec,
            deviceModel = deviceModel,
            androidVersion = androidVersion,
            testDurationMs = totalDuration
        )

        database.benchmarkDao().insertResult(result)
        result
    }

    private fun measureSingleCorePerformance(): Long {
        val start = SystemClock.elapsedRealtime()
        var count = 0
        // Compute primes up to 25,000
        for (i in 2..25000) {
            var isPrime = true
            var j = 2
            while (j * j <= i) {
                if (i % j == 0) {
                    isPrime = false
                    break
                }
                j++
            }
            if (isPrime) count++
        }

        // SHA-256 rounds
        val md = MessageDigest.getInstance("SHA-256")
        var data = "CorePulseBench_${count}".toByteArray()
        for (r in 0..1500) {
            data = md.digest(data)
        }

        val elapsed = SystemClock.elapsedRealtime() - start
        return elapsed.coerceAtLeast(1L)
    }

    private suspend fun measureMultiCorePerformance(cores: Int): Long = coroutineScope {
        val start = SystemClock.elapsedRealtime()
        val deferreds = (0 until cores).map { coreIndex ->
            async(Dispatchers.Default) {
                var total = 0.0
                for (step in 0..35000) {
                    val angle = Math.toRadians((step % 360).toDouble())
                    total += Math.sin(angle) * Math.cos(angle) + Math.sqrt((step + coreIndex).toDouble())
                }
                total
            }
        }
        deferreds.awaitAll()
        val elapsed = SystemClock.elapsedRealtime() - start
        elapsed.coerceAtLeast(1L)
    }

    private fun measureMemoryThroughput(): Double {
        val bufferSize = 8 * 1024 * 1024 // 8 MB buffer
        val iterations = 5
        val buffer = ByteArray(bufferSize)
        Random.nextBytes(buffer)

        val start = SystemClock.elapsedRealtime()
        for (i in 0 until iterations) {
            // Write
            for (j in 0 until bufferSize step 64) {
                buffer[j] = (buffer[j] + 1).toByte()
            }
            // Read
            var sum = 0
            for (j in 0 until bufferSize step 64) {
                sum += buffer[j]
            }
        }
        val elapsed = SystemClock.elapsedRealtime() - start
        val totalMb = (bufferSize.toDouble() * iterations * 2) / (1024.0 * 1024.0)
        val seconds = elapsed / 1000.0
        return if (seconds > 0) (totalMb / seconds).coerceIn(100.0, 15000.0) else 1500.0
    }
}

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "benchmark_results")
data class BenchmarkResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val singleCoreScore: Int,
    val multiCoreScore: Int,
    val memoryThroughputMbPerSec: Double,
    val deviceModel: String,
    val androidVersion: String,
    val testDurationMs: Long
)

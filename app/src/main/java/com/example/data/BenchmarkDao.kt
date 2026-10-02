package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BenchmarkDao {
    @Query("SELECT * FROM benchmark_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<BenchmarkResult>>

    @Query("SELECT * FROM benchmark_results ORDER BY multiCoreScore DESC LIMIT 1")
    suspend fun getBestResult(): BenchmarkResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: BenchmarkResult): Long

    @Query("DELETE FROM benchmark_results")
    suspend fun clearHistory()
}

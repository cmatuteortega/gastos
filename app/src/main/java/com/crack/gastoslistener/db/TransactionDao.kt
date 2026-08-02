package com.crack.gastoslistener.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun getBetween(from: Date, to: Date): Flow<List<Transaction>>

    @Query("SELECT source, SUM(amountCents) as total FROM transactions WHERE timestamp BETWEEN :from AND :to GROUP BY source")
    fun getTotalsBySource(from: Date, to: Date): Flow<List<SourceTotal>>
}

data class SourceTotal(val source: String, val total: Long)

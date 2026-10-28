package com.focusguardian.app.data.local.dao

import androidx.room.*
import com.focusguardian.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DomainDao {
    @Query("SELECT * FROM blocked_domains ORDER BY createdAt DESC")
    fun getAllDomains(): Flow<List<BlockedDomainEntity>>

    @Query("SELECT * FROM blocked_domains WHERE isEnabled = 1")
    suspend fun getActiveDomains(): List<BlockedDomainEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDomain(domain: BlockedDomainEntity): Long

    @Update
    suspend fun updateDomain(domain: BlockedDomainEntity)

    @Delete
    suspend fun deleteDomain(domain: BlockedDomainEntity)
}

@Dao
interface WordDao {
    @Query("SELECT * FROM blocked_words ORDER BY createdAt DESC")
    fun getAllWords(): Flow<List<BlockedWordEntity>>

    @Query("SELECT * FROM blocked_words WHERE isEnabled = 1")
    suspend fun getActiveWords(): List<BlockedWordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: BlockedWordEntity): Long

    @Update
    suspend fun updateWord(word: BlockedWordEntity)

    @Delete
    suspend fun deleteWord(word: BlockedWordEntity)
}

@Dao
interface ImageDao {
    @Query("SELECT * FROM blocked_images ORDER BY createdAt DESC")
    fun getAllImages(): Flow<List<BlockedImageEntity>>

    @Query("SELECT * FROM blocked_images WHERE isEnabled = 1")
    suspend fun getActiveImages(): List<BlockedImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: BlockedImageEntity): Long

    @Delete
    suspend fun deleteImage(image: BlockedImageEntity)
}

@Dao
interface LogDao {
    @Query("SELECT * FROM block_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<BlockLogEntity>>

    @Query("SELECT COUNT(*) FROM block_logs WHERE timestamp >= :sinceTimestamp")
    suspend fun countSince(sinceTimestamp: Long): Int

    @Insert
    suspend fun insertLog(log: BlockLogEntity)
}

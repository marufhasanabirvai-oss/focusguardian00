package com.focusguardian.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_images")
data class BlockedImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dHashHex: String, // 64-bit difference hash representation (16 hex chars)
    val similarityThreshold: Int = 85, // 50 - 99% similarity required to trigger penalty
    val isEnabled: Boolean = true,
    val lockUntilTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

package com.focusguardian.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_domains")
data class BlockedDomainEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val domain: String,
    val isEnabled: Boolean = true,
    val category: String = "custom", // "custom", "adult", "social"
    val daysOfWeekMask: Int = 127, // Bitmask for Mon-Sun (1111111 = all days)
    val startHour: Int = 0,
    val startMinute: Int = 0,
    val endHour: Int = 23,
    val endMinute: Int = 59,
    val lockUntilTimestamp: Long = 0L, // 0 = not locked, >0 = locked until epoch ms
    val createdAt: Long = System.currentTimeMillis()
)

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Check-in record for a specific date (Format "yyyy-MM-DD")
 * status: 1 = Checked In (打卡了 - Green), 0 = Missed / Failed (没打卡 - Red)
 */
@Entity(tableName = "check_ins")
data class CheckInRecord(
    @PrimaryKey val date: String,
    val status: Int = 1,
    val note: String = "",
    val pointsEarned: Int = 10,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Reward item that can be redeemed with points
 */
@Entity(tableName = "rewards")
data class Reward(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val pointsCost: Int,
    val description: String = "",
    val icon: String = "🎁",
    val isDefault: Boolean = false
)

/**
 * Log entry for a redeemed reward
 */
@Entity(tableName = "redemption_logs")
data class RedemptionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rewardTitle: String,
    val pointsDeducted: Int,
    val timestamp: Long = System.currentTimeMillis()
)

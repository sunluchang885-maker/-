package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins ORDER BY date DESC")
    fun getAllCheckIns(): Flow<List<CheckInRecord>>

    @Query("SELECT * FROM check_ins WHERE date = :date")
    suspend fun getCheckInByDate(date: String): CheckInRecord?

    @Query("SELECT * FROM check_ins WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getCheckInsBetween(startDate: String, endDate: String): Flow<List<CheckInRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckIn(checkIn: CheckInRecord)

    @Query("DELETE FROM check_ins WHERE date = :date")
    suspend fun deleteCheckIn(date: String)
}

@Dao
interface RewardDao {
    @Query("SELECT * FROM rewards ORDER BY isDefault DESC, pointsCost ASC")
    fun getAllRewards(): Flow<List<Reward>>

    @Query("SELECT COUNT(*) FROM rewards")
    suspend fun getRewardCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: Reward): Long

    @Update
    suspend fun updateReward(reward: Reward)

    @Delete
    suspend fun deleteReward(reward: Reward)
}

@Dao
interface RedemptionDao {
    @Query("SELECT * FROM redemption_logs ORDER BY timestamp DESC")
    fun getAllRedemptions(): Flow<List<RedemptionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRedemption(log: RedemptionLog)
}

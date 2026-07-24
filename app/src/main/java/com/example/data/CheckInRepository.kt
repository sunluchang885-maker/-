package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CheckInRepository(private val db: AppDatabase) {
    val checkInDao = db.checkInDao()
    val rewardDao = db.rewardDao()
    val redemptionDao = db.redemptionDao()

    val allCheckIns: Flow<List<CheckInRecord>> = checkInDao.getAllCheckIns()
    val allRewards: Flow<List<Reward>> = rewardDao.getAllRewards()
    val allRedemptions: Flow<List<RedemptionLog>> = redemptionDao.getAllRedemptions()

    /**
     * Total Points = Sum(Earned points: +10 for check-in, -15 for missed) - Sum(Redeemed points)
     */
    val totalPoints: Flow<Int> = combine(allCheckIns, allRedemptions) { checkIns, redemptions ->
        val totalNet = checkIns.sumOf { 
            when (it.status) {
                1 -> 10
                0 -> -15
                else -> 0
            }
        }
        val totalSpent = redemptions.sumOf { it.pointsDeducted }
        (totalNet - totalSpent).coerceAtLeast(0)
    }

    suspend fun initializeDefaultDataIfNeeded() {
        // Ensure default rewards list matches requirement: "奖励" (10分) and "享用美味大餐" (50分)
        if (rewardDao.getRewardCount() == 0) {
            rewardDao.insertReward(
                Reward(
                    title = "奖励",
                    pointsCost = 10,
                    description = "基础奖励，只需打卡1天（10分）即可兑换",
                    icon = "🎉",
                    isDefault = true
                )
            )
            rewardDao.insertReward(
                Reward(
                    title = "享用美味大餐",
                    pointsCost = 50,
                    description = "犒劳自己，积累50积分即可兑换",
                    icon = "🍕",
                    isDefault = false
                )
            )
        }
    }

    suspend fun seedSampleCheckInsIfEmpty() {
        // Check if database already has check-ins
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        for (i in 1..10) {
            val pastDate = today.minusDays(i.toLong()).format(formatter)
            if (checkInDao.getCheckInByDate(pastDate) == null) {
                val isSuccess = i % 4 != 0
                val status = if (isSuccess) 1 else 0
                checkInDao.upsertCheckIn(
                    CheckInRecord(
                        date = pastDate,
                        status = status,
                        note = if (status == 1) "坚持打卡第${11 - i}天！" else "未完成打卡",
                        pointsEarned = if (status == 1) 10 else -15
                    )
                )
            }
        }
    }

    suspend fun toggleCheckIn(dateStr: String, status: Int, note: String = "") {
        val points = if (status == 1) 10 else -15
        checkInDao.upsertCheckIn(
            CheckInRecord(
                date = dateStr,
                status = status,
                note = note,
                pointsEarned = points,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun redeemReward(reward: Reward, currentPoints: Int): Result<Unit> {
        if (currentPoints < reward.pointsCost) {
            return Result.failure(Exception("积分不足，无法兑换！"))
        }
        redemptionDao.insertRedemption(
            RedemptionLog(
                rewardTitle = reward.title,
                pointsDeducted = reward.pointsCost,
                timestamp = System.currentTimeMillis()
            )
        )
        return Result.success(Unit)
    }

    suspend fun addReward(title: String, pointsCost: Int, description: String, icon: String) {
        rewardDao.insertReward(
            Reward(
                title = title,
                pointsCost = pointsCost,
                description = description,
                icon = icon.ifBlank { "🎁" }
            )
        )
    }

    suspend fun deleteReward(reward: Reward) {
        rewardDao.deleteReward(reward)
    }

    suspend fun deleteCheckIn(dateStr: String) {
        checkInDao.deleteCheckIn(dateStr)
    }
}

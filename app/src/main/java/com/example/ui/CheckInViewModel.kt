package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CheckInRecord
import com.example.data.CheckInRepository
import com.example.data.RedemptionLog
import com.example.data.Reward
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

enum class ChartPeriod {
    WEEKLY, MONTHLY
}

data class DayChartItem(
    val label: String,
    val dateStr: String,
    val status: Int, // 1 = Checked In (Green), 0 = Missed (Red), -1 = Unrecorded
    val isToday: Boolean = false
)

data class UiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val chartPeriod: ChartPeriod = ChartPeriod.WEEKLY,
    val fontScaleFactor: Float = 1.1f,
    val totalPoints: Int = 0,
    val checkInMap: Map<String, CheckInRecord> = emptyMap(),
    val rewards: List<Reward> = emptyList(),
    val redemptionLogs: List<RedemptionLog> = emptyList(),
    val message: String? = null
)

class CheckInViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CheckInRepository
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    private val _chartPeriod = MutableStateFlow(ChartPeriod.WEEKLY)
    private val _fontScaleFactor = MutableStateFlow(1.1f)
    private val _userMessage = MutableStateFlow<String?>(null)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CheckInRepository(db)
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
            repository.seedSampleCheckInsIfEmpty()
        }
    }

    val uiState: StateFlow<UiState> = combine(
        _selectedDate,
        _currentMonth,
        _themeMode,
        _chartPeriod,
        _fontScaleFactor,
        repository.totalPoints,
        repository.allCheckIns,
        repository.allRewards,
        repository.allRedemptions,
        _userMessage
    ) { args ->
        val selectedDate = args[0] as LocalDate
        val currentMonth = args[1] as YearMonth
        val themeMode = args[2] as ThemeMode
        val chartPeriod = args[3] as ChartPeriod
        val fontScaleFactor = args[4] as Float
        val totalPoints = args[5] as Int
        @Suppress("UNCHECKED_CAST")
        val checkInsList = args[6] as List<CheckInRecord>
        @Suppress("UNCHECKED_CAST")
        val rewardsList = args[7] as List<Reward>
        @Suppress("UNCHECKED_CAST")
        val redemptionLogs = args[8] as List<RedemptionLog>
        val msg = args[9] as String?

        val map = checkInsList.associateBy { it.date }

        UiState(
            selectedDate = selectedDate,
            currentMonth = currentMonth,
            themeMode = themeMode,
            chartPeriod = chartPeriod,
            fontScaleFactor = fontScaleFactor,
            totalPoints = totalPoints,
            checkInMap = map,
            rewards = rewardsList,
            redemptionLogs = redemptionLogs,
            message = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun prevMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun goToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _currentMonth.value = YearMonth.from(today)
    }

    fun toggleCheckIn(date: LocalDate, status: Int, note: String = "") {
        val dateStr = date.format(dateFormatter)
        viewModelScope.launch {
            repository.toggleCheckIn(dateStr, status, note)
            val actionText = if (status == 1) "打卡成功！+10积分" else "标记为未打卡，扣除15积分"
            _userMessage.value = "${date.format(DateTimeFormatter.ofPattern("M月d日"))} $actionText"
        }
    }

    fun deleteCheckIn(date: LocalDate) {
        val dateStr = date.format(dateFormatter)
        viewModelScope.launch {
            repository.deleteCheckIn(dateStr)
            _userMessage.value = "${date.format(DateTimeFormatter.ofPattern("M月d日"))} 已清除记录，恢复初始状态"
        }
    }

    fun redeemReward(reward: Reward) {
        viewModelScope.launch {
            val result = repository.redeemReward(reward, uiState.value.totalPoints)
            if (result.isSuccess) {
                _userMessage.value = "🎉 成功兑换「${reward.title}」！已扣除 ${reward.pointsCost} 积分"
            } else {
                _userMessage.value = result.exceptionOrNull()?.message ?: "兑换失败"
            }
        }
    }

    fun addCustomReward(title: String, pointsCost: Int, description: String, icon: String) {
        viewModelScope.launch {
            repository.addReward(title, pointsCost, description, icon)
            _userMessage.value = "已添加新奖励「$title」"
        }
    }

    fun deleteReward(reward: Reward) {
        viewModelScope.launch {
            repository.deleteReward(reward)
            _userMessage.value = "已删除奖励「${reward.title}」"
        }
    }

    fun setChartPeriod(period: ChartPeriod) {
        _chartPeriod.value = period
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setFontScaleFactor(factor: Float) {
        _fontScaleFactor.value = factor.coerceIn(0.8f, 1.5f)
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // Calculation helper for Weekly Data (Mon - Sun)
    fun getWeeklyChartData(): List<DayChartItem> {
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val map = uiState.value.checkInMap
        val dayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

        return (0..6).map { i ->
            val date = monday.plusDays(i.toLong())
            val dateStr = date.format(dateFormatter)
            val record = map[dateStr]
            val status = record?.status ?: -1
            DayChartItem(
                label = dayNames[i],
                dateStr = dateStr,
                status = status,
                isToday = date == today
            )
        }
    }

    // Calculation helper for Monthly Data (current month days)
    fun getMonthlyChartData(): List<DayChartItem> {
        val yearMonth = uiState.value.currentMonth
        val daysInMonth = yearMonth.lengthOfMonth()
        val today = LocalDate.now()
        val map = uiState.value.checkInMap

        return (1..daysInMonth).map { day ->
            val date = yearMonth.atDay(day)
            val dateStr = date.format(dateFormatter)
            val record = map[dateStr]
            val status = record?.status ?: -1
            DayChartItem(
                label = "${day}日",
                dateStr = dateStr,
                status = status,
                isToday = date == today
            )
        }
    }

    // Calculate current streak
    fun calculateStreak(): Int {
        var streak = 0
        var checkDate = LocalDate.now()
        val map = uiState.value.checkInMap

        // If today is not checked in, check from yesterday
        if (map[checkDate.format(dateFormatter)]?.status != 1) {
            checkDate = checkDate.minusDays(1)
        }

        while (true) {
            val dateStr = checkDate.format(dateFormatter)
            if (map[dateStr]?.status == 1) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }
}

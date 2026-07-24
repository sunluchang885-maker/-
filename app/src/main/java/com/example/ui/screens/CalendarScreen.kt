package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CheckInViewModel
import com.example.ui.UiState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    viewModel: CheckInViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val selectedDateStr = uiState.selectedDate.format(dateFormatter)
    val selectedRecord = uiState.checkInMap[selectedDateStr]
    val selectedStatus = selectedRecord?.status ?: -1
    
    var noteText by remember(selectedDateStr) { mutableStateOf(selectedRecord?.note ?: "") }
    var isEditingNote by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector Header
        MonthHeaderCard(
            currentMonth = uiState.currentMonth,
            onPrevMonth = { viewModel.prevMonth() },
            onNextMonth = { viewModel.nextMonth() },
            onGoToday = { viewModel.goToToday() }
        )

        // Calendar Grid
        CalendarGrid(
            currentMonth = uiState.currentMonth,
            selectedDate = uiState.selectedDate,
            checkInMap = uiState.checkInMap,
            dateFormatter = dateFormatter,
            onSelectDate = { viewModel.selectDate(it) }
        )

        // Legend bar
        CalendarLegendRow()

        // Selected Date Details & Check-In Action Card
        SelectedDateActionCard(
            selectedDate = uiState.selectedDate,
            status = selectedStatus,
            noteText = noteText,
            isEditingNote = isEditingNote,
            onNoteTextChange = { noteText = it },
            onToggleNoteEditing = { isEditingNote = !isEditingNote },
            onCheckInSuccess = {
                viewModel.toggleCheckIn(uiState.selectedDate, 1, noteText)
            },
            onMarkFailed = {
                viewModel.toggleCheckIn(uiState.selectedDate, 0, noteText)
            }
        )

        // Monthly Stats Quick Summary
        MonthlySummaryCard(
            currentMonth = uiState.currentMonth,
            checkInMap = uiState.checkInMap,
            dateFormatter = dateFormatter
        )
    }
}

@Composable
fun MonthHeaderCard(
    currentMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onGoToday: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevMonth,
                modifier = Modifier.testTag("btn_prev_month")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "上一个月"
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${currentMonth.year}年 ${currentMonth.monthValue}月",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable { onGoToday() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Today,
                            contentDescription = "回到今天",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "今天",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.testTag("btn_next_month")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "下一个月"
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    checkInMap: Map<String, com.example.data.CheckInRecord>,
    dateFormatter: DateTimeFormatter,
    onSelectDate: (LocalDate) -> Unit
) {
    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
    val today = LocalDate.now()

    // Calculate grid items including padding before day 1
    val firstDayOfMonth = currentMonth.atDay(1)
    val dayOfWeekValue = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday, 1 = Mon ...
    val daysInMonth = currentMonth.lengthOfMonth()

    val gridItems = remember(currentMonth) {
        val list = mutableListOf<LocalDate?>()
        // Add empty slots for days before 1st
        for (i in 0 until dayOfWeekValue) {
            list.add(null)
        }
        // Add actual days
        for (day in 1..daysInMonth) {
            list.add(currentMonth.atDay(day))
        }
        list
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Day Name Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDays.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Days Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                userScrollEnabled = false
            ) {
                items(gridItems) { date ->
                    if (date != null) {
                        val dateStr = date.format(dateFormatter)
                        val record = checkInMap[dateStr]
                        val status = record?.status ?: -1
                        val isSelected = date == selectedDate
                        val isToday = date == today

                        CalendarDayTile(
                            date = date,
                            status = status,
                            isSelected = isSelected,
                            isToday = isToday,
                            onSelect = { onSelectDate(date) }
                        )
                    } else {
                        Spacer(modifier = Modifier.aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayTile(
    date: LocalDate,
    status: Int, // 1 = Green Check-in, 0 = Red Missed, -1 = Unrecorded
    isSelected: Boolean,
    isToday: Boolean,
    onSelect: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

    // Color definitions for check-in (Green) and missed (Red)
    val checkedInColor = if (isDark) Color(0xFF4CAF50) else Color(0xFF2E7D32)
    val missedColor = if (isDark) Color(0xFFFF5252) else Color(0xFFD32F2F)

    val bgColor by animateColorAsState(
        targetValue = when (status) {
            1 -> checkedInColor
            0 -> missedColor
            else -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "tile_bg"
    )

    val contentColor = when (status) {
        1, 0 -> Color.White
        else -> if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = if (isSelected) 2.5.dp else if (isToday) 1.5.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else if (isToday) MaterialTheme.colorScheme.secondary else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onSelect() }
            .testTag("day_tile_${date.dayOfMonth}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${date.dayOfMonth}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected || status != -1) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp
                ),
                color = contentColor
            )

            // Status Icon or dot indicator
            when (status) {
                1 -> Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "已打卡",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                0 -> Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "未打卡",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                else -> {
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarLegendRow() {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val greenColor = if (isDark) Color(0xFF4CAF50) else Color(0xFF2E7D32)
    val redColor = if (isDark) Color(0xFFFF5252) else Color(0xFFD32F2F)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(greenColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "绿色：打卡成功 (+10分)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(redColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "红色：未打卡 (-15分)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SelectedDateActionCard(
    selectedDate: LocalDate,
    status: Int, // 1 = Checked In, 0 = Missed, -1 = Unrecorded
    noteText: String,
    isEditingNote: Boolean,
    onNoteTextChange: (String) -> Unit,
    onToggleNoteEditing: () -> Unit,
    onCheckInSuccess: () -> Unit,
    onMarkFailed: () -> Unit
) {
    val displayDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE"))
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val greenColor = if (isDark) Color(0xFF4CAF50) else Color(0xFF2E7D32)
    val redColor = if (isDark) Color(0xFFFF5252) else Color(0xFFD32F2F)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                1 -> greenColor.copy(alpha = if (isDark) 0.15f else 0.08f)
                0 -> redColor.copy(alpha = if (isDark) 0.15f else 0.08f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = when (status) {
                            1 -> "状态：打卡成功 (获 10 积分)"
                            0 -> "状态：未打卡 (扣 15 积分)"
                            else -> "状态：尚未打卡"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (status) {
                            1 -> greenColor
                            0 -> redColor
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                IconButton(onClick = onToggleNoteEditing) {
                    Icon(
                        imageVector = Icons.Filled.EditNote,
                        contentDescription = "备注",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Note input or view
            AnimatedVisibility(visible = isEditingNote || noteText.isNotBlank()) {
                if (isEditingNote) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = onNoteTextChange,
                        label = { Text("打卡心得 / 备注") },
                        placeholder = { Text("例如：完成了30分钟晨跑、读书1章") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else if (noteText.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = "📝 $noteText",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCheckInSuccess,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_check_in_success"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = greenColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (status == 1) "已打卡 (+10分)" else "打卡成功 (+10分)")
                }

                OutlinedButton(
                    onClick = onMarkFailed,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_mark_failed"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = redColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (status == 0) "已记未打卡 (-15分)" else "设为未打卡 (-15分)")
                }
            }
        }
    }
}

@Composable
fun MonthlySummaryCard(
    currentMonth: YearMonth,
    checkInMap: Map<String, com.example.data.CheckInRecord>,
    dateFormatter: DateTimeFormatter
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val greenColor = if (isDark) Color(0xFF4CAF50) else Color(0xFF2E7D32)
    val redColor = if (isDark) Color(0xFFFF5252) else Color(0xFFD32F2F)

    val daysInMonth = currentMonth.lengthOfMonth()
    var checkedInDays = 0
    var missedDays = 0

    for (day in 1..daysInMonth) {
        val dateStr = currentMonth.atDay(day).format(dateFormatter)
        when (checkInMap[dateStr]?.status) {
            1 -> checkedInDays++
            0 -> missedDays++
        }
    }

    val totalRecorded = (checkedInDays + missedDays).coerceAtLeast(1)
    val rate = (checkedInDays * 100 / totalRecorded)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$checkedInDays 天",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = greenColor
                )
                Text(
                    text = "绿色打卡成功",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$missedDays 天",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = redColor
                )
                Text(
                    text = "红色未打卡",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$rate%",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "本月打卡率",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

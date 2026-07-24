package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChartPeriod
import com.example.ui.CheckInViewModel
import com.example.ui.DayChartItem
import com.example.ui.UiState

@Composable
fun StatsScreen(
    viewModel: CheckInViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val streak = viewModel.calculateStreak()
    val totalCheckedInDays = uiState.checkInMap.values.count { it.status == 1 }
    val totalMissedDays = uiState.checkInMap.values.count { it.status == 0 }
    val totalRecorded = (totalCheckedInDays + totalMissedDays).coerceAtLeast(1)
    val completionRate = (totalCheckedInDays * 100 / totalRecorded)

    val chartData = if (uiState.chartPeriod == ChartPeriod.WEEKLY) {
        viewModel.getWeeklyChartData()
    } else {
        viewModel.getMonthlyChartData()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector: 按周 / 按月
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = uiState.chartPeriod == ChartPeriod.WEEKLY,
                onClick = { viewModel.setChartPeriod(ChartPeriod.WEEKLY) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                modifier = Modifier.testTag("btn_chart_weekly")
            ) {
                Text("📅 按周查看统计")
            }

            SegmentedButton(
                selected = uiState.chartPeriod == ChartPeriod.MONTHLY,
                onClick = { viewModel.setChartPeriod(ChartPeriod.MONTHLY) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                modifier = Modifier.testTag("btn_chart_monthly")
            ) {
                Text("🗓️ 按月查看统计")
            }
        }

        // Overview Key Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "连续打卡",
                value = "$streak 天",
                icon = Icons.Filled.LocalFireDepartment,
                iconTint = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "打卡完成率",
                value = "$completionRate%",
                icon = Icons.Filled.CheckCircle,
                iconTint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "累计打卡",
                value = "$totalCheckedInDays 天",
                icon = Icons.Filled.DateRange,
                iconTint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "总包含未打卡",
                value = "$totalMissedDays 天",
                icon = Icons.Filled.BarChart,
                iconTint = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }

        // Chart Title Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.chartPeriod == ChartPeriod.WEEKLY) "本周打卡柱状图" else "${uiState.currentMonth.monthValue}月打卡记录图",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    // Color Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("已打卡", style = MaterialTheme.typography.labelSmall)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("未打卡", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Render Chart
                if (uiState.chartPeriod == ChartPeriod.WEEKLY) {
                    WeeklyBarChartCanvas(items = chartData)
                } else {
                    MonthlyBarChartScrollable(items = chartData)
                }
            }
        }

        // Summary Insights
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "💡 习惯养成小贴士",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "• 保持绿色柱状连续延伸，不打断 streak（连续天数），能获得更多打卡成就感！\n• 每天打卡获得 10 积分，可在兑换页换取「看电影」、「大餐」等心仪奖励，形成正向反馈！",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun WeeklyBarChartCanvas(items: List<DayChartItem>) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val greenColor = if (isDark) Color(0xFF4CAF50) else Color(0xFF2E7D32)
    val redColor = if (isDark) Color(0xFFFF5252) else Color(0xFFD32F2F)
    val unrecordedColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        items.forEach { item ->
            val barColor = when (item.status) {
                1 -> greenColor
                0 -> redColor
                else -> unrecordedColor
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // Score indicator above bar
                Text(
                    text = when (item.status) {
                        1 -> "+10"
                        0 -> "未打卡"
                        else -> "-"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (item.status == 1) greenColor else if (item.status == 0) redColor else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Bar Canvas
                Canvas(
                    modifier = Modifier
                        .width(28.dp)
                        .height(if (item.status == 1) 110.dp else if (item.status == 0) 60.dp else 25.dp)
                ) {
                    drawRoundRect(
                        color = barColor,
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Day Label (e.g., 周一)
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (item.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun MonthlyBarChartScrollable(items: List<DayChartItem>) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val greenColor = if (isDark) Color(0xFF4CAF50) else Color(0xFF2E7D32)
    val redColor = if (isDark) Color(0xFFFF5252) else Color(0xFFD32F2F)
    val unrecordedColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        items(items) { item ->
            val barColor = when (item.status) {
                1 -> greenColor
                0 -> redColor
                else -> unrecordedColor
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.width(32.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(20.dp)
                        .height(if (item.status == 1) 110.dp else if (item.status == 0) 50.dp else 18.dp)
                ) {
                    drawRoundRect(
                        color = barColor,
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = if (item.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

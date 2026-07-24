package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RedemptionLog
import com.example.data.Reward
import com.example.ui.CheckInViewModel
import com.example.ui.UiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RewardsScreen(
    viewModel: CheckInViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var rewardToRedeem by remember { mutableStateOf<Reward?>(null) }
    var rewardToDelete by remember { mutableStateOf<Reward?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Points Header Card
            item {
                TotalPointsHeaderCard(totalPoints = uiState.totalPoints)
            }

            // Section Title: Rewards List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎁 兑换列表",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                    
                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("新增奖励")
                    }
                }
            }

            // Reward Items
            items(uiState.rewards, key = { it.id }) { reward ->
                RewardItemCard(
                    reward = reward,
                    userPoints = uiState.totalPoints,
                    onRedeemClick = { rewardToRedeem = reward },
                    onDeleteClick = { rewardToDelete = reward }
                )
            }

            // Redemption History Section Title
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "📜 兑换历史记录",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Redemption Logs
            if (uiState.redemptionLogs.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "暂无兑换记录，打卡积攒积分即可兑换心仪的奖励吧！",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.redemptionLogs, key = { it.id }) { log ->
                    RedemptionLogItem(log = log)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(60.dp)) // Padding for FAB
            }
        }

        // Floating Action Button to add reward
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_reward"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "添加奖励"
            )
        }
    }

    // Confirm Redemption Dialog
    rewardToRedeem?.let { reward ->
        AlertDialog(
            onDismissRequest = { rewardToRedeem = null },
            title = { Text("确认兑换「${reward.title}」？") },
            text = {
                Text("将扣除 ${reward.pointsCost} 积分。\n当前积分：${uiState.totalPoints} 分\n兑换后剩余：${uiState.totalPoints - reward.pointsCost} 分")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.redeemReward(reward)
                        rewardToRedeem = null
                    },
                    modifier = Modifier.testTag("btn_confirm_redeem")
                ) {
                    Text("确认兑换")
                }
            },
            dismissButton = {
                TextButton(onClick = { rewardToRedeem = null }) {
                    Text("取消")
                }
            }
        )
    }

    // Delete Reward Dialog
    rewardToDelete?.let { reward ->
        AlertDialog(
            onDismissRequest = { rewardToDelete = null },
            title = { Text("删除奖励") },
            text = { Text("确定要删除「${reward.title}」吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReward(reward)
                        rewardToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { rewardToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    // Add Custom Reward Dialog
    if (showAddDialog) {
        AddRewardDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, cost, desc, icon ->
                viewModel.addCustomReward(title, cost, desc, icon)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TotalPointsHeaderCard(totalPoints: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "我的可用总积分",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$totalPoints",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 38.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "积分",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💡 成功打卡+10积分（未打卡扣15分），用积分兑换专属奖励！",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Stars,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun RewardItemCard(
    reward: Reward,
    userPoints: Int,
    onRedeemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val canRedeem = userPoints >= reward.pointsCost

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reward_card_${reward.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = reward.icon,
                            fontSize = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = reward.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (reward.isDefault) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "默认奖励",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Text(
                        text = "价值 ${reward.pointsCost} 积分",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (reward.description.isNotBlank()) {
                        Text(
                            text = reward.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onRedeemClick,
                    enabled = canRedeem,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_redeem_${reward.id}")
                ) {
                    Text(text = if (canRedeem) "兑换" else "积分不足")
                }

                if (!reward.isDefault) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RedemptionLogItem(log: RedemptionLog) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())
    }
    val dateStr = formatter.format(Instant.ofEpochMilli(log.timestamp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "兑换「${log.rewardTitle}」",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "-${log.pointsDeducted} 积分",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun AddRewardDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, cost: Int, description: String, icon: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("10") }
    var description by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("🎁") }

    val iconsList = listOf("🎁", "🎮", "🎬", "🍕", "☕", "📚", "✈️", "🎵")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加自定义奖励") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("奖励名称") },
                    placeholder = { Text("如：看电影、听音乐等") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it.filter { char -> char.isDigit() } },
                    label = { Text("所需积分 (价值)") },
                    placeholder = { Text("如：10") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述 (选填)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "选择图标：",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    iconsList.forEach { item ->
                        Surface(
                            shape = CircleShape,
                            color = if (icon == item) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (icon == item) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                TextButton(onClick = { icon = item }) {
                                    Text(item, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = costText.toIntOrNull() ?: 10
                    if (title.isNotBlank()) {
                        onAdd(title, cost, description, icon)
                    }
                },
                enabled = title.isNotBlank() && costText.isNotBlank(),
                modifier = Modifier.testTag("btn_save_reward")
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

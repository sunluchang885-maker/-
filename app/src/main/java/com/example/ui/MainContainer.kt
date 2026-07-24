package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.ThemeMode

enum class NavTab(val title: String, val icon: ImageVector) {
    CALENDAR("日历", Icons.Filled.CalendarMonth),
    STATS("统计", Icons.Filled.BarChart),
    REWARDS("兑换", Icons.Filled.CardGiftcard),
    SETTINGS("设置", Icons.Filled.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    viewModel: CheckInViewModel,
    uiState: UiState
) {
    var currentTab by remember { mutableStateOf(NavTab.CALENDAR) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle ViewModel toast/snackbar user messages
    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "日历打卡",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    // Top Right Score Badge (Clicking opens Rewards / Redeem screen!)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { currentTab = NavTab.REWARDS }
                            .testTag("top_points_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stars,
                                contentDescription = "总积分",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${uiState.totalPoints} 分",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Quick Dark/Light Theme Switch Icon
                    IconButton(
                        onClick = {
                            val nextMode = when (uiState.themeMode) {
                                ThemeMode.DARK -> ThemeMode.LIGHT
                                ThemeMode.LIGHT -> ThemeMode.DARK
                                ThemeMode.SYSTEM -> ThemeMode.DARK
                            }
                            viewModel.setThemeMode(nextMode)
                        },
                        modifier = Modifier.testTag("btn_quick_theme")
                    ) {
                        Icon(
                            imageVector = if (uiState.themeMode == ThemeMode.DARK) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "切换模式",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                        label = { Text(text = tab.title) },
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Crossfade(
            targetState = currentTab,
            label = "tab_crossfade",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tab ->
            when (tab) {
                NavTab.CALENDAR -> CalendarScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                NavTab.STATS -> StatsScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                NavTab.REWARDS -> RewardsScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                NavTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}

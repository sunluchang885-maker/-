package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CheckInViewModel
import com.example.ui.MainContainer
import com.example.ui.theme.CalendarCheckInTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CheckInViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            CalendarCheckInTheme(
                themeMode = uiState.themeMode,
                fontScaleFactor = uiState.fontScaleFactor
            ) {
                MainContainer(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}

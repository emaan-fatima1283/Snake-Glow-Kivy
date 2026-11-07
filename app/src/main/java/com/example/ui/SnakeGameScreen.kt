package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ControlMode
import com.example.ui.theme.CyberDark
import com.example.viewmodel.SnakeViewModel

@Composable
fun SnakeGameScreen(
    viewModel: SnakeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = CyberDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Top HUD & Controls
                SnakeHudView(
                    uiState = uiState,
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleHaptics = { viewModel.toggleHaptics() },
                    onOpenSettings = { showSettings = true }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Center Game Board (Direct swipe input supported)
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SnakeBoardView(
                        uiState = uiState,
                        onSwipeDirection = { dir -> viewModel.onDirectionInput(dir) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 3. Bottom Controls & D-Pad (with support for different control modes)
                val showDpad = uiState.controlMode == ControlMode.BOTH || uiState.controlMode == ControlMode.DPAD_ONLY
                SnakeControlsView(
                    gameState = uiState.gameState,
                    onDirectionClick = { dir -> viewModel.onDirectionInput(dir) },
                    onPauseClick = { viewModel.pauseGame() },
                    onResumeClick = {
                        if (uiState.gameState == com.example.model.GameState.IDLE) viewModel.startGame()
                        else if (uiState.gameState == com.example.model.GameState.GAME_OVER) viewModel.startGame()
                        else viewModel.resumeGame()
                    },
                    onRestartClick = { viewModel.restartGame() },
                    showDpad = showDpad,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                )
            }

            // Game State Overlays (Ready, Paused, Game Over)
            SnakeGameOverlays(
                uiState = uiState,
                onStartClick = { viewModel.startGame() },
                onResumeClick = { viewModel.resumeGame() },
                onRestartClick = { viewModel.restartGame() },
                onSelectDifficulty = { diff -> viewModel.setDifficulty(diff) },
                onOpenSettings = { showSettings = true }
            )

            // Settings Dialog
            if (showSettings) {
                SnakeSettingsDialog(
                    uiState = uiState,
                    onDismiss = { showSettings = false },
                    onSelectDifficulty = { diff -> viewModel.setDifficulty(diff) },
                    onSelectControlMode = { mode -> viewModel.setControlMode(mode) },
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleHaptics = { viewModel.toggleHaptics() },
                    onResetScores = { viewModel.resetAllScores() }
                )
            }
        }
    }
}

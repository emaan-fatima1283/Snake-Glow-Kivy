package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Difficulty
import com.example.model.GameState
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SnakeUiState

@Composable
fun SnakeGameOverlays(
    uiState: SnakeUiState,
    onStartClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onSelectDifficulty: (Difficulty) -> Unit,
    onOpenSettings: () -> Unit
) {
    // 1. Ready / Idle Overlay
    AnimatedVisibility(
        visible = uiState.gameState == GameState.IDLE,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ReadyOverlay(
            uiState = uiState,
            onStartClick = onStartClick,
            onSelectDifficulty = onSelectDifficulty
        )
    }

    // 2. Pause Overlay
    AnimatedVisibility(
        visible = uiState.gameState == GameState.PAUSED,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        PauseOverlay(
            onResumeClick = onResumeClick,
            onRestartClick = onRestartClick,
            onOpenSettings = onOpenSettings
        )
    }

    // 3. Game Over Overlay
    AnimatedVisibility(
        visible = uiState.gameState == GameState.GAME_OVER,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        GameOverOverlay(
            uiState = uiState,
            onPlayAgainClick = onRestartClick,
            onOpenSettings = onOpenSettings
        )
    }
}

@Composable
private fun ReadyOverlay(
    uiState: SnakeUiState,
    onStartClick: () -> Unit,
    onSelectDifficulty: (Difficulty) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseReady")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "readyScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onStartClick() }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CyberSurface.copy(alpha = 0.95f))
                .border(2.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            // Arcade Header
            Text(
                text = "RETRO ARCADE",
                color = NeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )

            Text(
                text = "SNAKE GLOW",
                color = NeonGreen,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )

            // High score badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = NeonYellow,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "RECORD: ${uiState.highScore}",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Difficulty Selector Row
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SELECT DIFFICULTY",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Difficulty.values().forEach { diff ->
                        val isSelected = uiState.difficulty == diff
                        val diffColor = when (diff) {
                            Difficulty.EASY -> NeonGreen
                            Difficulty.MEDIUM -> NeonCyan
                            Difficulty.HARD -> NeonPink
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) diffColor.copy(alpha = 0.25f) else CyberSurfaceVariant)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) diffColor else CyberCardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = diffColor)
                                ) { onSelectDifficulty(diff) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("diff_select_${diff.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = diff.displayName,
                                color = if (isSelected) diffColor else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Start Button
            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(pulseScale)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = NeonGreen)
                    .testTag("start_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Start Game",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "TAP TO START",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // Tips
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.TouchApp,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Swipe board or tap D-Pad to move",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun PauseOverlay(
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onResumeClick() }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CyberSurface)
                .border(2.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(28.dp)
        ) {
            Text(
                text = "GAME PAUSED",
                color = NeonCyan,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )

            // Resume Button
            Button(
                onClick = onResumeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("resume_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                    Text(text = "RESUME", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            // Restart Button
            Button(
                onClick = onRestartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberSurfaceVariant,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp))
                    .testTag("restart_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, tint = NeonPink)
                    Text(text = "RESTART", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            // Settings Button
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = TextSecondary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(text = "SETTINGS", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun GameOverOverlay(
    uiState: SnakeUiState,
    onPlayAgainClick: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseGameOver")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gameOverScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark.copy(alpha = 0.90f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CyberSurface)
                .border(2.dp, NeonPink.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            // Header
            Text(
                text = "GAME OVER",
                color = NeonPink,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            // New High Score Banner
            if (uiState.isNewHighScore) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(NeonYellow.copy(alpha = 0.2f), NeonYellow.copy(alpha = 0.35f), NeonYellow.copy(alpha = 0.2f))
                            )
                        )
                        .border(1.dp, NeonYellow, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = NeonYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "NEW PERSONAL BEST!",
                            color = NeonYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Final Score Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "FINAL SCORE",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = uiState.score.toString(),
                    color = NeonGreen,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(label = "FOOD EATEN", value = uiState.foodEatenCount.toString())
                    StatItem(label = "LENGTH", value = uiState.snake.size.toString())
                    StatItem(label = "BEST", value = uiState.highScore.toString())
                }
            }

            // Play Again Button
            Button(
                onClick = onPlayAgainClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(pulseScale)
                    .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = NeonGreen)
                    .testTag("play_again_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                    Text(
                        text = "PLAY AGAIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

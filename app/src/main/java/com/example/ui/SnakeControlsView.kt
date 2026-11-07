package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Direction
import com.example.model.GameState
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SnakeControlsView(
    gameState: GameState,
    onDirectionClick: (Direction) -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    showDpad: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Action Bar: Pause/Resume and Restart with instant touch down response
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isPlaying = gameState == GameState.RUNNING
            val isPaused = gameState == GameState.PAUSED

            // Play / Pause / Resume Button
            InstantActionButton(
                onClick = {
                    when (gameState) {
                        GameState.RUNNING -> onPauseClick()
                        GameState.PAUSED -> onResumeClick()
                        else -> onResumeClick()
                    }
                },
                activeColor = if (isPlaying) NeonCyan else NeonGreen,
                testTag = "control_pause_resume"
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause Game" else "Resume Game",
                        tint = if (isPlaying) NeonCyan else NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = when (gameState) {
                            GameState.RUNNING -> "PAUSE"
                            GameState.PAUSED -> "RESUME"
                            GameState.IDLE -> "START"
                            GameState.GAME_OVER -> "NEW GAME"
                        },
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Quick Restart Button
            InstantActionButton(
                onClick = onRestartClick,
                activeColor = NeonPink,
                testTag = "control_restart"
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Restart Game",
                        tint = NeonPink,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "RESTART",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // On-Screen Cyber D-Pad with 0ms Touch Latency and Active Pressed Feedback
        if (showDpad) {
            Box(
                modifier = Modifier
                    .size(196.dp)
                    .padding(top = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background D-Pad cross ambient aura
                Box(
                    modifier = Modifier
                        .size(184.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    CyberSurfaceVariant.copy(alpha = 0.8f),
                                    CyberSurface.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(1.5.dp, CyberCardBorder.copy(alpha = 0.6f), CircleShape)
                )

                // UP Button (Top Center)
                DpadDirectionButton(
                    direction = Direction.UP,
                    icon = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Move Up",
                    testTag = "dpad_up",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp),
                    onDirectionPress = onDirectionClick
                )

                // DOWN Button (Bottom Center)
                DpadDirectionButton(
                    direction = Direction.DOWN,
                    icon = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Move Down",
                    testTag = "dpad_down",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp),
                    onDirectionPress = onDirectionClick
                )

                // LEFT Button (Center Start)
                DpadDirectionButton(
                    direction = Direction.LEFT,
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Move Left",
                    testTag = "dpad_left",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp),
                    onDirectionPress = onDirectionClick
                )

                // RIGHT Button (Center End)
                DpadDirectionButton(
                    direction = Direction.RIGHT,
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Move Right",
                    testTag = "dpad_right",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    onDirectionPress = onDirectionClick
                )

                // Center Pause/Resume Indicator Button
                CenterControlButton(
                    gameState = gameState,
                    testTag = "dpad_center_pause",
                    onPauseClick = onPauseClick,
                    onResumeClick = onResumeClick
                )
            }
        }
    }
}

/**
 * Ultra-responsive directional button with 0ms touch latency.
 * Triggers immediately on pointer DOWN with rich animated pressed state and neon glow.
 */
@Composable
private fun DpadDirectionButton(
    direction: Direction,
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    modifier: Modifier = Modifier,
    onDirectionPress: (Direction) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    // Snappy bouncy spring animation for tactile button press feel
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dpadScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) NeonCyan.copy(alpha = 0.40f) else CyberSurfaceVariant,
        animationSpec = tween(durationMillis = 60),
        label = "dpadBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isPressed) Color.White else NeonCyan.copy(alpha = 0.65f),
        animationSpec = tween(durationMillis = 60),
        label = "dpadBorder"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isPressed) Color.White else NeonCyan,
        animationSpec = tween(durationMillis = 60),
        label = "dpadIcon"
    )

    val borderWidth = if (isPressed) 2.5.dp else 1.5.dp
    val elevation = if (isPressed) 14.dp else 5.dp

    Box(
        modifier = modifier
            .size(60.dp)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isPressed) NeonGreen else NeonCyan,
                ambientColor = NeonCyan
            )
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .pointerInput(direction) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    // CRITICAL: Trigger immediately on contact without waiting for release!
                    onDirectionPress(direction)
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(36.dp)
        )
    }
}

/**
 * Center D-Pad button for Pause/Resume with instant touch response and active glow.
 */
@Composable
private fun CenterControlButton(
    gameState: GameState,
    testTag: String,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.86f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "centerScale"
    )

    val isRunning = gameState == GameState.RUNNING
    val tintColor = if (isRunning) NeonCyan else NeonGreen

    Box(
        modifier = modifier
            .size(46.dp)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 14.dp else 4.dp,
                shape = CircleShape,
                spotColor = tintColor
            )
            .clip(CircleShape)
            .background(if (isPressed) tintColor.copy(alpha = 0.40f) else CyberSurfaceVariant)
            .border(
                width = if (isPressed) 2.5.dp else 1.5.dp,
                color = if (isPressed) Color.White else tintColor.copy(alpha = 0.6f),
                shape = CircleShape
            )
            .pointerInput(gameState) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    if (gameState == GameState.RUNNING) onPauseClick()
                    else onResumeClick()
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isRunning) "Pause Game" else "Resume Game",
            tint = if (isPressed) Color.White else tintColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Instant action button with spring-press tactile response.
 */
@Composable
private fun InstantActionButton(
    onClick: () -> Unit,
    activeColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "actionScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isPressed) activeColor.copy(alpha = 0.25f) else CyberSurface)
            .border(
                width = if (isPressed) 2.dp else 1.2.dp,
                color = if (isPressed) activeColor else CyberCardBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    onClick()
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
            .padding(horizontal = 16.dp, vertical = 9.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

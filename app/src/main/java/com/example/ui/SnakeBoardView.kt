package com.example.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.Direction
import com.example.model.Food
import com.example.model.FoodType
import com.example.model.Point
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.GridBorderGlow
import com.example.ui.theme.GridCellColor
import com.example.ui.theme.GridLineColor
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenDim
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.viewmodel.SnakeUiState
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SnakeBoardView(
    uiState: SnakeUiState,
    onSwipeDirection: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for glowing food
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "foodPulse"
    )

    // Gesture tracking for continuous swipe detection
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }
    val dragThreshold = 18f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(uiState.gridWidth.toFloat() / uiState.gridHeight.toFloat())
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(16.dp), spotColor = NeonGreen)
            .clip(RoundedCornerShape(16.dp))
            .background(CyberDark)
            .border(2.dp, GridBorderGlow, RoundedCornerShape(16.dp))
            .testTag("snake_game_board")
            .pointerInput(uiState.gameState) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y

                        if (abs(totalDragX) >= dragThreshold || abs(totalDragY) >= dragThreshold) {
                            if (abs(totalDragX) > abs(totalDragY)) {
                                if (totalDragX > 0) {
                                    onSwipeDirection(Direction.RIGHT)
                                } else {
                                    onSwipeDirection(Direction.LEFT)
                                }
                            } else {
                                if (totalDragY > 0) {
                                    onSwipeDirection(Direction.DOWN)
                                } else {
                                    onSwipeDirection(Direction.UP)
                                }
                            }
                            totalDragX = 0f
                            totalDragY = 0f
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / uiState.gridWidth
            val cellHeight = size.height / uiState.gridHeight

            // 1. Draw subtle neon arcade grid
            drawGrid(uiState.gridWidth, uiState.gridHeight, cellWidth, cellHeight)

            // 2. Draw regular glowing food
            drawFood(uiState.regularFood, cellWidth, cellHeight, pulseScale)

            // 3. Draw bonus golden food (if active)
            uiState.bonusFood?.let { bonus ->
                drawBonusFood(bonus, cellWidth, cellHeight, pulseScale)
            }

            // 4. Draw Snake with neon gradient and eyes
            drawSnake(uiState.snake, uiState.direction, cellWidth, cellHeight)

            // 5. Draw Particle spark bursts
            for (p in uiState.particles) {
                val px = p.x * cellWidth
                val py = p.y * cellHeight
                drawCircle(
                    color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
                    radius = p.initialRadius * p.life,
                    center = Offset(px, py)
                )
            }
        }
    }
}

private fun DrawScope.drawGrid(gridWidth: Int, gridHeight: Int, cellW: Float, cellH: Float) {
    // Draw subtle grid cell dots/lines
    for (x in 0 until gridWidth) {
        for (y in 0 until gridHeight) {
            drawRoundRect(
                color = GridCellColor,
                topLeft = Offset(x * cellW + 1f, y * cellH + 1f),
                size = Size(cellW - 2f, cellH - 2f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
    }

    // Outer grid subtle boundary stroke
    drawRect(
        color = GridLineColor,
        topLeft = Offset.Zero,
        size = size,
        style = Stroke(width = 1f)
    )
}

private fun DrawScope.drawFood(food: Food, cellW: Float, cellH: Float, pulse: Float) {
    val centerX = food.position.x * cellW + (cellW / 2f)
    val centerY = food.position.y * cellH + (cellH / 2f)
    val baseRadius = (minOf(cellW, cellH) / 2.2f)

    // Outer radiant ambient glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(NeonPink.copy(alpha = 0.5f), Color.Transparent),
            center = Offset(centerX, centerY),
            radius = baseRadius * 2.2f * pulse
        ),
        radius = baseRadius * 2.2f * pulse,
        center = Offset(centerX, centerY)
    )

    // Inner bright core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, NeonPink, Color(0xFFC2185B)),
            center = Offset(centerX - baseRadius * 0.25f, centerY - baseRadius * 0.25f),
            radius = baseRadius * pulse
        ),
        radius = baseRadius * pulse,
        center = Offset(centerX, centerY)
    )

    // Top shine highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = baseRadius * 0.25f,
        center = Offset(centerX - baseRadius * 0.35f, centerY - baseRadius * 0.35f)
    )
}

private fun DrawScope.drawBonusFood(bonus: Food, cellW: Float, cellH: Float, pulse: Float) {
    val centerX = bonus.position.x * cellW + (cellW / 2f)
    val centerY = bonus.position.y * cellH + (cellH / 2f)
    val baseRadius = (minOf(cellW, cellH) / 2.1f)
    val remainingRatio = bonus.remainingRatio(System.currentTimeMillis())

    // Golden ambient glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(NeonYellow.copy(alpha = 0.6f), Color.Transparent),
            center = Offset(centerX, centerY),
            radius = baseRadius * 2.5f * pulse
        ),
        radius = baseRadius * 2.5f * pulse,
        center = Offset(centerX, centerY)
    )

    // Golden Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, NeonYellow, Color(0xFFFF8F00)),
            center = Offset(centerX - baseRadius * 0.2f, centerY - baseRadius * 0.2f),
            radius = baseRadius * pulse
        ),
        radius = baseRadius * pulse,
        center = Offset(centerX, centerY)
    )

    // Remaining countdown ring
    drawArc(
        color = Color.White,
        startAngle = -90f,
        sweepAngle = 360f * remainingRatio,
        useCenter = false,
        topLeft = Offset(centerX - baseRadius * 1.35f, centerY - baseRadius * 1.35f),
        size = Size(baseRadius * 2.7f, baseRadius * 2.7f),
        style = Stroke(width = 3.5f)
    )
}

private fun DrawScope.drawSnake(
    snake: List<Point>,
    direction: Direction,
    cellW: Float,
    cellH: Float
) {
    if (snake.isEmpty()) return

    val totalSegments = snake.size.toFloat()

    // 1. Draw body segments from tail to neck
    for (i in (snake.size - 1) downTo 1) {
        val segment = snake[i]
        val ratio = i.toFloat() / totalSegments

        // Smooth transition color from emerald green to cyan
        val segmentColor = lerpColor(NeonGreenDim, NeonCyan, ratio)
        val pad = 1.8f
        val segWidth = cellW - pad * 2f
        val segHeight = cellH - pad * 2f
        val topLeft = Offset(segment.x * cellW + pad, segment.y * cellH + pad)

        // Subtle glow aura behind segment
        drawRoundRect(
            color = segmentColor.copy(alpha = 0.25f),
            topLeft = Offset(segment.x * cellW, segment.y * cellH),
            size = Size(cellW, cellH),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Segment block
        drawRoundRect(
            color = segmentColor,
            topLeft = topLeft,
            size = Size(segWidth, segHeight),
            cornerRadius = CornerRadius(5f, 5f)
        )
    }

    // 2. Draw Snake Head
    val head = snake[0]
    val headPad = 1.2f
    val headWidth = cellW - headPad * 2f
    val headHeight = cellH - headPad * 2f
    val headTopLeft = Offset(head.x * cellW + headPad, head.y * cellH + headPad)
    val headCenter = Offset(head.x * cellW + cellW / 2f, head.y * cellH + cellH / 2f)

    // Head outer neon glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(NeonGreen.copy(alpha = 0.45f), Color.Transparent),
            center = headCenter,
            radius = cellW * 1.2f
        ),
        radius = cellW * 1.2f,
        center = headCenter
    )

    // Head rounded rectangle
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(NeonGreen, Color(0xFF00E676)),
            start = headTopLeft,
            end = Offset(headTopLeft.x + headWidth, headTopLeft.y + headHeight)
        ),
        topLeft = headTopLeft,
        size = Size(headWidth, headHeight),
        cornerRadius = CornerRadius(7f, 7f)
    )

    // Draw Cyber Eyes
    val eyeRadius = cellW * 0.15f
    val pupilRadius = eyeRadius * 0.55f

    val (eye1Offset, eye2Offset) = when (direction) {
        Direction.UP -> Pair(
            Offset(headCenter.x - cellW * 0.22f, headCenter.y - cellH * 0.15f),
            Offset(headCenter.x + cellW * 0.22f, headCenter.y - cellH * 0.15f)
        )
        Direction.DOWN -> Pair(
            Offset(headCenter.x - cellW * 0.22f, headCenter.y + cellH * 0.15f),
            Offset(headCenter.x + cellW * 0.22f, headCenter.y + cellH * 0.15f)
        )
        Direction.LEFT -> Pair(
            Offset(headCenter.x - cellW * 0.15f, headCenter.y - cellH * 0.22f),
            Offset(headCenter.x - cellW * 0.15f, headCenter.y + cellH * 0.22f)
        )
        Direction.RIGHT -> Pair(
            Offset(headCenter.x + cellW * 0.15f, headCenter.y - cellH * 0.22f),
            Offset(headCenter.x + cellW * 0.15f, headCenter.y + cellH * 0.22f)
        )
    }

    // Whites of the eyes
    drawCircle(color = Color.White, radius = eyeRadius, center = eye1Offset)
    drawCircle(color = Color.White, radius = eyeRadius, center = eye2Offset)

    // Pupils looking in direction
    val pupilShift = when (direction) {
        Direction.UP -> Offset(0f, -pupilRadius * 0.4f)
        Direction.DOWN -> Offset(0f, pupilRadius * 0.4f)
        Direction.LEFT -> Offset(-pupilRadius * 0.4f, 0f)
        Direction.RIGHT -> Offset(pupilRadius * 0.4f, 0f)
    }

    drawCircle(
        color = CyberDark,
        radius = pupilRadius,
        center = Offset(eye1Offset.x + pupilShift.x, eye1Offset.y + pupilShift.y)
    )
    drawCircle(
        color = CyberDark,
        radius = pupilRadius,
        center = Offset(eye2Offset.x + pupilShift.x, eye2Offset.y + pupilShift.y)
    )
}

private fun lerpColor(c1: Color, c2: Color, factor: Float): Color {
    val f = factor.coerceIn(0f, 1f)
    return Color(
        red = c1.red + (c2.red - c1.red) * f,
        green = c1.green + (c2.green - c1.green) * f,
        blue = c1.blue + (c2.blue - c1.blue) * f,
        alpha = c1.alpha + (c2.alpha - c1.alpha) * f
    )
}

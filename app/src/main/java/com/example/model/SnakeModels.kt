package com.example.model

import androidx.compose.ui.graphics.Color

data class Point(val x: Int, val y: Int)

enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    fun isOpposite(other: Direction): Boolean {
        return (this == UP && other == DOWN) ||
                (this == DOWN && other == UP) ||
                (this == LEFT && other == RIGHT) ||
                (this == RIGHT && other == LEFT)
    }

    fun getDelta(): Point {
        return when (this) {
            UP -> Point(0, -1)
            DOWN -> Point(0, 1)
            LEFT -> Point(-1, 0)
            RIGHT -> Point(1, 0)
        }
    }
}

enum class Difficulty(
    val displayName: String,
    val initialSpeedMs: Long,
    val minSpeedMs: Long,
    val speedStepMs: Long,
    val baseFoodPoints: Int,
    val bonusFoodPoints: Int
) {
    EASY(
        displayName = "Easy",
        initialSpeedMs = 230L,
        minSpeedMs = 140L,
        speedStepMs = 2L,
        baseFoodPoints = 10,
        bonusFoodPoints = 30
    ),
    MEDIUM(
        displayName = "Medium",
        initialSpeedMs = 175L,
        minSpeedMs = 95L,
        speedStepMs = 3L,
        baseFoodPoints = 15,
        bonusFoodPoints = 45
    ),
    HARD(
        displayName = "Hard",
        initialSpeedMs = 120L,
        minSpeedMs = 60L,
        speedStepMs = 3L,
        baseFoodPoints = 25,
        bonusFoodPoints = 75
    );

    fun calculateSpeedMs(score: Int): Long {
        val calculated = initialSpeedMs - (score * speedStepMs / 10L)
        return calculated.coerceAtLeast(minSpeedMs)
    }
}

enum class FoodType {
    REGULAR,
    BONUS
}

data class Food(
    val position: Point,
    val type: FoodType = FoodType.REGULAR,
    val createdTimeMillis: Long = System.currentTimeMillis(),
    val durationMillis: Long = 8000L
) {
    fun isExpired(currentTimeMillis: Long): Boolean {
        return type == FoodType.BONUS && (currentTimeMillis - createdTimeMillis > durationMillis)
    }

    fun remainingRatio(currentTimeMillis: Long): Float {
        if (type != FoodType.BONUS) return 1f
        val elapsed = currentTimeMillis - createdTimeMillis
        return (1f - (elapsed.toFloat() / durationMillis.toFloat())).coerceIn(0f, 1f)
    }
}

enum class GameState {
    IDLE,
    RUNNING,
    PAUSED,
    GAME_OVER
}

enum class ControlMode(val label: String) {
    BOTH("Both (Swipe + D-Pad)"),
    SWIPE_ONLY("Swipe Gestures Only"),
    DPAD_ONLY("D-Pad Buttons Only")
}

data class SparkParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val initialRadius: Float,
    val life: Float = 1.0f
)

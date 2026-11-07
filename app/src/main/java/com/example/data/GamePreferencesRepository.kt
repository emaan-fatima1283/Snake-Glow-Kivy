package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ControlMode
import com.example.model.Difficulty

class GamePreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("snake_game_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HIGH_SCORE_PREFIX = "high_score_"
        private const val KEY_DIFFICULTY = "selected_difficulty"
        private const val KEY_CONTROL_MODE = "selected_control_mode"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        private const val KEY_TOTAL_GAMES = "total_games_played"
        private const val KEY_TOTAL_FOOD = "total_food_eaten"
        private const val KEY_LONGEST_SNAKE = "longest_snake_length"
    }

    fun getHighScore(difficulty: Difficulty): Int {
        return prefs.getInt(KEY_HIGH_SCORE_PREFIX + difficulty.name, 0)
    }

    /**
     * Updates high score if new score is higher.
     * Returns true if this was a new high score record!
     */
    fun recordScore(difficulty: Difficulty, score: Int): Boolean {
        val currentHigh = getHighScore(difficulty)
        if (score > currentHigh) {
            prefs.edit().putInt(KEY_HIGH_SCORE_PREFIX + difficulty.name, score).apply()
            return true
        }
        return false
    }

    fun incrementGameStats(foodEaten: Int, snakeLength: Int) {
        val totalGames = prefs.getInt(KEY_TOTAL_GAMES, 0) + 1
        val totalFood = prefs.getInt(KEY_TOTAL_FOOD, 0) + foodEaten
        val currentLongest = prefs.getInt(KEY_LONGEST_SNAKE, 3)
        val longest = maxOf(currentLongest, snakeLength)

        prefs.edit()
            .putInt(KEY_TOTAL_GAMES, totalGames)
            .putInt(KEY_TOTAL_FOOD, totalFood)
            .putInt(KEY_LONGEST_SNAKE, longest)
            .apply()
    }

    fun getTotalGames(): Int = prefs.getInt(KEY_TOTAL_GAMES, 0)
    fun getTotalFood(): Int = prefs.getInt(KEY_TOTAL_FOOD, 0)
    fun getLongestSnake(): Int = prefs.getInt(KEY_LONGEST_SNAKE, 3)

    fun getDifficulty(): Difficulty {
        val name = prefs.getString(KEY_DIFFICULTY, Difficulty.MEDIUM.name) ?: Difficulty.MEDIUM.name
        return try {
            Difficulty.valueOf(name)
        } catch (_: Exception) {
            Difficulty.MEDIUM
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        prefs.edit().putString(KEY_DIFFICULTY, difficulty.name).apply()
    }

    fun getControlMode(): ControlMode {
        val name = prefs.getString(KEY_CONTROL_MODE, ControlMode.BOTH.name) ?: ControlMode.BOTH.name
        return try {
            ControlMode.valueOf(name)
        } catch (_: Exception) {
            ControlMode.BOTH
        }
    }

    fun setControlMode(mode: ControlMode) {
        prefs.edit().putString(KEY_CONTROL_MODE, mode.name).apply()
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun isHapticsEnabled(): Boolean {
        return prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
    }

    fun resetAllScores() {
        prefs.edit()
            .remove(KEY_HIGH_SCORE_PREFIX + Difficulty.EASY.name)
            .remove(KEY_HIGH_SCORE_PREFIX + Difficulty.MEDIUM.name)
            .remove(KEY_HIGH_SCORE_PREFIX + Difficulty.HARD.name)
            .putInt(KEY_TOTAL_GAMES, 0)
            .putInt(KEY_TOTAL_FOOD, 0)
            .putInt(KEY_LONGEST_SNAKE, 3)
            .apply()
    }
}

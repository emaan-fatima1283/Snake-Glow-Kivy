package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffectManager
import com.example.data.GamePreferencesRepository
import com.example.model.ControlMode
import com.example.model.Difficulty
import com.example.model.Direction
import com.example.model.Food
import com.example.model.FoodType
import com.example.model.GameState
import com.example.model.Point
import com.example.model.SparkParticle
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class SnakeUiState(
    val gameState: GameState = GameState.IDLE,
    val snake: List<Point> = listOf(Point(10, 12), Point(10, 13), Point(10, 14)),
    val direction: Direction = Direction.UP,
    val regularFood: Food = Food(position = Point(10, 6), type = FoodType.REGULAR),
    val bonusFood: Food? = null,
    val score: Int = 0,
    val highScore: Int = 0,
    val isNewHighScore: Boolean = false,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val controlMode: ControlMode = ControlMode.BOTH,
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val foodEatenCount: Int = 0,
    val currentSpeedMs: Long = Difficulty.MEDIUM.initialSpeedMs,
    val gridWidth: Int = 20,
    val gridHeight: Int = 24,
    val particles: List<SparkParticle> = emptyList(),
    val totalGamesPlayed: Int = 0,
    val totalFoodEatenLifetime: Int = 0,
    val longestSnakeLifetime: Int = 3
)

class SnakeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamePreferencesRepository(application)
    val soundManager = SoundEffectManager(application)

    private val _uiState = MutableStateFlow(
        SnakeUiState(
            difficulty = repository.getDifficulty(),
            highScore = repository.getHighScore(repository.getDifficulty()),
            controlMode = repository.getControlMode(),
            isSoundEnabled = repository.isSoundEnabled(),
            isHapticsEnabled = repository.isHapticsEnabled(),
            totalGamesPlayed = repository.getTotalGames(),
            totalFoodEatenLifetime = repository.getTotalFood(),
            longestSnakeLifetime = repository.getLongestSnake()
        )
    )
    val uiState: StateFlow<SnakeUiState> = _uiState.asStateFlow()

    private var gameLoopJob: Job? = null
    private val inputQueue = ArrayDeque<Direction>(2)

    init {
        soundManager.isSoundEnabled = repository.isSoundEnabled()
        soundManager.isHapticsEnabled = repository.isHapticsEnabled()
        resetGameToIdle()
    }

    fun startGame() {
        soundManager.playButtonClick()
        inputQueue.clear()

        val initialSnake = listOf(Point(10, 12), Point(10, 13), Point(10, 14))
        val initialDir = Direction.UP
        val initialFood = spawnFood(initialSnake, emptyList())

        val currentDiff = _uiState.value.difficulty
        val currentHigh = repository.getHighScore(currentDiff)

        _uiState.update {
            it.copy(
                gameState = GameState.RUNNING,
                snake = initialSnake,
                direction = initialDir,
                regularFood = initialFood,
                bonusFood = null,
                score = 0,
                highScore = currentHigh,
                isNewHighScore = false,
                foodEatenCount = 0,
                currentSpeedMs = currentDiff.initialSpeedMs,
                particles = emptyList()
            )
        }

        launchGameLoop()
    }

    fun pauseGame() {
        if (_uiState.value.gameState == GameState.RUNNING) {
            soundManager.playButtonClick()
            gameLoopJob?.cancel()
            _uiState.update { it.copy(gameState = GameState.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameState == GameState.PAUSED) {
            soundManager.playButtonClick()
            _uiState.update { it.copy(gameState = GameState.RUNNING) }
            launchGameLoop()
        }
    }

    fun restartGame() {
        gameLoopJob?.cancel()
        startGame()
    }

    fun resetGameToIdle() {
        gameLoopJob?.cancel()
        inputQueue.clear()
        val initialSnake = listOf(Point(10, 12), Point(10, 13), Point(10, 14))
        val currentDiff = _uiState.value.difficulty
        _uiState.update {
            it.copy(
                gameState = GameState.IDLE,
                snake = initialSnake,
                direction = Direction.UP,
                regularFood = Point(10, 6).let { pt -> Food(pt, FoodType.REGULAR) },
                bonusFood = null,
                score = 0,
                highScore = repository.getHighScore(currentDiff),
                isNewHighScore = false,
                foodEatenCount = 0,
                currentSpeedMs = currentDiff.initialSpeedMs,
                particles = emptyList()
            )
        }
    }

    fun onDirectionInput(newDirection: Direction) {
        soundManager.playButtonClick()

        if (_uiState.value.gameState == GameState.IDLE) {
            startGame()
            if (newDirection != Direction.DOWN) {
                inputQueue.addLast(newDirection)
            }
            return
        }

        if (_uiState.value.gameState != GameState.RUNNING) return

        val lastQueued = if (inputQueue.isNotEmpty()) inputQueue.last else _uiState.value.direction
        if (!newDirection.isOpposite(lastQueued) && newDirection != lastQueued) {
            if (inputQueue.size < 2) {
                inputQueue.addLast(newDirection)
            } else {
                // If user changes their mind or taps fast, update the pending turn
                inputQueue.removeLast()
                inputQueue.addLast(newDirection)
            }
        }
    }

    private fun launchGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (isActive && _uiState.value.gameState == GameState.RUNNING) {
                val delayTime = _uiState.value.currentSpeedMs
                delay(delayTime)
                tick()
            }
        }
    }

    private fun tick() {
        val currentState = _uiState.value
        if (currentState.gameState != GameState.RUNNING) return

        // 1. Resolve direction from input queue
        val nextDirection = if (inputQueue.isNotEmpty()) {
            inputQueue.removeFirst()
        } else {
            currentState.direction
        }

        val currentSnake = currentState.snake
        val head = currentSnake.first()
        val delta = nextDirection.getDelta()
        val newHead = Point(head.x + delta.x, head.y + delta.y)

        // 2. Wall collision check
        if (newHead.x < 0 || newHead.x >= currentState.gridWidth ||
            newHead.y < 0 || newHead.y >= currentState.gridHeight
        ) {
            handleGameOver()
            return
        }

        // 3. Self-collision check (check against body excluding tail unless food is eaten)
        val willEatRegular = newHead == currentState.regularFood.position
        val willEatBonus = currentState.bonusFood != null &&
                newHead == currentState.bonusFood.position &&
                !currentState.bonusFood.isExpired(System.currentTimeMillis())

        val bodyToCheck = if (willEatRegular || willEatBonus) currentSnake else currentSnake.dropLast(1)
        if (bodyToCheck.contains(newHead)) {
            handleGameOver()
            return
        }

        // 4. Update snake position
        val newSnake = mutableListOf(newHead)
        if (willEatRegular || willEatBonus) {
            newSnake.addAll(currentSnake)
        } else {
            newSnake.addAll(currentSnake.dropLast(1))
        }

        // 5. Handle Food Consumption
        var newScore = currentState.score
        var newFoodEatenCount = currentState.foodEatenCount
        var newRegularFood = currentState.regularFood
        var newBonusFood = currentState.bonusFood
        var newParticles = updateParticles(currentState.particles)

        // Check if bonus food expired
        if (newBonusFood != null && newBonusFood.isExpired(System.currentTimeMillis())) {
            newBonusFood = null
        }

        if (willEatRegular) {
            newScore += currentState.difficulty.baseFoodPoints
            newFoodEatenCount++
            soundManager.playEatRegular()
            newParticles = newParticles + createSparks(newHead.x.toFloat(), newHead.y.toFloat(), NeonPink)
            newRegularFood = spawnFood(newSnake, listOfNotNull(newBonusFood?.position))

            // Check if bonus food should spawn (every 5 regular foods, if none active)
            if (newFoodEatenCount % 5 == 0 && newBonusFood == null) {
                newBonusFood = spawnBonusFood(newSnake, listOf(newRegularFood.position))
            }
        } else if (willEatBonus) {
            newScore += currentState.difficulty.bonusFoodPoints
            newFoodEatenCount++
            soundManager.playEatBonus()
            newParticles = newParticles + createSparks(newHead.x.toFloat(), newHead.y.toFloat(), NeonYellow)
            newBonusFood = null
        }

        // Calculate dynamic speed based on score and difficulty
        val newSpeedMs = currentState.difficulty.calculateSpeedMs(newScore)

        _uiState.update {
            it.copy(
                snake = newSnake,
                direction = nextDirection,
                regularFood = newRegularFood,
                bonusFood = newBonusFood,
                score = newScore,
                foodEatenCount = newFoodEatenCount,
                currentSpeedMs = newSpeedMs,
                particles = newParticles
            )
        }
    }

    private fun handleGameOver() {
        gameLoopJob?.cancel()
        soundManager.playGameOver()

        val state = _uiState.value
        val isNewRecord = repository.recordScore(state.difficulty, state.score)
        repository.incrementGameStats(state.foodEatenCount, state.snake.size)

        val updatedHighScore = repository.getHighScore(state.difficulty)
        val totalGames = repository.getTotalGames()
        val totalFood = repository.getTotalFood()
        val longestSnake = repository.getLongestSnake()

        _uiState.update {
            it.copy(
                gameState = GameState.GAME_OVER,
                highScore = updatedHighScore,
                isNewHighScore = isNewRecord,
                totalGamesPlayed = totalGames,
                totalFoodEatenLifetime = totalFood,
                longestSnakeLifetime = longestSnake
            )
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        if (_uiState.value.difficulty != difficulty) {
            repository.setDifficulty(difficulty)
            val newHighScore = repository.getHighScore(difficulty)
            _uiState.update {
                it.copy(
                    difficulty = difficulty,
                    highScore = newHighScore,
                    currentSpeedMs = difficulty.initialSpeedMs
                )
            }
            if (_uiState.value.gameState == GameState.IDLE) {
                resetGameToIdle()
            }
        }
    }

    fun setControlMode(mode: ControlMode) {
        repository.setControlMode(mode)
        _uiState.update { it.copy(controlMode = mode) }
    }

    fun toggleSound() {
        val newState = !_uiState.value.isSoundEnabled
        repository.setSoundEnabled(newState)
        soundManager.isSoundEnabled = newState
        _uiState.update { it.copy(isSoundEnabled = newState) }
        if (newState) soundManager.playButtonClick()
    }

    fun toggleHaptics() {
        val newState = !_uiState.value.isHapticsEnabled
        repository.setHapticsEnabled(newState)
        soundManager.isHapticsEnabled = newState
        _uiState.update { it.copy(isHapticsEnabled = newState) }
        if (newState) soundManager.playButtonClick()
    }

    fun resetAllScores() {
        repository.resetAllScores()
        _uiState.update {
            it.copy(
                highScore = 0,
                isNewHighScore = false,
                totalGamesPlayed = 0,
                totalFoodEatenLifetime = 0,
                longestSnakeLifetime = 3
            )
        }
        soundManager.playButtonClick()
    }

    private fun spawnFood(snake: List<Point>, occupied: List<Point>): Food {
        val unavailable = (snake + occupied).toSet()
        val availablePoints = mutableListOf<Point>()
        val width = _uiState.value.gridWidth
        val height = _uiState.value.gridHeight

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pt = Point(x, y)
                if (!unavailable.contains(pt)) {
                    availablePoints.add(pt)
                }
            }
        }

        val chosenPoint = if (availablePoints.isNotEmpty()) {
            availablePoints[Random.nextInt(availablePoints.size)]
        } else {
            Point(0, 0)
        }

        return Food(chosenPoint, FoodType.REGULAR)
    }

    private fun spawnBonusFood(snake: List<Point>, occupied: List<Point>): Food {
        val unavailable = (snake + occupied).toSet()
        val availablePoints = mutableListOf<Point>()
        val width = _uiState.value.gridWidth
        val height = _uiState.value.gridHeight

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pt = Point(x, y)
                if (!unavailable.contains(pt)) {
                    availablePoints.add(pt)
                }
            }
        }

        val chosenPoint = if (availablePoints.isNotEmpty()) {
            availablePoints[Random.nextInt(availablePoints.size)]
        } else {
            Point(width / 2, height / 2)
        }

        return Food(
            position = chosenPoint,
            type = FoodType.BONUS,
            createdTimeMillis = System.currentTimeMillis(),
            durationMillis = 8000L
        )
    }

    private fun createSparks(gridX: Float, gridY: Float, color: Color): List<SparkParticle> {
        val particles = mutableListOf<SparkParticle>()
        val count = 12
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 0.4f + 0.15f
            particles.add(
                SparkParticle(
                    x = gridX + 0.5f,
                    y = gridY + 0.5f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    initialRadius = Random.nextFloat() * 4f + 3f,
                    life = 1.0f
                )
            )
        }
        return particles
    }

    private fun updateParticles(particles: List<SparkParticle>): List<SparkParticle> {
        if (particles.isEmpty()) return emptyList()
        return particles.mapNotNull { p ->
            val nextLife = p.life - 0.15f
            if (nextLife <= 0f) null
            else p.copy(
                x = p.x + p.vx,
                y = p.y + p.vy,
                life = nextLife
            )
        }
    }
}

package com.example

import com.example.model.Difficulty
import com.example.model.Direction
import com.example.model.Food
import com.example.model.FoodType
import com.example.model.Point
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testDirectionOpposites() {
        // Snake must never be allowed to reverse directly into itself
        assertTrue(Direction.UP.isOpposite(Direction.DOWN))
        assertTrue(Direction.DOWN.isOpposite(Direction.UP))
        assertTrue(Direction.LEFT.isOpposite(Direction.RIGHT))
        assertTrue(Direction.RIGHT.isOpposite(Direction.LEFT))

        assertFalse(Direction.UP.isOpposite(Direction.LEFT))
        assertFalse(Direction.UP.isOpposite(Direction.RIGHT))
        assertFalse(Direction.DOWN.isOpposite(Direction.LEFT))
        assertFalse(Direction.DOWN.isOpposite(Direction.RIGHT))
    }

    @Test
    fun testDirectionDeltas() {
        assertEquals(Point(0, -1), Direction.UP.getDelta())
        assertEquals(Point(0, 1), Direction.DOWN.getDelta())
        assertEquals(Point(-1, 0), Direction.LEFT.getDelta())
        assertEquals(Point(1, 0), Direction.RIGHT.getDelta())
    }

    @Test
    fun testDifficultySpeedScaling() {
        val easy = Difficulty.EASY
        val initialSpeed = easy.calculateSpeedMs(0)
        val fasterSpeed = easy.calculateSpeedMs(200)
        val clampedSpeed = easy.calculateSpeedMs(10000)

        assertEquals(easy.initialSpeedMs, initialSpeed)
        assertTrue(fasterSpeed < initialSpeed)
        assertEquals(easy.minSpeedMs, clampedSpeed)
    }

    @Test
    fun testFoodExpiration() {
        val regular = Food(Point(5, 5), FoodType.REGULAR)
        assertFalse(regular.isExpired(System.currentTimeMillis() + 100000L))

        val bonus = Food(Point(5, 5), FoodType.BONUS, createdTimeMillis = 1000L, durationMillis = 5000L)
        assertFalse(bonus.isExpired(3000L))
        assertTrue(bonus.isExpired(7000L))
    }

    @Test
    fun testSnakeMovementAndGrowth() {
        // Initial snake of length 3 moving UP
        var snake = listOf(Point(10, 12), Point(10, 13), Point(10, 14))
        var direction = Direction.UP

        // 1 normal step (no food)
        val delta = direction.getDelta()
        val newHead = Point(snake.first().x + delta.x, snake.first().y + delta.y)
        snake = listOf(newHead) + snake.dropLast(1)

        assertEquals(Point(10, 11), snake[0])
        assertEquals(Point(10, 12), snake[1])
        assertEquals(Point(10, 13), snake[2])
        assertEquals(3, snake.size)

        // 1 step eating food -> should grow
        val delta2 = direction.getDelta()
        val foodHead = Point(snake.first().x + delta2.x, snake.first().y + delta2.y)
        snake = listOf(foodHead) + snake // entire previous snake retained

        assertEquals(Point(10, 10), snake[0])
        assertEquals(4, snake.size)
    }

    @Test
    fun testCollisionDetectionLogic() {
        val gridWidth = 20
        val gridHeight = 24

        // Wall collision checks
        val outLeft = Point(-1, 10)
        val outRight = Point(20, 10)
        val outTop = Point(10, -1)
        val outBottom = Point(10, 24)
        val inside = Point(10, 10)

        assertTrue(outLeft.x < 0 || outLeft.x >= gridWidth || outLeft.y < 0 || outLeft.y >= gridHeight)
        assertTrue(outRight.x < 0 || outRight.x >= gridWidth || outRight.y < 0 || outRight.y >= gridHeight)
        assertTrue(outTop.x < 0 || outTop.x >= gridWidth || outTop.y < 0 || outTop.y >= gridHeight)
        assertTrue(outBottom.x < 0 || outBottom.x >= gridWidth || outBottom.y < 0 || outBottom.y >= gridHeight)
        assertFalse(inside.x < 0 || inside.x >= gridWidth || inside.y < 0 || inside.y >= gridHeight)
    }
}

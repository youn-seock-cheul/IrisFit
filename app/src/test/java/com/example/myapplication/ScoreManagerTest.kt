package com.example.myapplication

import com.example.myapplication.data.ScoreManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreManagerTest {

    @Test
    fun `processTick with Hit adds 10 points for first 4 seconds`() {
        val scoreManager = ScoreManager(initialLevel = 1, initialExp = 0, targetExpPerLevel = 500)

        repeat(4) {
            scoreManager.processTick(isHit = true)
        }

        assertEquals(4, scoreManager.continuousHitSeconds)
        assertFalse(scoreManager.isComboActive)
        assertEquals(40, scoreManager.currentExp)
    }

    @Test
    fun `processTick with Hit activates combo on 5th second and adds 15 points per tick`() {
        val scoreManager = ScoreManager(initialLevel = 1, initialExp = 0, targetExpPerLevel = 500)

        // 1 to 4 sec: 4 * 10 = 40 pts
        repeat(4) {
            scoreManager.processTick(isHit = true)
        }

        // 5th sec: 40 + 15 = 55 pts, combo active
        scoreManager.processTick(isHit = true)
        assertEquals(5, scoreManager.continuousHitSeconds)
        assertTrue(scoreManager.isComboActive)
        assertEquals(55, scoreManager.currentExp)

        // 6th sec: 55 + 15 = 70 pts
        scoreManager.processTick(isHit = true)
        assertEquals(70, scoreManager.currentExp)
    }

    @Test
    fun `processTick with Miss resets combo seconds`() {
        val scoreManager = ScoreManager(initialLevel = 1, initialExp = 0, targetExpPerLevel = 500)

        repeat(6) {
            scoreManager.processTick(isHit = true)
        }
        assertTrue(scoreManager.isComboActive)

        scoreManager.processTick(isHit = false)
        assertEquals(0, scoreManager.continuousHitSeconds)
        assertFalse(scoreManager.isComboActive)
    }

    @Test
    fun `addExp triggers level up when EXP reaches target`() {
        var levelUpEventLevel = -1
        val scoreManager = ScoreManager(
            initialLevel = 1,
            initialExp = 480,
            targetExpPerLevel = 500,
            onLevelUpListener = { newLevel -> levelUpEventLevel = newLevel }
        )

        // 480 + 10 = 490
        scoreManager.processTick(isHit = true)
        assertEquals(1, scoreManager.currentLevel)
        assertEquals(490, scoreManager.currentExp)

        // 490 + 10 = 500 -> Level Up to 2, EXP reset to 0
        scoreManager.processTick(isHit = true)
        assertEquals(2, scoreManager.currentLevel)
        assertEquals(0, scoreManager.currentExp)
        assertEquals(2, levelUpEventLevel)
    }
}

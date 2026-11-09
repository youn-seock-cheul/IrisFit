package com.example.myapplication.data

import kotlin.math.max

class ScoreManager(
    initialLevel: Int = 1,
    initialExp: Int = 0,
    val targetExpPerLevel: Int = 500,
    private val onLevelUpListener: ((newLevel: Int) -> Unit)? = null,
    private val onExpChangedListener: ((currentExp: Int, level: Int) -> Unit)? = null
) {
    var currentLevel: Int = max(1, initialLevel)
        private set

    var currentExp: Int = max(0, initialExp)
        private set

    var continuousHitSeconds: Int = 0
        private set

    val isComboActive: Boolean
        get() = continuousHitSeconds >= 5

    /**
     * Called every second when Hit status is maintained or Miss occurs.
     * @param isHit whether gaze was within target marker allowance range during the last second.
     */
    fun processTick(isHit: Boolean) {
        if (isHit) {
            continuousHitSeconds++
            val pointsEarned = if (isComboActive) 15 else 10
            addExp(pointsEarned)
        } else {
            continuousHitSeconds = 0
            onExpChangedListener?.invoke(currentExp, currentLevel)
        }
    }

    private fun addExp(points: Int) {
        currentExp += points
        while (currentExp >= targetExpPerLevel) {
            currentExp -= targetExpPerLevel
            currentLevel++
            onLevelUpListener?.invoke(currentLevel)
        }
        onExpChangedListener?.invoke(currentExp, currentLevel)
    }

    fun resetCombo() {
        continuousHitSeconds = 0
    }
}

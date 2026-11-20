package com.example.myapplication

import com.example.myapplication.session.RingDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class RingDirectionTest {
    @Test fun ringGapMatchesTheDirectionLabelInCanvasCoordinates() {
        val expectedGap = mapOf(
            RingDirection.UP to 270f, RingDirection.DOWN to 90f,
            RingDirection.LEFT to 180f, RingDirection.RIGHT to 0f
        )
        expectedGap.forEach { (direction, angle) ->
            assertEquals(angle, (direction.startAngle + 315f) % 360f, 0.001f)
        }
    }
}

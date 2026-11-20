package com.example.myapplication

import com.example.myapplication.ui.SatelliteOrbitGeometry
import com.example.myapplication.ui.pursuitPlanetLocalizationKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

class SatelliteOrbitGeometryTest {
    @Test fun pursuitModesMapToRequestedPlanetOrder() {
        assertEquals(
            listOf(
                "pursuit_planet_earth",
                "pursuit_planet_jupiter",
                "pursuit_planet_saturn",
                "pursuit_planet_venus"
            ),
            (0..3).map(::pursuitPlanetLocalizationKey)
        )
    }

    @Test fun allFourOrbitModesHaveDistinctBoundedPaths() {
        val signatures = (0..3).map { mode ->
            (0..15).map { sample ->
                val angle = sample * SatelliteOrbitGeometry.TWO_PI / 16f
                val point = SatelliteOrbitGeometry.position(mode, angle)
                assertTrue(point.x in -1.2f..1.2f)
                assertTrue(point.y in -0.8f..0.8f)
                assertTrue(point.z in -0.5f..0.5f)
                (point.x * 1000).toInt() to (point.y * 1000).toInt()
            }
        }
        assertEquals(4, signatures.distinct().size)
    }

    @Test fun projectedSatellitesStayInsideOrbitCanvasAndRepeat() {
        val width = 900f
        val height = 600f
        (0..3).forEach { mode ->
            (0..360).step(15).forEach { degrees ->
                val angle = Math.toRadians(degrees.toDouble()).toFloat()
                val point = SatelliteOrbitGeometry.project(mode, angle, width, height)
                assertTrue(point.x in width * 0.08f..width * 0.92f)
                assertTrue(point.y in height * 0.08f..height * 0.92f)
            }
            val start = SatelliteOrbitGeometry.project(mode, 0.37f, width, height)
            val end = SatelliteOrbitGeometry.project(
                mode,
                0.37f + SatelliteOrbitGeometry.TWO_PI,
                width,
                height
            )
            assertTrue(abs(start.x - end.x) < 0.01f)
            assertTrue(abs(start.y - end.y) < 0.01f)
        }
    }

    @Test fun smoothSCurveHasNoSharpTurnsOrJumps() {
        val samples = 360
        val points = (0..samples).map { sample ->
            SatelliteOrbitGeometry.position(
                mode = 3,
                angle = sample * SatelliteOrbitGeometry.TWO_PI / samples
            )
        }

        assertTrue(abs(points.first().x - points.last().x) < 0.001f)
        assertTrue(abs(points.first().y - points.last().y) < 0.001f)
        assertTrue(abs(points.first().z - points.last().z) < 0.001f)
        points.zipWithNext().forEach { (start, end) ->
            val distance = sqrt(
                (end.x - start.x) * (end.x - start.x) +
                    (end.y - start.y) * (end.y - start.y) +
                    (end.z - start.z) * (end.z - start.z)
            )
            assertTrue("S-curve has an abrupt step: $distance", distance < 0.05f)
        }
    }
}

package com.example.myapplication.ui

import androidx.compose.foundation.Canvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

data class OrbitPoint(val x: Float, val y: Float, val z: Float)
data class OrbitProjection(val x: Float, val y: Float, val depth: Float)

object SatelliteOrbitGeometry {
    const val TWO_PI = 6.2831855f

    fun position(mode: Int, angle: Float): OrbitPoint {
        val sine = sin(angle)
        val cosine = cos(angle)
        return when (mode.coerceIn(0, 3)) {
            0 -> {
                val denominator = 1f + cosine * cosine
                OrbitPoint(
                    x = 1.08f * sine / denominator,
                    y = 0.68f * sine * cosine / denominator,
                    z = 0.34f * sin(2f * angle)
                )
            }
            1 -> OrbitPoint(
                x = 0.92f * sine,
                y = 0.66f * sin(2f * angle),
                z = 0.28f * cosine
            )
            2 -> OrbitPoint(
                x = 0.78f * cosine,
                y = 0.62f * sine,
                z = 0.42f * sine
            )
            else -> OrbitPoint(
                x = 0.9f * sine,
                y = 0.46f * sine + 0.2f * sin(2f * angle),
                z = 0.28f * cosine + 0.08f * sin(2f * angle)
            )
        }
    }

    fun project(mode: Int, angle: Float, width: Float, height: Float): OrbitProjection {
        val point = position(mode, angle)
        val perspective = 1f / (1f - point.z * 0.18f)
        val tilt = 0.42f
        val projectedX = point.x * perspective
        val projectedY = (point.y * cos(tilt) - point.z * sin(tilt)) * perspective
        return OrbitProjection(
            x = width * (0.5f + projectedX * 0.35f),
            y = height * (0.5f + projectedY * 0.34f),
            depth = point.z
        )
    }
}

internal fun pursuitPlanetLocalizationKey(mode: Int): String = when (mode.coerceIn(0, 3)) {
    0 -> "pursuit_planet_earth"
    1 -> "pursuit_planet_jupiter"
    2 -> "pursuit_planet_saturn"
    else -> "pursuit_planet_venus"
}

@androidx.compose.runtime.Composable
fun SatelliteOrbitCanvas(
    angle: Float,
    mode: Int,
    orbitColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.testTag("satellite_orbit_canvas")) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val minDimension = size.minDimension
        val planetRadius = minDimension * 0.0775f
        val satelliteRadius = minDimension * 0.015f
        val strokeWidth = 1.5.dp.toPx()
        val samples = 180

        drawRect(Color(0xFF07111F))
        repeat(34) { index ->
            val x = ((index * 137 + 59) % 997) / 997f * size.width
            val y = ((index * 257 + 31) % 991) / 991f * size.height
            drawCircle(
                color = Color.White.copy(alpha = if (index % 4 == 0) 0.72f else 0.34f),
                radius = (0.7f + index % 3 * 0.35f).dp.toPx(),
                center = Offset(x, y)
            )
        }

        fun orbitProjection(sample: Int): OrbitProjection =
            SatelliteOrbitGeometry.project(mode, angle + sample * SatelliteOrbitGeometry.TWO_PI / samples, size.width, size.height)

        val satellite = SatelliteOrbitGeometry.project(mode, angle, size.width, size.height)
        val satelliteCenter = Offset(satellite.x, satellite.y)

        fun drawSatellite() {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFF4F0E7), Color(0xFFB8B8B1), Color(0xFF555C67)),
                    center = Offset(satellite.x - satelliteRadius * 0.38f, satellite.y - satelliteRadius * 0.42f),
                    radius = satelliteRadius * 1.8f
                ),
                radius = satelliteRadius,
                center = satelliteCenter
            )
            drawCircle(
                color = Color(0xFF73777B).copy(alpha = 0.75f),
                radius = satelliteRadius * 0.16f,
                center = Offset(satellite.x + satelliteRadius * 0.34f, satellite.y - satelliteRadius * 0.1f)
            )
            drawCircle(
                color = Color(0xFF7C7E7C).copy(alpha = 0.7f),
                radius = satelliteRadius * 0.1f,
                center = Offset(satellite.x - satelliteRadius * 0.14f, satellite.y + satelliteRadius * 0.42f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.42f),
                radius = satelliteRadius * 0.13f,
                center = Offset(satellite.x - satelliteRadius * 0.4f, satellite.y - satelliteRadius * 0.45f)
            )
        }

        val backOrbit = Path()
        val frontOrbit = Path()
        for (sample in 0 until samples) {
            val start = orbitProjection(sample)
            val end = orbitProjection(sample + 1)
            val path = if ((start.depth + end.depth) * 0.5f <= 0f) backOrbit else frontOrbit
            path.moveTo(start.x, start.y)
            path.lineTo(end.x, end.y)
        }
        drawPath(backOrbit, color = orbitColor.copy(alpha = 0.28f), style = Stroke(width = strokeWidth))
        if (satellite.depth <= 0f) drawSatellite()

        if (mode == 2) {
            drawSaturnRing(center, planetRadius, strokeWidth, front = false)
        }

        drawPlanet(mode, center, planetRadius, angle)

        drawPath(frontOrbit, color = orbitColor.copy(alpha = 0.72f), style = Stroke(width = strokeWidth))
        if (mode == 2) {
            drawSaturnRing(center, planetRadius, strokeWidth, front = true)
        }
        if (satellite.depth > 0f) drawSatellite()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlanet(
    mode: Int,
    center: Offset,
    radius: Float,
    angle: Float
) {
    val diameter = radius * 2f
    val left = center.x - radius
    val top = center.y - radius
    val baseColors = when (mode) {
        0 -> listOf(Color(0xFF72D7F2), Color(0xFF1474B8), Color(0xFF082F67))
        1 -> listOf(Color(0xFFFFE6A6), Color(0xFFD59A52), Color(0xFF8F4D2B))
        2 -> listOf(Color(0xFFFFE8B0), Color(0xFFC99B5B), Color(0xFF74503A))
        else -> listOf(Color(0xFFFFF0A5), Color(0xFFE7AF4F), Color(0xFF9B552A))
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = baseColors,
            center = Offset(center.x - radius * 0.36f, center.y - radius * 0.42f),
            radius = radius * 1.8f
        ),
        radius = radius,
        center = center
    )

    val unitCircle = Path().apply { addOval(Rect(0f, 0f, 1f, 1f)) }
    withTransform({
        translate(left, top)
        scale(diameter, diameter, pivot = Offset.Zero)
    }) {
        rotate(
            degrees = Math.toDegrees((angle * 0.2f).toDouble()).toFloat(),
            pivot = Offset(0.5f, 0.5f)
        ) {
            clipPath(unitCircle) {
                when (mode) {
                0 -> {
                    val africaEurope = Path().apply {
                        moveTo(0.47f, 0.28f)
                        cubicTo(0.56f, 0.24f, 0.65f, 0.31f, 0.64f, 0.39f)
                        cubicTo(0.62f, 0.44f, 0.57f, 0.44f, 0.55f, 0.49f)
                        cubicTo(0.53f, 0.56f, 0.57f, 0.61f, 0.53f, 0.68f)
                        cubicTo(0.50f, 0.75f, 0.47f, 0.70f, 0.45f, 0.63f)
                        cubicTo(0.43f, 0.56f, 0.39f, 0.53f, 0.41f, 0.47f)
                        cubicTo(0.43f, 0.42f, 0.39f, 0.38f, 0.42f, 0.34f)
                        close()
                    }
                    val americas = Path().apply {
                        moveTo(0.27f, 0.22f)
                        cubicTo(0.36f, 0.25f, 0.38f, 0.34f, 0.34f, 0.40f)
                        cubicTo(0.31f, 0.45f, 0.34f, 0.51f, 0.30f, 0.57f)
                        cubicTo(0.27f, 0.63f, 0.30f, 0.70f, 0.25f, 0.76f)
                        cubicTo(0.21f, 0.70f, 0.23f, 0.63f, 0.19f, 0.57f)
                        cubicTo(0.15f, 0.51f, 0.19f, 0.44f, 0.20f, 0.37f)
                        cubicTo(0.20f, 0.30f, 0.22f, 0.25f, 0.27f, 0.22f)
                        close()
                    }
                    val land = Color(0xFF4FAD68)
                    drawPath(americas, color = land)
                    drawPath(africaEurope, color = land.copy(alpha = 0.94f))
                    drawPath(
                        Path().apply {
                            moveTo(0.70f, 0.24f)
                            cubicTo(0.81f, 0.27f, 0.85f, 0.35f, 0.80f, 0.40f)
                            cubicTo(0.75f, 0.43f, 0.74f, 0.48f, 0.69f, 0.45f)
                            cubicTo(0.65f, 0.40f, 0.67f, 0.32f, 0.70f, 0.24f)
                            close()
                        },
                        color = Color(0xFF79C66A)
                    )
                    drawPath(
                        Path().apply {
                            moveTo(0.06f, 0.37f)
                            cubicTo(0.20f, 0.31f, 0.29f, 0.35f, 0.39f, 0.32f)
                            moveTo(0.58f, 0.73f)
                            cubicTo(0.69f, 0.67f, 0.78f, 0.71f, 0.91f, 0.63f)
                        },
                        color = Color.White.copy(alpha = 0.46f),
                        style = Stroke(width = 0.035f)
                    )
                }
                1 -> {
                    val bandColors = listOf(
                        Color(0xFFB96D43), Color(0xFFF0C982), Color(0xFFB9754A),
                        Color(0xFFE7B96D), Color(0xFF9E5536), Color(0xFFE8C27B),
                        Color(0xFFC47C4E)
                    )
                    bandColors.forEachIndexed { index, color ->
                        val y = 0.17f + index * 0.105f
                        drawPath(
                            Path().apply {
                                moveTo(0.02f, y)
                                cubicTo(0.27f, y - 0.07f, 0.72f, y + 0.08f, 0.98f, y - 0.015f)
                            },
                            color = color.copy(alpha = 0.86f),
                            style = Stroke(width = 0.075f)
                        )
                    }
                    drawOval(
                        color = Color(0xFFC95F43),
                        topLeft = Offset(0.57f, 0.53f),
                        size = Size(0.24f, 0.12f)
                    )
                    drawOval(
                        color = Color(0xFFFFC58B).copy(alpha = 0.8f),
                        topLeft = Offset(0.60f, 0.55f),
                        size = Size(0.18f, 0.07f)
                    )
                }
                2 -> {
                    listOf(
                        Color(0xFFE4C68D), Color(0xFFAF8050), Color(0xFFF1D6A0),
                        Color(0xFFB58B5B), Color(0xFFEBD09B), Color(0xFFA8784E)
                    ).forEachIndexed { index, color ->
                        val y = 0.18f + index * 0.13f
                        drawPath(
                            Path().apply {
                                moveTo(0f, y)
                                cubicTo(0.32f, y + 0.045f, 0.66f, y - 0.04f, 1f, y + 0.02f)
                            },
                            color = color.copy(alpha = 0.6f),
                            style = Stroke(width = 0.055f)
                        )
                    }
                }
                else -> {
                    val cloudColors = listOf(
                        Color(0xFFFFF2B1).copy(alpha = 0.84f),
                        Color(0xFFFFD978).copy(alpha = 0.76f),
                        Color(0xFFF2C35D).copy(alpha = 0.75f),
                        Color(0xFFFFE9A0).copy(alpha = 0.72f)
                    )
                    cloudColors.forEachIndexed { index, color ->
                        val y = 0.28f + index * 0.15f
                        drawPath(
                            Path().apply {
                                moveTo(-0.1f, y + 0.08f)
                                cubicTo(0.18f, y - 0.1f, 0.47f, y + 0.16f, 0.72f, y - 0.01f)
                                cubicTo(0.88f, y - 0.11f, 1.02f, y - 0.03f, 1.1f, y - 0.08f)
                            },
                            color = color,
                            style = Stroke(width = 0.1f)
                        )
                    }
                    drawOval(
                        color = Color(0xFFFFF7D2).copy(alpha = 0.6f),
                        topLeft = Offset(0.18f, 0.22f),
                        size = Size(0.2f, 0.07f)
                    )
                    }
                }
            }
        }
    }

    drawCircle(
        color = Color(0xFF061323).copy(alpha = 0.48f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.2.dp.toPx())
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.19f),
        radius = radius * 0.17f,
        center = Offset(center.x - radius * 0.34f, center.y - radius * 0.39f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSaturnRing(
    center: Offset,
    planetRadius: Float,
    strokeWidth: Float,
    front: Boolean
) {
    rotate(degrees = -18f, pivot = center) {
        val ringBounds = Offset(center.x - planetRadius * 1.85f, center.y - planetRadius * 0.31f)
        val ringSize = Size(planetRadius * 3.7f, planetRadius * 0.62f)
        if (front) {
            drawArc(
                color = Color(0xFFE6C78F).copy(alpha = 0.88f),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = ringBounds,
                size = ringSize,
                style = Stroke(width = strokeWidth * 2.2f)
            )
            drawArc(
                color = Color(0xFF9C754F).copy(alpha = 0.8f),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(ringBounds.x, ringBounds.y + strokeWidth * 1.3f),
                size = Size(ringSize.width, ringSize.height - strokeWidth * 2.6f),
                style = Stroke(width = strokeWidth * 0.55f)
            )
        } else {
            drawArc(
                color = Color(0xFFCEAD78).copy(alpha = 0.72f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = ringBounds,
                size = ringSize,
                style = Stroke(width = strokeWidth * 2.2f)
            )
        }
    }
}

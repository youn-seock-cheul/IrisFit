package com.example.myapplication

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.myapplication.ui.theme.AppColorTone
import com.example.myapplication.ui.theme.IrisBackground
import com.example.myapplication.ui.theme.IrisGreen
import com.example.myapplication.ui.theme.irisColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IrisColorSchemeTest {
    private fun contrast(a: Color, b: Color): Float {
        val x = a.luminance()
        val y = b.luminance()
        return (maxOf(x, y) + 0.05f) / (minOf(x, y) + 0.05f)
    }

    @Test fun allFiveTonesInBothModesMeetNormalTextContrast() {
        AppColorTone.entries.forEach { tone ->
            listOf(false, true).forEach { dark ->
                val colors = irisColorScheme(tone, dark)
                val pairs = listOf(
                    colors.primary to colors.onPrimary,
                    colors.primaryContainer to colors.onPrimaryContainer,
                    colors.secondaryContainer to colors.onSecondaryContainer,
                    colors.tertiaryContainer to colors.onTertiaryContainer,
                    colors.background to colors.onBackground,
                    colors.surface to colors.onSurface,
                    colors.surfaceVariant to colors.onSurfaceVariant,
                    colors.surfaceContainerHigh to colors.onSurface,
                    colors.surface to colors.primary
                )
                pairs.forEachIndexed { index, (background, foreground) ->
                    val ratio = contrast(background, foreground)
                    assertTrue("$tone dark=$dark pair=$index contrast=$ratio", ratio >= 4.5f)
                }
            }
        }
    }

    @Test fun defaultGreenIsPreservedAndBatteryTitleIsReadableInEveryStatus() {
        assertEquals(IrisGreen, irisColorScheme(AppColorTone.GREEN, false).primary)
        assertEquals(IrisBackground, irisColorScheme(AppColorTone.GREEN, false).background)
        listOf(0xFF1B3A2B, 0xFF382B14, 0xFF3D2513, 0xFF3C1F1F, 0xFF262626).forEach {
            assertTrue(contrast(Color(it), Color.White) >= 4.5f)
            assertTrue(contrast(Color(it), Color(0xFFCDD5D0)) >= 4.5f)
        }
    }
}

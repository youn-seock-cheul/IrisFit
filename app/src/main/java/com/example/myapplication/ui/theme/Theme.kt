package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val IrisDarkColorScheme = darkColorScheme(
    primary = IrisGreenDark,
    onPrimary = Color(0xFF17382D),
    primaryContainer = IrisGreenContainerDark,
    onPrimaryContainer = Color(0xFFD0E9DC),
    secondary = IrisSecondaryDark,
    onSecondary = Color(0xFF24382F),
    secondaryContainer = Color(0xFF344A40),
    onSecondaryContainer = Color(0xFFD5E6DC),
    tertiary = IrisAccentDark,
    onTertiary = Color(0xFF442B12),
    tertiaryContainer = Color(0xFF604326),
    onTertiaryContainer = Color(0xFFFFDDBF),
    background = IrisBackgroundDark,
    onBackground = IrisTextDark,
    surface = IrisSurfaceDark,
    onSurface = IrisTextDark,
    surfaceVariant = IrisSurfaceVariantDark,
    onSurfaceVariant = IrisTextVariantDark,
    outline = IrisOutlineDark,
    outlineVariant = IrisOutlineVariantDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val IrisLightColorScheme = lightColorScheme(
    primary = IrisGreen,
    onPrimary = Color.White,
    primaryContainer = IrisGreenContainer,
    onPrimaryContainer = Color(0xFF123D31),
    secondary = IrisSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDFEAE3),
    onSecondaryContainer = Color(0xFF253A30),
    tertiary = IrisAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF5E4D2),
    onTertiaryContainer = Color(0xFF38220C),
    background = IrisBackground,
    onBackground = IrisText,
    surface = IrisSurface,
    onSurface = IrisText,
    surfaceVariant = IrisSurfaceVariant,
    onSurfaceVariant = IrisTextVariant,
    outline = IrisOutline,
    outlineVariant = IrisOutlineVariant,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val LegacyDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LegacyLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val LegacyTypography = androidx.compose.material3.Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

private data class TonePalette(
    val primary: Long, val primaryDark: Long,
    val container: Long, val containerDark: Long,
    val background: Long, val backgroundDark: Long,
    val variant: Long, val variantDark: Long
)

private val tonePalettes = mapOf(
    AppColorTone.BLUE to TonePalette(0xFF315F85, 0xFFA4CBEF, 0xFFDCEBF8, 0xFF234660, 0xFFF5F8FC, 0xFF111922, 0xFFE8EFF6, 0xFF283746),
    AppColorTone.LAVENDER to TonePalette(0xFF66558B, 0xFFD2C1F0, 0xFFEEE5FA, 0xFF493C64, 0xFFF9F7FC, 0xFF1B1722, 0xFFF0EAF5, 0xFF38303F),
    AppColorTone.PEACH to TonePalette(0xFF855336, 0xFFF2BC97, 0xFFFAE5D5, 0xFF593C29, 0xFFFCF8F4, 0xFF231A15, 0xFFF5ECE4, 0xFF43342A),
    AppColorTone.GRAY to TonePalette(0xFF52616B, 0xFFBBCBD5, 0xFFE3EAF0, 0xFF35454F, 0xFFF7F8F9, 0xFF181D20, 0xFFEDF0F2, 0xFF323A40)
)

fun irisColorScheme(tone: AppColorTone, dark: Boolean): ColorScheme {
    val base = if (dark) IrisDarkColorScheme else IrisLightColorScheme
    val themed = if (tone == AppColorTone.GREEN) base else {
        val palette = tonePalettes.getValue(tone)
        val primary = Color(if (dark) palette.primaryDark else palette.primary)
        val container = Color(if (dark) palette.containerDark else palette.container)
        val background = Color(if (dark) palette.backgroundDark else palette.background)
        val variant = Color(if (dark) palette.variantDark else palette.variant)
        val foreground = if (dark) Color(0xFFEDF0F4) else Color(0xFF202225)
        val onPrimary = if (dark) Color(0xFF192024) else Color.White
        base.copy(
            primary = primary, onPrimary = onPrimary,
            primaryContainer = container, onPrimaryContainer = foreground,
            secondary = primary, onSecondary = onPrimary,
            secondaryContainer = container, onSecondaryContainer = foreground,
            tertiary = primary, onTertiary = onPrimary,
            tertiaryContainer = container, onTertiaryContainer = foreground,
            background = background, onBackground = foreground,
            surface = if (dark) lerp(background, variant, 0.4f) else Color.White,
            onSurface = foreground,
            surfaceVariant = variant,
            onSurfaceVariant = if (dark) Color(0xFFC3C8CE) else Color(0xFF4C5359),
            outline = if (dark) Color(0xFF9099A1) else Color(0xFF727B82),
            outlineVariant = if (dark) variant else lerp(variant, primary, 0.12f),
            inverseSurface = foreground,
            inverseOnSurface = background,
            inversePrimary = Color(if (dark) palette.primary else palette.primaryDark)
        )
    }
    return themed.copy(
        surfaceTint = themed.primary,
        surfaceBright = if (dark) themed.surfaceVariant else themed.surface,
        surfaceDim = if (dark) themed.background else themed.surfaceVariant,
        surfaceContainerLowest = themed.background,
        surfaceContainerLow = themed.surface,
        surfaceContainer = lerp(themed.surface, themed.surfaceVariant, 0.35f),
        surfaceContainerHigh = lerp(themed.surface, themed.surfaceVariant, 0.65f),
        surfaceContainerHighest = themed.surfaceVariant
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    colorTone: AppColorTone = AppColorTone.GREEN,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> irisColorScheme(colorTone, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = com.example.myapplication.ui.theme.Typography,
        content = content
    )
}

@Composable
fun LegacyBatteryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> LegacyDarkColorScheme
        else -> LegacyLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LegacyTypography,
        content = content
    )
}
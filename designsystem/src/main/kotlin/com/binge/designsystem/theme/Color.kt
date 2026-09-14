package com.binge.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Brand */
private val BingeAmber = Color(0xFFFFC107)
private val BingeAmberLight = Color(0xFF7B5800)
private val BingeGold = Color(0xFFFFE082)

/**
 * Tertiary accent — teal, amber's complement, so the scheme has a contrasting second hue rather
 * than the single amber family. Light tone reads on white; dark tone on dark surfaces.
 */
private val BingeTeal = Color(0xFF00796B)
private val BingeTealLight = Color(0xFF4DB6AC)

/** Dark surface ramp */
private val DarkBackground = Color(0xFF0E0E0F)
private val DarkSurface = Color(0xFF161618)
private val DarkSurfaceLowest = Color(0xFF0A0A0B)
private val DarkSurfaceLow = Color(0xFF161618)
private val DarkSurface1 = Color(0xFF1E1E22)
private val DarkSurfaceHigh = Color(0xFF28282C)
private val DarkSurfaceHighest = Color(0xFF34343A)
private val DarkOnSurface = Color(0xFFEDEDED)
private val DarkOnSurfaceVariant = Color(0xFF9C9C9C)
private val DarkOutline = Color(0x29FFFFFF)
private val DarkOutlineVariant = Color(0x14FFFFFF)

/** Light surface ramp */
private val LightBackground = Color(0xFFFFFBFF)
private val LightSurface = Color(0xFFFFFBFF)
private val LightSurfaceLowest = Color(0xFFFFFFFF)
private val LightSurfaceLow = Color(0xFFF7F4F2)
private val LightSurface1 = Color(0xFFF0EDEA)
private val LightSurfaceHigh = Color(0xFFE8E5E2)
private val LightSurfaceHighest = Color(0xFFE0DDD9)
private val LightOnSurface = Color(0xFF1C1B1F)
private val LightOnSurfaceVariant = Color(0xFF5F5E63)
private val LightOutline = Color(0x291C1B1F)
private val LightOutlineVariant = Color(0x141C1B1F)

val DarkColorScheme = darkColorScheme(
    primary = BingeAmber,
    onPrimary = Color(0xFF3E2800),
    primaryContainer = Color(0xFF593D00),
    onPrimaryContainer = Color(0xFFFFE08C),
    secondary = BingeGold,
    onSecondary = Color(0xFF3E2800),
    secondaryContainer = Color(0xFF593D00),
    onSecondaryContainer = Color(0xFFFFE08C),
    tertiary = BingeTealLight,
    onTertiary = Color(0xFF00382F),
    tertiaryContainer = Color(0xFF00504A),
    onTertiaryContainer = Color(0xFF6FF0E0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkSurfaceLowest,
    surfaceContainerLow = DarkSurfaceLow,
    surfaceContainer = DarkSurface1,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = DarkSurfaceHighest,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
)

val LightColorScheme = lightColorScheme(
    primary = BingeAmberLight,
    onPrimary = Color.White,
    primaryContainer = BingeGold,
    onPrimaryContainer = Color(0xFF3E2800),
    secondary = BingeAmberLight,
    onSecondary = Color.White,
    secondaryContainer = BingeGold,
    onSecondaryContainer = Color(0xFF3E2800),
    tertiary = BingeTeal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF6FF0E0),
    onTertiaryContainer = Color(0xFF00201C),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightSurfaceLowest,
    surfaceContainerLow = LightSurfaceLow,
    surfaceContainer = LightSurface1,
    surfaceContainerHigh = LightSurfaceHigh,
    surfaceContainerHighest = LightSurfaceHighest,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
)

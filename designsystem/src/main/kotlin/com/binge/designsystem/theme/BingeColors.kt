package com.binge.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended color palette for tokens outside Material 3's ColorScheme — accents, image-overlay
 * colors (scrims, glass), the rating-distribution bucket gradient, and the initials-avatar palette.
 *
 * `scrim` is the overlay base; call sites apply their own alpha (e.g. `scrim.copy(alpha = 0.55f)`)
 * so one gradient can reuse the same base at multiple intensities. Tokens that render on imagery
 * rather than the surface ramp are identical across light/dark; `ratingBucketGradient` is the
 * exception, tuned per theme so its red→green (bad→good) ramp reads on both `surfaceContainer`s.
 */
@Immutable
data class BingeColors(
    val ratingStar: Color,
    /**
     * Ink for text/icons *on* a [ratingStar] fill (the TV rating badge). A designed pair, not
     * `onPrimary`: `ratingStar` only happens to equal the brand accent today, so borrowing the
     * accent's ink would silently break the badge the day either hue moves.
     */
    val onRatingStar: Color,
    /** Positive-state container pair (approved/available chips) — M3 has no success slot. */
    val successContainer: Color,
    val onSuccessContainer: Color,
    /**
     * Semantic sentiment accents (see [BingeSentiment]), one theme-tuned hue per meaning:
     * [caution] amber "waiting" → [info] blue "in motion" → [positive] green "done" → [negative] red
     * "refused". [accentPurple] carries no sentiment — a raw category accent (e.g. the subtitle-issue
     * tag). Tuned per theme to read as text: darker in light mode, brighter in dark.
     */
    val caution: Color,
    val info: Color,
    val positive: Color,
    val negative: Color,
    /**
     * Saturated *fill* variants of the sentiment accents — for graphical use (chip dot, tag/row
     * icons) at WCAG 3:1, so brighter than the [caution]/[info]/[positive]/[negative] *label* tones
     * that carry text at 4.5:1. In dark mode the accents are already bright, so fill == label.
     */
    val cautionFill: Color,
    val infoFill: Color,
    val positiveFill: Color,
    val negativeFill: Color,
    val accentPurple: Color,
    /** Active "favourite" red — the conventional heart affordance, diverging from the app accent. */
    val favourite: Color,
    val scrim: Color,
    val onScrim: Color,
    val titleShadow: Color,
    val ratingBucketGradient: List<Color>,
    val avatarPalette: List<Color>,
    val avatarInk: Color,
)

private val Amber500 = Color(0xFFFFC107)

/**
 * Ink on an amber fill, shared across themes (amber is a light tone on either ramp). The near-black
 * carries a warm cast rather than flat #000 so the badge reads as part of the amber, not a hole in it.
 */
private val AmberInk = Color(0xFF3E2800)

/** Deeper, saturated tones read against the light surfaceContainer. Worst → best: red → green. */
private val LightBucketGradient = listOf(
    Color(0xFFE53935), // red 600
    Color(0xFFFB8C00), // orange 600
    Color(0xFFFDD835), // yellow 600
    Color(0xFF7CB342), // light green 600
    Color(0xFF43A047), // green 600
)

/** Lighter, slightly desaturated tones for contrast against the dark surfaceContainer. */
private val DarkBucketGradient = listOf(
    Color(0xFFEF5350), // red 400
    Color(0xFFFFA726), // orange 400
    Color(0xFFFFEE58), // yellow 400
    Color(0xFF9CCC65), // light green 400
    Color(0xFF66BB6A), // green 400
)

/**
 * Deterministic pastel tones for initials-fallback avatars, theme-agnostic by design: light enough
 * that `avatarInk` (near-black) reads on every tone in both themes — `colorScheme.onSurface` would
 * print white text on pastel pink in dark mode.
 */
private val AvatarPalette = listOf(
    Color(0xFFFF8A80),
    Color(0xFFFFB74D),
    Color(0xFFFFD54F),
    Color(0xFFAED581),
    Color(0xFF4DD0E1),
    Color(0xFF7986CB),
    Color(0xFFBA68C8),
    Color(0xFFF06292),
)
private val AvatarInk = Color(0xFF1A1A1A)

/**
 * Favourite heart red, tuned per theme so the active heart reads as "favourite red"
 * against either accent-tinted pill: deep red 700 on light, lighter red 400 on dark.
 */
private val LightFavourite = Color(0xFFD32F2F)
private val DarkFavourite = Color(0xFFFF6B6B)

val LightBingeColors = BingeColors(
    ratingStar = Amber500,
    onRatingStar = AmberInk,
    successContainer = Color(0xFFB9E5BD),
    onSuccessContainer = Color(0xFF16341C),
    caution = Color(0xFF745100),
    info = Color(0xFF0F57A8),
    positive = Color(0xFF1A6024),
    negative = Color(0xFFA81A1A),
    // Vivid fills for dots/icons (3:1) — as saturated as each hue allows while still reading on the
    // light surface; amber caps lowest, so caution's fill is a rich amber-orange rather than yellow.
    cautionFill = Color(0xFFB26A00),
    infoFill = Color(0xFF1565C0),
    positiveFill = Color(0xFF2E7D32),
    negativeFill = Color(0xFFC62828),
    accentPurple = Color(0xFF5E35B1),
    favourite = LightFavourite,
    scrim = Color.Black,
    onScrim = Color.White,
    titleShadow = Color.Black.copy(alpha = 0.4f),
    ratingBucketGradient = LightBucketGradient,
    avatarPalette = AvatarPalette,
    avatarInk = AvatarInk,
)

val DarkBingeColors = BingeColors(
    ratingStar = Amber500,
    onRatingStar = AmberInk,
    successContainer = Color(0xFF2D4F33),
    onSuccessContainer = Color(0xFFBFE9C4),
    caution = Color(0xFFF5B66B),
    info = Color(0xFF7FC2F5),
    positive = Color(0xFF56D79A),
    negative = Color(0xFFFF8A80),
    // Dark accents are already bright enough to double as dot/icon fills, so fill == label here.
    cautionFill = Color(0xFFF5B66B),
    infoFill = Color(0xFF7FC2F5),
    positiveFill = Color(0xFF56D79A),
    negativeFill = Color(0xFFFF8A80),
    accentPurple = Color(0xFFA98BFF),
    favourite = DarkFavourite,
    scrim = Color.Black,
    onScrim = Color.White,
    titleShadow = Color.Black.copy(alpha = 0.4f),
    ratingBucketGradient = DarkBucketGradient,
    avatarPalette = AvatarPalette,
    avatarInk = AvatarInk,
)

val LocalBingeColors = staticCompositionLocalOf<BingeColors> {
    error("BingeColors not provided — wrap content with BingeExpressiveTheme.")
}

object BingeTheme {
    val colors: BingeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalBingeColors.current
}

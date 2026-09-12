package com.binge.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * The app's semantic colour vocabulary: a sentiment names *what a colour means*, not a hue, so
 * status chips, category tags, and row icons all speak the same language. [Positive] (done / online),
 * [Negative] (error / declined), [Caution] (waiting / needs attention), [Info] (in motion /
 * informational), and [Neutral] (no sentiment — muted). Resolve to a saturated, theme-tuned accent
 * with [accent]; paint that accent over the same hue at low alpha for the tonal chip/tag/tile fill.
 *
 * Hues that carry no sentiment (e.g. the purple category tint) are *not* sentiments — they stay raw
 * accents on [BingeColors] (`accentPurple`) and are referenced directly.
 */
enum class BingeSentiment {
    Positive,
    Negative,
    Caution,
    Info,
    Neutral,
}

/**
 * The sentiment's *label* accent — the AA-tuned tone (4.5:1) for text: a chip/tag label, or a coloured
 * caption. Darker in light mode. For the dot/icon/fill, use [fill] (brighter); they're the same in dark.
 */
@Composable
fun BingeSentiment.accent(): Color =
    when (this) {
        BingeSentiment.Positive -> BingeTheme.colors.positive
        BingeSentiment.Negative -> BingeTheme.colors.negative
        BingeSentiment.Caution -> BingeTheme.colors.caution
        BingeSentiment.Info -> BingeTheme.colors.info
        BingeSentiment.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
    }

/**
 * The sentiment's saturated *fill* — the vivid tone for a graphical element (chip dot, tag icon, row
 * icon) that only needs 3:1, so it reads more colourfully than [accent]. Use [accent] for any text.
 */
@Composable
fun BingeSentiment.fill(): Color =
    when (this) {
        BingeSentiment.Positive -> BingeTheme.colors.positiveFill
        BingeSentiment.Negative -> BingeTheme.colors.negativeFill
        BingeSentiment.Caution -> BingeTheme.colors.cautionFill
        BingeSentiment.Info -> BingeTheme.colors.infoFill
        BingeSentiment.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
    }

/** Tonal wash alphas — paler in light mode, where accents are darker for contrast (mirrors the chips). */
private const val TONAL_CONTAINER_ALPHA_LIGHT = 0.12f
private const val TONAL_CONTAINER_ALPHA_DARK = 0.16f

/**
 * The tonal container fill for an accent — the same hue at a low, theme-tuned alpha. The faint wash a
 * sentiment accent sits on for a chip, a tag, or a row-icon box.
 */
@Composable
fun Color.tonalContainer(): Color =
    copy(alpha = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) TONAL_CONTAINER_ALPHA_DARK else TONAL_CONTAINER_ALPHA_LIGHT)

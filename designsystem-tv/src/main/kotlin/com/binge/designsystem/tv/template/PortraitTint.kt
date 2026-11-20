package com.binge.designsystem.tv.template

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import coil3.BitmapImage
import coil3.Image
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/** The leftmost share of the photo's width read for its edge colour. */
private const val EDGE_WIDTH_SHARE = 0.06f

/** Rows read start this far down: the biography and tabs sit in the lower half, so that is the edge the copy sits against. */
private const val EDGE_TOP_SHARE = 0.5f
private const val SAMPLE_COLUMNS = 8
private const val SAMPLE_ROWS = 24

/** Relative luminance the tint is darkened to at most: white copy keeps about 11:1 over it, the biography's grey about 6.8:1. */
private const val MAX_TINT_LUMINANCE = 0.04f
private const val MAX_TINT_SATURATION = 0.5f
private const val CLAMP_PASSES = 2

private const val SRGB_THRESHOLD = 0.04045f
private const val SRGB_LINEAR_DIVISOR = 12.92f
private const val SRGB_OFFSET = 0.055f
private const val SRGB_SCALE = 1.055f
private const val SRGB_GAMMA = 2.4f
private const val LINEAR_THRESHOLD = 0.0031308f
private const val CHANNEL_MAX = 255f
private const val LUMA_RED = 0.2126f
private const val LUMA_GREEN = 0.7152f
private const val LUMA_BLUE = 0.0722f
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val BYTE_MASK = 0xFF

/**
 * The wash colour for [this] photo, or null when it is not a bitmap Coil kept in software (the one this can read). Only a small
 * grid of its left edge is read, so it is cheap enough to run where the photo reports it has loaded.
 */
internal fun Image.edgeTint(): Color? {
    val bitmap = (this as? BitmapImage)?.bitmap?.takeUnless { it.config == Bitmap.Config.HARDWARE } ?: return null
    return edgeTint(bitmap.width, bitmap.height) { x, y -> bitmap.getPixel(x, y) }
}

/**
 * The darkened average of the photo's left edge over its lower half, from pixels read through [argbAt], or null for an empty
 * image. The average is taken in linear light, so a bright strip beside a dark one does not come out muddier than it looks.
 */
internal fun edgeTint(
    width: Int,
    height: Int,
    argbAt: (x: Int, y: Int) -> Int,
): Color? {
    if (width <= 0 || height <= 0) return null
    val columnSpan = max(1, (width * EDGE_WIDTH_SHARE).toInt())
    val top = (height * EDGE_TOP_SHARE).toInt()
    val rowSpan = max(1, height - top)
    var red = 0f
    var green = 0f
    var blue = 0f
    for (column in 0 until SAMPLE_COLUMNS) {
        val x = min(width - 1, column * columnSpan / SAMPLE_COLUMNS)
        for (row in 0 until SAMPLE_ROWS) {
            val y = min(height - 1, top + row * rowSpan / SAMPLE_ROWS)
            val argb = argbAt(x, y)
            red += toLinear(((argb shr RED_SHIFT) and BYTE_MASK) / CHANNEL_MAX)
            green += toLinear(((argb shr GREEN_SHIFT) and BYTE_MASK) / CHANNEL_MAX)
            blue += toLinear((argb and BYTE_MASK) / CHANNEL_MAX)
        }
    }
    val count = (SAMPLE_COLUMNS * SAMPLE_ROWS).toFloat()
    return clampTint(Color(fromLinear(red / count), fromLinear(green / count), fromLinear(blue / count)))
}

/**
 * [color] held to [MAX_TINT_SATURATION] and darkened to [MAX_TINT_LUMINANCE], hue kept, so copy stays readable over any photo.
 * Each step slightly undoes the other (sRGB is not a pure power law), so they run twice, which lands inside both bounds.
 */
internal fun clampTint(color: Color): Color = (0 until CLAMP_PASSES).fold(color) { current, _ -> darken(limitSaturation(current)) }

private fun limitSaturation(color: Color): Color {
    val peak = max(color.red, max(color.green, color.blue))
    val saturation = saturationOf(color)
    if (saturation <= MAX_TINT_SATURATION) return color
    val keep = MAX_TINT_SATURATION / saturation
    return Color(peak - (peak - color.red) * keep, peak - (peak - color.green) * keep, peak - (peak - color.blue) * keep)
}

private fun darken(color: Color): Color {
    val luminance = luminanceOf(color)
    if (luminance <= MAX_TINT_LUMINANCE) return color
    val scale = MAX_TINT_LUMINANCE / luminance
    return Color(
        fromLinear(toLinear(color.red) * scale),
        fromLinear(toLinear(color.green) * scale),
        fromLinear(toLinear(color.blue) * scale),
    )
}

/** The relative luminance of [color], for the bounds the clamp promises. */
internal fun luminanceOf(color: Color): Float =
    LUMA_RED * toLinear(color.red) + LUMA_GREEN * toLinear(color.green) + LUMA_BLUE * toLinear(color.blue)

/** The HSV saturation of [color]. */
internal fun saturationOf(color: Color): Float {
    val peak = max(color.red, max(color.green, color.blue))
    return if (peak <= 0f) 0f else (peak - min(color.red, min(color.green, color.blue))) / peak
}

private fun toLinear(channel: Float): Float =
    if (channel <= SRGB_THRESHOLD) channel / SRGB_LINEAR_DIVISOR else ((channel + SRGB_OFFSET) / SRGB_SCALE).pow(SRGB_GAMMA)

private fun fromLinear(linear: Float): Float =
    (if (linear <= LINEAR_THRESHOLD) linear * SRGB_LINEAR_DIVISOR else SRGB_SCALE * linear.pow(1f / SRGB_GAMMA) - SRGB_OFFSET)
        .coerceIn(0f, 1f)

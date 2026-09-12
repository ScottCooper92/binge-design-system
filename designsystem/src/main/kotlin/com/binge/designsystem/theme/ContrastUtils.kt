package com.binge.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/** WCAG AA minimum contrast ratio for normal-size body text (4.5:1). */
const val WCAG_CONTRAST_NORMAL = 4.5

/**
 * WCAG contrast ratio between this colour and [against], from 1.0 (identical) to 21.0
 * (black vs white). Order-independent. Alpha is ignored — both colours are treated as opaque,
 * matching [ColorUtils.calculateContrast]'s requirement that the background be opaque.
 */
fun Color.contrastRatio(against: Color): Double = ColorUtils.calculateContrast(toArgb(), against.toArgb())

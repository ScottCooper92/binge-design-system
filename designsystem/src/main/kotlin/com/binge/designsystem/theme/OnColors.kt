package com.binge.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import me.tatarka.google.material.contrast.Contrast
import me.tatarka.google.material.hct.Hct

/**
 * Returns [preferred] when it already clears [minRatio] contrast against [background], otherwise a
 * derived neutral ink that does — so a hand-picked on-colour is honoured wherever it's already
 * legible, and only replaced when the background drifts out from under it.
 *
 * The safety floor for Binge's bespoke pairings: surfaces like the pastel avatar palette pair text
 * with a colour that isn't an M3 slot, and under dynamic colour the background isn't known at design
 * time, so the guarantee must be derived. Passing the design's [preferred] ink keeps the current look
 * pixel-identical while guaranteeing legibility if a tone ever drifts below the target.
 *
 * @param minRatio target WCAG contrast ratio; defaults to [WCAG_CONTRAST_NORMAL] (4.5:1, body text).
 */
fun contrastSafeOn(
    background: Color,
    preferred: Color,
    minRatio: Double = WCAG_CONTRAST_NORMAL,
): Color = if (preferred.contrastRatio(background) >= minRatio) preferred else contrastSafeOn(background, minRatio)

/**
 * Derives a legible foreground ("on") colour for [background] guaranteed to meet [minRatio] — a
 * neutral ink/paper tone (near-black or near-white) on the background's own hue.
 *
 * Built on `material-color-utilities` (the HCT / [Contrast] engine behind Material You): the
 * background maps to its HCT tone, and the foreground tone is lightened or darkened until it clears
 * [minRatio]. Where the exact ratio is unreachable (a mid-tone background), it falls back to the
 * maximal contrast available.
 *
 * Prefer the [preferred]-aware overload for design surfaces with a chosen ink; this raw form is for
 * sites with no preferred on-colour at all.
 *
 * @param minRatio target WCAG contrast ratio; defaults to [WCAG_CONTRAST_NORMAL] (4.5:1, body text).
 */
fun contrastSafeOn(background: Color, minRatio: Double = WCAG_CONTRAST_NORMAL): Color {
    val backgroundHct = Hct.fromInt(background.toArgb())
    val onTone = contrastSafeOnTone(backgroundHct.tone, minRatio)
    return Color(Hct.from(backgroundHct.hue, NEUTRAL_INK_CHROMA, onTone).toInt())
}

/**
 * Pure tone arithmetic behind [contrastSafeOn]: given a [backgroundTone] (HCT L*, 0..100), returns
 * the foreground tone that clears [minRatio] contrast against it.
 *
 * Lightens over dark backgrounds, darkens over light. The `*Unsafe` variants clamp to tone 0/100
 * instead of returning -1 when the exact ratio is unreachable, so a mid-tone background still yields
 * the highest-contrast tone available. Pure `material-color-utilities` (no [Color.toArgb]), so it's
 * unit-testable without the Android colour engine.
 */
internal fun contrastSafeOnTone(backgroundTone: Double, minRatio: Double): Double =
    if (backgroundTone < MID_TONE) {
        val lighter = Contrast.lighter(backgroundTone, minRatio)
        if (lighter >= 0) lighter else Contrast.lighterUnsafe(backgroundTone, minRatio)
    } else {
        val darker = Contrast.darker(backgroundTone, minRatio)
        if (darker >= 0) darker else Contrast.darkerUnsafe(backgroundTone, minRatio)
    }

/** HCT tone midpoint: below it a background is "dark" (lighten the ink), above it "light" (darken it). */
private const val MID_TONE = 50.0

/** Chroma for the derived ink: 0 keeps it a neutral grey so it reads as ink/paper, not a tinted hue. */
private const val NEUTRAL_INK_CHROMA = 0.0

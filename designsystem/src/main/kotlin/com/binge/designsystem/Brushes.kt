package com.binge.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * A horizontal gradient read from the layout's **start** edge rather than the screen's left — the shape
 * every side scrim in the design system has (dense at the edge, a held middle, then clear).
 *
 * [Brush.horizontalGradient] draws in absolute coordinates: stop `0f` is the physical left whatever the
 * layout direction. These scrims exist to darken behind start-aligned content — the expanded rail's
 * glyphs, the hero copy, the cinematic header's poster — and all of that content *does* mirror, so under
 * RTL an absolute ramp lands its dense end on the side facing the content and its clear end under the
 * thing it was meant to protect. `supportsRtl` is set in the manifest, so this applies to any RTL system
 * language, shipped RTL strings or not.
 *
 * Variadic rather than three fixed stops, which is what this took while the phone was its only caller: the
 * TV scrims it gained in #2108 ramp over five, and an arity that fits some callers and not others is how a
 * second, absolute copy gets written for the ones it does not fit.
 */
@Composable
fun startHorizontalGradient(vararg colorStops: Pair<Float, Color>): Brush =
    startHorizontalGradient(LocalLayoutDirection.current, *colorStops)

fun startHorizontalGradient(layoutDirection: LayoutDirection, vararg colorStops: Pair<Float, Color>): Brush =
    when (layoutDirection) {
        LayoutDirection.Ltr -> Brush.horizontalGradient(colorStops = colorStops)
        // Reversed as well as remapped: horizontalGradient wants ascending stops, and 1f - stop
        // inverts their order.
        LayoutDirection.Rtl ->
            Brush.horizontalGradient(
                colorStops = Array(colorStops.size) { colorStops[colorStops.lastIndex - it].mirrored() },
            )
    }

private fun Pair<Float, Color>.mirrored(): Pair<Float, Color> = (1f - first) to second

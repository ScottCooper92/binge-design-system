package com.binge.designsystem.tv.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.R as TvR

private const val SPINNER_SWEEP_DEGREES = 270f
private const val SPINNER_START_DEGREES = -90f
private const val SPINNER_PERIOD_MS = 1_000
private const val FULL_TURN_DEGREES = 360f

/**
 * The indeterminate spinner a loading [TvButton] or [TvIconButton] draws in its icon's box, at its content colour.
 *
 * tv-material ships no progress indicator, and the Material 3 one is on the other side of the seam, so the arc is
 * drawn here. It carries no semantics of its own: the button it sits in says it is busy, on the node that carries its
 * name, so a screen reader hears one control rather than a button and a separate progress bar. Under [LocalReduceMotion] the arc holds still: it still reads as busy, and a baseline stays stable.
 */
@Composable
internal fun TvButtonSpinner(color: Color, modifier: Modifier = Modifier) {
    val stroke = dimensionResource(TvR.dimen.tv_button_spinner_stroke)
    val rotation = spinnerRotation()
    Canvas(modifier.size(dimensionResource(TvR.dimen.tv_button_icon))) {
        val strokePx = stroke.toPx()
        val inset = strokePx / 2
        drawArc(
            color = color,
            startAngle = SPINNER_START_DEGREES + rotation,
            sweepAngle = SPINNER_SWEEP_DEGREES,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(size.width - strokePx, size.height - strokePx),
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun spinnerRotation(): Float {
    if (LocalReduceMotion.current) return 0f
    val transition = rememberInfiniteTransition(label = "tv-button-spinner")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_TURN_DEGREES,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SPINNER_PERIOD_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "tv-button-spinner-rotation",
    )
    return rotation
}

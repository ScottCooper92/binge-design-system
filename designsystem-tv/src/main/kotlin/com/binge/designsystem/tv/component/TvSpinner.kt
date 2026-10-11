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
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.theme.LocalReduceMotion

private const val SPINNER_SWEEP_DEGREES = 270f
private const val SPINNER_START_DEGREES = -90f
private const val SPINNER_PERIOD_MS = 1_000
private const val FULL_TURN_DEGREES = 360f

/**
 * The indeterminate spinner a busy control draws in place of its icon or trailing mark, at its content colour: a
 * loading [TvButton] or [TvIconButton], or a [com.binge.designsystem.tv.template.TvPaneRow] whose value is loading.
 *
 * tv-material ships no progress indicator, and the Material 3 one is on the other side of the seam, so the arc is
 * drawn here. It carries no semantics of its own: the control it sits in says it is busy, on the node that carries its
 * name, so a screen reader hears one control rather than a second stop. Under [LocalReduceMotion] the arc holds still.
 */
@Composable
internal fun TvSpinner(
    color: Color,
    diameter: Dp,
    stroke: Dp,
    modifier: Modifier = Modifier,
) {
    val rotation = spinnerRotation()
    Canvas(modifier.size(diameter)) {
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
    val transition = rememberInfiniteTransition(label = "tv-spinner")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_TURN_DEGREES,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SPINNER_PERIOD_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "tv-spinner-rotation",
    )
    return rotation
}

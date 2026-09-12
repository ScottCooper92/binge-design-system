package com.binge.designsystem.modifier

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.theme.LocalReduceMotion

private const val DEFAULT_SELECTED_SCALE = 0.985f

/**
 * Tactile "press-in" affordance for selectable surfaces — scales the receiver down
 * slightly and lifts a soft shadow when [selected] is true. Reduce-motion safe
 * (snaps instantly under [LocalReduceMotion]).
 *
 * Apply before [Modifier.clip] so the shadow draws outside the clipped content.
 * Border + selectable/toggleable semantics are the caller's concern.
 */
@Composable
fun Modifier.selectionLift(
    selected: Boolean,
    shape: Shape,
    restingElevation: Dp,
    liftedElevation: Dp,
    selectedScale: Float = DEFAULT_SELECTED_SCALE,
): Modifier {
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (selected) selectedScale else 1f,
        animationSpec = if (reduceMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow),
        label = "selection-lift-scale",
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.shadow(if (selected) liftedElevation else restingElevation, shape, clip = false)
}

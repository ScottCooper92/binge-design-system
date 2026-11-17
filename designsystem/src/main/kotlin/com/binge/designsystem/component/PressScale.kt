package com.binge.designsystem.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.binge.designsystem.theme.LocalReduceMotion

/**
 * The press feedback a poster-sized tile gives: it scales down a little while [interactionSource] is pressed and springs back. Under
 * reduced motion ([LocalReduceMotion]) it does not move at all.
 *
 * [MediaCard] and the category tiles ([BackdropTile], [LogoTile]) share it, so a row of both feels alike and a change to the scale or the
 * spring moves them together. The scale is read when the layer draws, so a press redraws the tile without recomposing it.
 *
 * Put it before the clip and the click handler, as [MediaCard] does, so the whole tile scales and the press feeds the same
 * [interactionSource] the click handler is given.
 */
@Composable
internal fun Modifier.pressScale(interactionSource: InteractionSource): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !reduceMotion) PRESSED_SCALE else 1f,
        animationSpec = if (reduceMotion) {
            snap()
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        },
        label = "press-scale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

private const val PRESSED_SCALE = 0.95f

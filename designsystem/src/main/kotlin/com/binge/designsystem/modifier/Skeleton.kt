package com.binge.designsystem.modifier

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.LocalReduceMotion

private const val SHIMMER_PERIOD_MS = 1200
private const val SHIMMER_TRAVEL = 2f
private const val SHIMMER_FROZEN_PROGRESS = 0.5f
private const val SHIMMER_HIGHLIGHT_ALPHA = 0.35f

/**
 * Paints the receiver's own bounds as a shimmering loading placeholder when [visible], masking wrapped
 * children; a no-op passthrough otherwise. A Loading state renders its *real* content with
 * `Modifier.skeleton(isLoading)`, so the skeleton is the layout and can't drift from it.
 *
 * A `surfaceVariant` base with a lighter highlight sweeping across; the sweep freezes at a fixed offset
 * under [LocalReduceMotion] so screenshot baselines stay deterministic. [shape] clips the placeholder,
 * defaulting to the media-card radius (the common poster/card case).
 */
@Composable
fun Modifier.skeleton(visible: Boolean, shape: Shape = BingeShapes.MediaCard): Modifier {
    if (!visible) return this

    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface.copy(alpha = SHIMMER_HIGHLIGHT_ALPHA)
    val progress = shimmerProgress()

    return this
        .clip(shape)
        .drawWithContent {
            drawRect(base)
            val sweep = size.width * progress
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, highlight, Color.Transparent),
                    start = Offset(sweep - size.width, 0f),
                    end = Offset(sweep, 0f),
                ),
            )
        }
}

/**
 * Drives the highlight sweep. Under [LocalReduceMotion] it returns a fixed progress so a single
 * captured frame is stable; otherwise it loops across the bounds on [SHIMMER_PERIOD_MS].
 */
@Composable
private fun shimmerProgress(): Float {
    if (LocalReduceMotion.current) return SHIMMER_FROZEN_PROGRESS
    val transition = rememberInfiniteTransition(label = "skeleton-shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = SHIMMER_TRAVEL,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_PERIOD_MS),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeleton-shimmer-progress",
    )
    return progress
}

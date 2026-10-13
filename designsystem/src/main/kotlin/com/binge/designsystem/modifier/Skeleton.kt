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

/** How opaque the sweep's highlight is over its surface colour; [skeleton] and a TV caller of [shimmerSkeleton] share it. */
const val SKELETON_HIGHLIGHT_ALPHA = 0.35f

/**
 * Paints the receiver's own bounds as a shimmering loading placeholder when [visible], masking wrapped
 * children; a no-op passthrough otherwise. A Loading state renders its *real* content with
 * `Modifier.skeleton(isLoading)`, so the skeleton is the layout and can't drift from it.
 *
 * The phone wrapper over [shimmerSkeleton]: a Material 3 `surfaceVariant` base with a lighter `surface`
 * highlight. [shape] clips the placeholder, defaulting to the media-card radius. TV code calls
 * [shimmerSkeleton] with its own scheme instead, because it has no Material 3 theme to read.
 */
@Composable
fun Modifier.skeleton(visible: Boolean, shape: Shape = BingeShapes.MediaCard): Modifier =
    shimmerSkeleton(
        visible = visible,
        base = MaterialTheme.colorScheme.surfaceVariant,
        highlight = MaterialTheme.colorScheme.surface.copy(alpha = SKELETON_HIGHLIGHT_ALPHA),
        shape = shape,
    )

/**
 * [skeleton] without a theme: a [base] fill with a [highlight] sweeping across it, clipped to [shape], when [visible].
 * The colours are required, so a caller outside Material 3, such as the TV module, cannot fall back to its baseline
 * scheme by leaving them out. The sweep freezes at a fixed offset under [LocalReduceMotion], so screenshot
 * baselines stay deterministic.
 */
@Composable
fun Modifier.shimmerSkeleton(
    visible: Boolean,
    base: Color,
    highlight: Color,
    shape: Shape = BingeShapes.MediaCard,
): Modifier {
    if (!visible) return this

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

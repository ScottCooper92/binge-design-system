package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.modifier.SKELETON_HIGHLIGHT_ALPHA
import com.binge.designsystem.modifier.shimmerSkeleton
import com.binge.designsystem.theme.BingeShapes

/**
 * One shimmering placeholder: a bar for a line of text, a plate for a poster. The caller sizes it. Never focusable,
 * so a skeleton never gives the D-pad somewhere to stop that disappears when the content arrives. Painted from the
 * tv-material scheme through the theme-free [shimmerSkeleton], so an app's brand passed to `BingeTvTheme` reaches it.
 */
@Composable
internal fun TvSkeletonBlock(modifier: Modifier = Modifier, shape: Shape = BingeShapes.ElementSmall) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier.shimmerSkeleton(
            visible = true,
            shape = shape,
            base = scheme.surfaceVariant,
            highlight = scheme.surface.copy(alpha = SKELETON_HIGHLIGHT_ALPHA),
        ),
    )
}

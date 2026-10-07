package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.modifier.skeleton
import com.binge.designsystem.theme.BingeShapes

private const val HIGHLIGHT_ALPHA = 0.35f

/**
 * One shimmering placeholder: a bar for a line of text, a plate for a poster. The caller sizes it. Never focusable,
 * so a skeleton never gives the D-pad somewhere to stop that disappears when the content arrives. Painted from the
 * tv-material scheme, so an app's brand passed to `BingeTvTheme` reaches it.
 */
@Composable
fun TvSkeletonBlock(modifier: Modifier = Modifier, shape: Shape = BingeShapes.ElementSmall) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier.skeleton(
            visible = true,
            shape = shape,
            base = scheme.surfaceVariant,
            highlight = scheme.surface.copy(alpha = HIGHLIGHT_ALPHA),
        ),
    )
}

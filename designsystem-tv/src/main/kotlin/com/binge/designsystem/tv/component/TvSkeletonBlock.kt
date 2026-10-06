package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.binge.designsystem.modifier.skeleton
import com.binge.designsystem.theme.BingeShapes

/**
 * One shimmering placeholder: a bar for a line of text, a plate for a poster. The caller sizes it. Never focusable,
 * so a skeleton never gives the D-pad somewhere to stop that disappears when the content arrives.
 */
@Composable
fun TvSkeletonBlock(modifier: Modifier = Modifier, shape: Shape = BingeShapes.ElementSmall) {
    Box(modifier = modifier.skeleton(visible = true, shape = shape))
}

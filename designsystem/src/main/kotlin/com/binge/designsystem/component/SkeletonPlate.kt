package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.binge.designsystem.modifier.skeleton
import com.binge.designsystem.theme.BingeShapes

/**
 * One shimmering rounded block, sized by its caller: the plate a hand-built loading skeleton is made of, standing in
 * for a line of text, a poster or a chip. [shape] clips it, defaulting to the small element radius. A skeleton that
 * mirrors real content renders that content with `Modifier.skeleton` instead; this is for one built from plates.
 */
@Composable
fun SkeletonPlate(modifier: Modifier = Modifier, shape: Shape = BingeShapes.ElementSmall) {
    Box(modifier.skeleton(visible = true, shape = shape))
}

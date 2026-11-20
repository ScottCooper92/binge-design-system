package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.theme.BingeShapes

/**
 * One line of copy, as a loading skeleton draws it: a plate of [widthFraction] × [height]. [color] is for a plate on a card.
 */
@Composable
fun TvSkeletonPlate(
    widthFraction: Float,
    height: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = BingeShapes.Tag,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(shape)
            .background(color),
    )
}

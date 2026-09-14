package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.R as DesR

/**
 * A hairline vertical divider between two side-by-side TV panes.
 *
 * Fills the height it is given; the caller insets it — to the overscan-safe area, say — through [modifier].
 */
@Composable
fun TvVerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(dimensionResource(DesR.dimen.hairline_thickness))
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.border),
    )
}

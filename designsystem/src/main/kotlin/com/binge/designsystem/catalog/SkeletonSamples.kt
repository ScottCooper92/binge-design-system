package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.R
import com.binge.designsystem.modifier.skeleton
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeShapes

/**
 * The skeleton modifier over the shapes it most often clips: a poster card, a text bar and a pill. Under
 * [ScreenshotTheme] motion is reduced, so the shimmer holds still at a fixed offset.
 */
@Composable
fun SkeletonSample() {
    ScreenshotTheme {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
        ) {
            Box(
                Modifier
                    .width(dimensionResource(R.dimen.card_width))
                    .aspectRatio(CARD_ASPECT_RATIO)
                    .skeleton(visible = true),
            )
            Box(
                Modifier
                    .width(dimensionResource(R.dimen.skeleton_header_width))
                    .height(dimensionResource(R.dimen.skeleton_header_height))
                    .skeleton(visible = true, shape = BingeShapes.ElementSmall),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.detail_action_button_size))
                    .skeleton(visible = true, shape = BingeShapes.Pill),
            )
        }
    }
}

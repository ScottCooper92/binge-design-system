@file:CatalogGroup("Media carousel")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.R
import com.binge.designsystem.component.CarouselSkeleton
import com.binge.designsystem.component.SkeletonPlate
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeShapes

/** The media carousel while its titles load: a row of poster placeholders under a title bar. */
@Composable
fun CarouselSkeletonSample() {
    ScreenshotTheme {
        CarouselSkeleton()
    }
}

/** Skeleton plates, the blocks a hand-built loading skeleton is made of: a line, a short line and a rounded poster. */
@Composable
fun SkeletonPlateSample() {
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            SkeletonPlate(Modifier.fillMaxWidth().height(dimensionResource(R.dimen.skeleton_header_height)))
            SkeletonPlate(
                Modifier.width(dimensionResource(R.dimen.skeleton_header_width)).height(dimensionResource(R.dimen.skeleton_header_height)),
            )
            SkeletonPlate(
                Modifier.width(dimensionResource(R.dimen.card_width)).aspectRatio(CARD_ASPECT_RATIO),
                shape = BingeShapes.MediaCard,
            )
        }
    }
}

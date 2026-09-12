package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.SeeAllTile
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [SeeAllTile] (group `"Tiles"`) — see the convention on [MediaCardRatedSample].
 * The trailing "see all" tile is sized like a poster cell, so each sample pins a poster width and 2:3
 * aspect ratio rather than stretching to the canvas.
 */
@Composable
fun SeeAllTileSample() {
    ScreenshotTheme {
        SeeAllTile(
            onClick = {},
            modifier = Modifier
                .width(dimensionResource(R.dimen.see_all_tile_width))
                .aspectRatio(POSTER_ASPECT_RATIO),
        )
    }
}

/** Custom-label form — the trailing tile can name its destination. */
@Composable
fun SeeAllTileLabelledSample() {
    ScreenshotTheme {
        SeeAllTile(
            label = "Discover more",
            onClick = {},
            modifier = Modifier
                .width(dimensionResource(R.dimen.see_all_tile_width))
                .aspectRatio(POSTER_ASPECT_RATIO),
        )
    }
}

private const val POSTER_ASPECT_RATIO = 2f / 3f

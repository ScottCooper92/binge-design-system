package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.WatchProviderTile
import com.binge.designsystem.component.WatchProviderUi
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [WatchProviderTile] (group `"Tiles"`) — see the convention on
 * [MediaCardRatedSample]. The tile is a square grid cell, so each sample pins its width rather than
 * stretching to the canvas; with no network image the logo falls back to the provider name, keeping
 * the render deterministic.
 */
@Composable
fun WatchProviderTileSelectedSample() {
    ScreenshotTheme {
        WatchProviderTile(
            provider = WatchProviderUi(id = 1, name = "Netflix", logoUrl = ""),
            selected = true,
            onToggle = {},
            modifier = Modifier.width(dimensionResource(R.dimen.see_all_tile_width)),
        )
    }
}

/** Unselected — no border or check badge; the provider name fills the cell. */
@Composable
fun WatchProviderTileUnselectedSample() {
    ScreenshotTheme {
        WatchProviderTile(
            provider = WatchProviderUi(id = 2, name = "Disney+", logoUrl = ""),
            selected = false,
            onToggle = {},
            modifier = Modifier.width(dimensionResource(R.dimen.see_all_tile_width)),
        )
    }
}

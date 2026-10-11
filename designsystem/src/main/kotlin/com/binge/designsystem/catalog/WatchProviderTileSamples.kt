package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.WatchProviderTile
import com.binge.designsystem.component.WatchProviderUi
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [WatchProviderTile] — see the convention on
 * [MediaCardRatedSample]. The tile is a square grid cell, so each sample pins its width rather than
 * stretching to the canvas; with no network image the logo falls back to the provider name, keeping
 * the render deterministic. Tapping a tile toggles it, starting from the state the frame shows.
 */
@Composable
fun WatchProviderTileSelectedSample() {
    var selected by rememberSaveable { mutableStateOf(true) }
    ScreenshotTheme {
        WatchProviderTile(
            provider = WatchProviderUi(id = 1, name = "Netflix", logoUrl = ""),
            selected = selected,
            onToggle = { selected = !selected },
            modifier = Modifier.width(dimensionResource(R.dimen.provider_tile_sample_width)),
        )
    }
}

/** Unselected — no border or check badge; the provider name fills the cell. */
@Composable
fun WatchProviderTileUnselectedSample() {
    var selected by rememberSaveable { mutableStateOf(false) }
    ScreenshotTheme {
        WatchProviderTile(
            provider = WatchProviderUi(id = 2, name = "Disney+", logoUrl = ""),
            selected = selected,
            onToggle = { selected = !selected },
            modifier = Modifier.width(dimensionResource(R.dimen.provider_tile_sample_width)),
        )
    }
}

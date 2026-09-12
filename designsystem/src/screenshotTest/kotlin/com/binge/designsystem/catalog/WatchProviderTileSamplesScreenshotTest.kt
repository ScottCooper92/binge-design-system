package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [WatchProviderTile][com.binge.designsystem.component.WatchProviderTile] catalog samples (#750). */
class WatchProviderTileSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Selected() {
        WatchProviderTileSelectedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Unselected() {
        WatchProviderTileUnselectedSample()
    }
}

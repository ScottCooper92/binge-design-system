package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** Screenshot coverage for the immersive hub — at rest with and without a hero, with a row anchored — and its see-all tile. */
class TvImmersiveSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun hub() = Frame { TvImmersiveHubSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun hubHero() = Frame { TvImmersiveHubHeroSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun hubFocused() = Frame { TvImmersiveHubFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun hubEmptyRow() = Frame { TvImmersiveHubEmptyRowSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun seeAllTile() = Frame { TvSeeAllTileSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

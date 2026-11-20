package com.binge.designsystem.tv.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviewsOnBlack
import com.binge.designsystem.tv.preview.TvPreviewsOnBlackMonochrome

/**
 * The same sample through each on-black canvas, so the style the canvas bakes in shows: focus in the accent under
 * [TvPreviewsOnBlack] and in white under [TvPreviewsOnBlackMonochrome]. Black is the window's, in both.
 */
class TvPreviewOnBlackStyleScreenshotTest {
    @PreviewTest
    @TvPreviewsOnBlack
    @Composable
    fun sideSheetAccent() {
        TvSideSheetSample()
    }

    @PreviewTest
    @TvPreviewsOnBlackMonochrome
    @Composable
    fun sideSheetMonochrome() {
        TvSideSheetSample()
    }
}

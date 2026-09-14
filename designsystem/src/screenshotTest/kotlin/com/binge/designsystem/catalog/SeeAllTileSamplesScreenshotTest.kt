package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [SeeAllTile][com.binge.designsystem.component.SeeAllTile] catalog samples. */
class SeeAllTileSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Default() {
        SeeAllTileSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Labelled() {
        SeeAllTileLabelledSample()
    }
}

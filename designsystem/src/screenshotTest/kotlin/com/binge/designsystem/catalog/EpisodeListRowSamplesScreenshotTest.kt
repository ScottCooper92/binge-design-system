package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [EpisodeListRow] catalog samples (#751). */
class EpisodeListRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ready() {
        EpisodeListRowSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun skeleton() {
        EpisodeListRowSkeletonSample()
    }
}

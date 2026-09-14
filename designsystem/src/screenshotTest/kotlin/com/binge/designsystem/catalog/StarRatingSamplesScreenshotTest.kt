package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [StarRating][com.binge.designsystem.component.StarRating] catalog samples. */
class StarRatingSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Display() {
        StarRatingDisplaySample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Interactive() {
        StarRatingInteractiveSample()
    }
}

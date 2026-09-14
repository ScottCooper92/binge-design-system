package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [RatingCard][com.binge.designsystem.component.RatingCard] catalog samples. */
class RatingCardSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Unrated() {
        RatingCardUnratedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Rated() {
        RatingCardRatedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun SignedOut() {
        RatingCardSignedOutSample()
    }
}

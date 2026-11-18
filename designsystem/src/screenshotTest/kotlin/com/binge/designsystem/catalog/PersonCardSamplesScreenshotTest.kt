package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the person-card catalog samples: the tile, its long-text and missing-content variants, the row that hosts it, and its skeleton. */
class PersonCardSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Default() {
        PersonCardSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun LongName() {
        PersonCardLongNameSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun LongSubtitle() {
        PersonCardLongSubtitleSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun NoPhoto() {
        PersonCardNoPhotoSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun NoSubtitle() {
        PersonCardNoSubtitleSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Row() {
        PersonCardRowSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Skeleton() {
        PersonCardSkeletonSample()
    }
}

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.TallWorstCaseFontPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for [com.binge.designsystem.component.PersonProfileHeader] and its skeleton. */
class PersonProfileSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun header() = PersonProfileHeaderSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun headerLong() = PersonProfileHeaderLongSample()

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun skeleton() = PersonProfileSkeletonSample()

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun skeletonHinted() = PersonProfileSkeletonHintedSample()

    @PreviewTest
    @TallWorstCaseFontPreviews
    @Composable
    fun skeletonHintedLargeFont() = PersonProfileSkeletonHintedSample()
}

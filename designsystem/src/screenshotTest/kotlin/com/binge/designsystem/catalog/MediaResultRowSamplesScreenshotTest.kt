package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for [com.binge.designsystem.component.MediaResultRow]: each kind, and the long case at 1.3x and 2x font. */
class MediaResultRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun poster() = MediaResultRowPosterSample()

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun avatar() = MediaResultRowAvatarSample()

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun logo() = MediaResultRowLogoSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun still() = MediaResultRowStillSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun longText() = MediaResultRowLongTextSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun marks() = MediaResultRowMarksSample()

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun skeleton() = MediaResultRowSkeletonSample()
}

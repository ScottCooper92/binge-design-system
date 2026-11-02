package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.FontScalePreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for the [ServerCardAllChipsSample] family: light and dark, and the worst case at 1.3x and 2x font. */
class ServerCardSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @FontScalePreviews
    @Composable
    fun AllChips() {
        ServerCardAllChipsSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun Plain() {
        ServerCardPlainSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @FontScalePreviews
    @Composable
    fun Long() {
        ServerCardLongSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun ManyChips() {
        ServerCardManyChipsSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun LoadingStats() {
        ServerCardLoadingStatsSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Bare() {
        ServerCardBareSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Hub() {
        HubCardSample()
    }
}

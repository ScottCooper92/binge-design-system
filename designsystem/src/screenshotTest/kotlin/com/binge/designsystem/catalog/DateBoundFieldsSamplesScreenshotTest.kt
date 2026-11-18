package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for the From/To date bound fields: each state, light and dark, and the long labels at the worst-case font scales. */
class DateBoundFieldsSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun Empty() {
        DateBoundFieldsEmptySample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun OneSet() {
        DateBoundFieldsOneSetSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun BothSet() {
        DateBoundFieldsBothSetSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun LongLabels() {
        DateBoundFieldsLongLabelsSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun LongLabelsEmpty() {
        DateBoundFieldsLongLabelsEmptySample()
    }
}

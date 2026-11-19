package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.LocalePreviews
import com.binge.designsystem.preview.TabletScreenPreview
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for the [DetailInfoGrid][com.binge.designsystem.component.DetailInfoGrid] samples. */
class DetailInfoGridSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun Phone() {
        DetailInfoGridSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun LongWord() {
        DetailInfoGridLongWordSample()
    }

    @PreviewTest
    @TabletScreenPreview
    @Composable
    fun Tablet() {
        DetailInfoGridSample()
    }

    @PreviewTest
    @LocalePreviews
    @Composable
    fun Spanish() {
        DetailInfoGridSpanishSample()
    }

    @PreviewTest
    @TabletScreenPreview
    @Composable
    fun SpanishTablet() {
        DetailInfoGridSpanishSample()
    }
}

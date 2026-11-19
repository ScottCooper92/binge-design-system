package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for [LabelledIconButton]: a row of three in English and in Spanish, light and dark and at the worst-case font scales, and the disabled row. */
class LabelledIconButtonSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun Row() {
        LabelledIconButtonRowSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun SpanishRow() {
        LabelledIconButtonSpanishRowSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Disabled() {
        LabelledIconButtonDisabledSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun LongWord() {
        LabelledIconButtonLongWordSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun SpanishLongWord() {
        LabelledIconButtonSpanishLongWordSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun TrailingMark() {
        LabelledIconButtonTrailingMarkSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun SpanishTrailingMark() {
        LabelledIconButtonSpanishTrailingMarkSample()
    }
}

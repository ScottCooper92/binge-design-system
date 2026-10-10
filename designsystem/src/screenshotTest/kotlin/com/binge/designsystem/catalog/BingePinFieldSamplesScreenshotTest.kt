package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.LargestFontPreview

/** Screenshot coverage for the PIN field's catalog samples: part-typed, marked after a wrong PIN, and at the largest font size. */
class BingePinFieldSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Typing() {
        BingePinFieldSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Wrong() {
        BingePinFieldErrorSample()
    }

    @PreviewTest
    @LargestFontPreview
    @Composable
    fun TypingLargestFont() {
        BingePinFieldSample()
    }
}

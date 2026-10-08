package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the PIN field's catalog samples: part-typed, and marked after a wrong PIN. */
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
}

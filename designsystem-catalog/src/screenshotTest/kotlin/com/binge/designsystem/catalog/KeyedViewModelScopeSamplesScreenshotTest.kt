package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for [KeyedViewModelScopeSample], at its start: Scope A, with nothing added. */
class KeyedViewModelScopeSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun scope() {
        KeyedViewModelScopeSample()
    }
}

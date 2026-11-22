package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for [com.binge.designsystem.component.BingeOverflowMenu]: a normal, a disabled and a destructive entry. */
class BingeOverflowMenuSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun entries() = BingeOverflowMenuSample()
}

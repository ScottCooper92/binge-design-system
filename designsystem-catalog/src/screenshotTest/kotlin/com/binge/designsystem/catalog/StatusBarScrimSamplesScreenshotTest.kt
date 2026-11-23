package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.PreviewSystemBarInsets

/** Screenshot coverage for the status-bar scrim's gradient. */
class StatusBarScrimSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Wash() = PreviewSystemBarInsets { StatusBarScrimSample() }
}

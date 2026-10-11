package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** Screenshot coverage for the TV skeleton pages: the hub's and the detail page's loading states. */
class TvSkeletonSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun hub() = Frame { TvImmersiveHubSkeletonSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun hubOverlay() = Frame { TvImmersiveHubSkeletonOverlaySample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun detail() = Frame { TvDetailPageSkeletonSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

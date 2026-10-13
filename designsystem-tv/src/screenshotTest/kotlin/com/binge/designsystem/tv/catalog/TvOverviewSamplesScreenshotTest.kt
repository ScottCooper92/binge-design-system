package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/**
 * Screenshot coverage for the full-screen reader. The long-title and scrolled frames pin the vertical overscan
 * inset, which is content padding inside the scroll; the scrolled one is the only frame that changes if that
 * inset ever moves outside it, and a JVM test cannot stand in because Robolectric has no font metrics, so the
 * column never overflows.
 */
class TvOverviewSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun overview() = Frame { TvOverviewScreenSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun overviewLongTitle() = Frame { TvOverviewScreenLongTitleSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun overviewScrolled() = Frame { TvOverviewScreenScrolledSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun overviewReader() = Frame { TvOverviewScreenReaderSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

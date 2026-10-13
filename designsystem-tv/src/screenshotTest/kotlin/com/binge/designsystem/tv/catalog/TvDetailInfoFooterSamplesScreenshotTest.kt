package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** Screenshot coverage for the Details footer: both layouts, at rest and with the ring lit, and the tighter header. */
class TvDetailInfoFooterSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun twoColumns() = Frame { TvDetailInfoFooterSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun threeColumnsFocused() = Frame { TvDetailInfoFooterFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun mediumHeader() = Frame { TvDetailInfoFooterMediumHeaderSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun table() = Frame { TvDetailInfoFooterTableSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun tableFocused() = Frame { TvDetailInfoFooterTableFocusedSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

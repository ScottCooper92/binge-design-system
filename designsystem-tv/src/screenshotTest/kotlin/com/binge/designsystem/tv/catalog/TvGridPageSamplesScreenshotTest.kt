package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/**
 * The grid page's states: at rest, with a first and a third-row cell focused, a long title under a long heading, loading, the
 * held retry, a failure and the two empty results. Focus is a parameter, so a static frame can show it.
 */
class TvGridPageSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun resting() = Frame { TvGridPageSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun focusedFirstCell() = Frame { TvGridPageFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun anchoredRowHidesTheRowAbove() = Frame { TvGridPageAnchoredSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun twoLineTitleClearsTheHeading() = Frame { TvGridPageLongTitleSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun loading() = Frame { TvGridPageLoadingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun retryHeldRingsEntryPlate() = Frame { TvGridPageRetryHeldSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun failed() = Frame { TvGridPageFailedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun emptyWithAction() = Frame { TvGridPageEmptySample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun emptyNoAction() = Frame { TvGridPageEmptyNoActionSample() }

    /** No outer inset, so the page's own insets protect its content; the Column keeps a surface from stretching a fixed band. */
    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) { content() }
        }
    }
}

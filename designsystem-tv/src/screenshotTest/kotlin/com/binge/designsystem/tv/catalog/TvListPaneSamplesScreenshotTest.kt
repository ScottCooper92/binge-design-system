package com.binge.designsystem.tv.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TV_PREVIEW_SPEC
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/**
 * Screenshot coverage for the list-and-pane board: focus in the list, focus in the pane, the same two as a filter
 * panel, rows carrying a status mark (with the longest labels, in Spanish), and the board's skeleton.
 * The pair worth diffing is the first two, where the described row moves from the full fill to the dim accent.
 */
class TvListPaneSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun board() = Frame { TvListPaneBoardSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun boardPaneFocused() = Frame { TvListPaneBoardPaneFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun filterBoard() = Frame { TvListPaneFilterBoardSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun filterBoardPaneFocused() = Frame { TvListPaneFilterBoardPaneFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rowStatus() = Frame { TvListPaneBoardRowStatusSample() }

    @PreviewTest
    @Preview(name = "tv-es", device = TV_PREVIEW_SPEC, locale = "es", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun rowStatusLongLabels() = Frame { TvListPaneBoardRowStatusLongLabelsSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun boardSkeleton() = Frame { TvListPaneBoardSkeletonSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun boardSkeletonRowDetail() = Frame { TvListPaneBoardSkeletonRowDetailSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/**
 * Screenshot coverage for the list-and-pane board: focus in the list, focus in the pane, and the board's skeleton.
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
    fun boardSkeleton() = Frame { TvListPaneBoardSkeletonSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

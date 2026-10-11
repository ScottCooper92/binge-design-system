package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** Screenshot coverage for the TV choice controls: the option group, the checkbox row, the choice row and the tab row. */
class TvChoiceSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun optionGroup() = Frame { TvOptionGroupSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun checkboxRow() = Frame { TvCheckboxRowSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun choiceRow() = Frame { TvChoiceRowSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun tabRow() = Frame { TvTabRowSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun tabRowFocused() = Frame { TvTabRowFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun tabRowCountsUnknown() = Frame { TvTabRowCountsUnknownSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun tabRowWithoutCounts() = Frame { TvTabRowWithoutCountsSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

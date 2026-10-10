package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** Screenshot coverage for the TV text and search fields: each state, and a narrow search bar holding long copy. */
class TvTextFieldSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun textFieldStates() = Frame { TvTextFieldStatesSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun searchFieldStates() = Frame { TvSearchFieldStatesSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun searchFieldNarrow() = Frame { TvSearchFieldNarrowSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

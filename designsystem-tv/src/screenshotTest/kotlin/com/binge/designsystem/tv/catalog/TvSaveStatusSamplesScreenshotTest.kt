package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** Screenshot coverage for the save status line: saving, saved, and a failure with Retry, and a refusal with its reason. */
class TvSaveStatusSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun saveStatusStates() =
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            TvSaveStatusStatesSample()
        }

    @PreviewTest
    @TvPreviews
    @Composable
    fun saveStatusNarrow() =
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            TvSaveStatusNarrowSample()
        }

    @PreviewTest
    @TvPreviews
    @Composable
    fun saveStatusRefusedNarrow() =
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            TvSaveStatusRefusedNarrowSample()
        }
}

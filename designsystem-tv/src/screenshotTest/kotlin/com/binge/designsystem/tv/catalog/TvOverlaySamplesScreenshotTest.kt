package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** Screenshot coverage for the side sheet's thumbnail rows and the confirm dialog's card. */
class TvOverlaySamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetRowThumbnail() = Frame { TvSideSheetRowThumbnailSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun confirmDialog() = Frame { TvConfirmDialogSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

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
    fun sideSheetRowReservedLine() = Frame { TvSideSheetRowReservedLineSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetStackPage() = Frame { TvSideSheetStackPageSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetRowLongLabel() = Frame { TvSideSheetRowLongLabelSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun confirmDialog() = Frame { TvConfirmDialogSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun confirmDialogExtraContent() = Frame { TvConfirmDialogExtraContentSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

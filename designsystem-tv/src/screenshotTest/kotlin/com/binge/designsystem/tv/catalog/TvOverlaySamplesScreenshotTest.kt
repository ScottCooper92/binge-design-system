package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** Screenshot coverage for the side sheet's thumbnail rows, its read-outs and the confirm dialog's card. */
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
    fun sideSheetRowNote() = Frame { TvSideSheetRowNoteSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetReadout() = Frame { TvSideSheetReadoutSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetRowTrailing() = Frame { TvSideSheetRowTrailingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetRowOpensPage() = Frame { TvSideSheetRowOpensPageSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetRowSaving() = Frame { TvSideSheetRowSavingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetToBottomEdge() = Frame { TvSideSheetToBottomEdgeSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun switchOn() = Frame { TvSwitchSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun switchOff() = Frame { TvSwitchOffSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun switchDisabled() = Frame { TvSwitchDisabledSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun switchDestructive() = Frame { TvSwitchDestructiveSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun infoCard() = Frame { TvInfoCardSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun infoCardLeading() = Frame { TvInfoCardLeadingSample() }

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

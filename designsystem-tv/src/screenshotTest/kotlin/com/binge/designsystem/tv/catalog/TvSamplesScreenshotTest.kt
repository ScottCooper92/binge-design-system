package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** Screenshot coverage for the TV catalog samples: one frame per sample, each on the TV panel. */
class TvSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun button() = Frame { TvButtonSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun compactButton() = Frame { TvCompactButtonSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun compactButtonLoading() = Frame { TvCompactButtonLoadingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun messagePlateCompactButton() = Frame { TvMessagePlateCompactButtonSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun buttonLoading() = Frame { TvButtonLoadingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun buttonOnPanel() = Frame { TvButtonOnPanelSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun iconButton() = Frame { TvIconButtonSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun iconButtonLoading() = Frame { TvIconButtonLoadingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sectionTitle() = Frame { TvSectionTitleSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun cardRow() = Frame { TvCardRowSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun cardRowFocused() = Frame { TvCardRowFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun cardRowNoHeading() = Frame { TvCardRowNoHeadingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun messagePlate() = Frame { TvMessagePlateSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun messagePlateTopStart() = Frame { TvMessagePlateTopStartSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheet() = Frame { TvSideSheetSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun errorPlate() = Frame { TvErrorPlateSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetConfirm() = Frame { TvSideSheetConfirmSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetConfirmNeutral() = Frame { TvSideSheetConfirmNeutralSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheetConfirmKeep() = Frame { TvSideSheetConfirmKeepSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun errorPlateNoRetry() = Frame { TvErrorPlateNoRetrySample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun errorPlateTitled() = Frame { TvErrorPlateTitledSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun focusIndicator() = Frame { TvFocusIndicatorSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun verticalDivider() = Frame { TvVerticalDividerSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rowEmphasis() = Frame { TvRowEmphasisSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun selectedTick() = Frame { TvSelectedTickSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun qrCode() = Frame { TvQrCodeSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun pairingCard() = Frame { TvPairingCardSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun pairingCardQrOnly() = Frame { TvPairingCardQrOnlySample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun pairingCardLoading() = Frame { TvPairingCardLoadingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun pairingCardError() = Frame { TvPairingCardErrorSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun pairingCardErrorLongMessage() = Frame { TvPairingCardErrorLongMessageSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun initialsAvatar() = Frame { TvInitialsAvatarSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

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
    fun buttonOnPanel() = Frame { TvButtonOnPanelSample() }

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
    fun messagePlate() = Frame { TvMessagePlateSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun messagePlateTopStart() = Frame { TvMessagePlateTopStartSample() }

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
    fun navRailExpanded() = Frame { TvNavRailSample(expanded = true) }

    @PreviewTest
    @TvPreviews
    @Composable
    fun navRailCollapsed() = Frame { TvNavRailSample(expanded = false) }

    @PreviewTest
    @TvPreviews
    @Composable
    fun navRailCollapsedLastSelected() = Frame { TvNavRailSample(expanded = false, selectedKey = "lists") }

    /**
     * The glass half of the fill, which no other frame reaches: production reports artwork from a
     * `DisposableEffect`, and effects do not run in a preview, so every frame above renders at a solid panel.
     *
     * Expanded is solid over artwork too — the ramp was tuned for the 72dp strip, and stretched over 220dp its
     * hold ends inside the labels. These two are what say which state gets which.
     */
    @PreviewTest
    @TvPreviews
    @Composable
    fun navRailExpandedOverArtwork() = Frame { TvNavRailSample(expanded = true, artworkBehind = true) }

    @PreviewTest
    @TvPreviews
    @Composable
    fun navRailCollapsedOverArtwork() = Frame { TvNavRailSample(expanded = false, artworkBehind = true) }

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

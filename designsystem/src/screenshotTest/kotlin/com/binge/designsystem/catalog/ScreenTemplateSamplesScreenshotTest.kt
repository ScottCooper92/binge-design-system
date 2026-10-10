package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.PreviewCutout
import com.binge.designsystem.preview.PreviewEdge
import com.binge.designsystem.preview.PreviewSystemBarInsets
import com.binge.designsystem.preview.ScreenPreviews
import com.binge.designsystem.preview.ScreenStatePreview

/** [com.binge.designsystem.preview.ScreenPreviews]' `phone-land` cell, for a frame that adds side insets to it. */
private const val PHONE_LANDSCAPE = "spec:width=411dp,height=891dp,orientation=landscape"

/**
 * Screenshot coverage for the screen templates. Each template's layout frame takes the device matrix; its
 * other states take the single phone cell, since they change content within a layout the matrix settled.
 */
class ScreenTemplateSamplesScreenshotTest {
    @PreviewTest
    @ScreenPreviews
    @Composable
    fun screenScaffold() = BingeScreenScaffoldSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun screenScaffoldSmallBar() = BingeScreenScaffoldSmallBarSample()

    /** With no bar the body reads the status bar inset directly, which a default frame renders as zero. */
    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun screenScaffoldNoBarUnderSystemBars() = PreviewSystemBarInsets { BingeScreenScaffoldNoBarSample() }

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun screenScaffoldScrolledUnder() = BingeScreenScaffoldScrolledUnderSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun screenScaffoldShownOverContent() = BingeScreenScaffoldShownOverContentSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun screenScaffoldNoBack() = BingeScreenScaffoldNoBackSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun screenScaffoldSmallScrolledUnder() = BingeScreenScaffoldSmallBarScrolledUnderSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun screenScaffoldSmallShownOverContent() = BingeScreenScaffoldSmallBarShownOverContentSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun screenScaffoldSmallNoBack() = BingeScreenScaffoldSmallBarNoBackSample()

    @PreviewTest
    @ScreenPreviews
    @Composable
    fun message() = MessageScreenSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun messageStackedActions() = MessageScreenStackedActionsSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun messageEmpty() = MessageScreenEmptySample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun messageLoading() = LoadingMessageScreenSample()

    @PreviewTest
    @ScreenPreviews
    @Composable
    fun filteredList() = FilteredListScreenSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun filteredListNotReady() = FilteredListScreenNotReadySample()

    @PreviewTest
    @ScreenPreviews
    @Composable
    fun heroDetail() = HeroDetailScreenSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun heroDetailFooter() = HeroDetailScreenFooterSample()

    @PreviewTest
    @ScreenPreviews
    @Composable
    fun heroDetailLazy() = HeroDetailLazyScreenSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun heroDetailState() = HeroDetailStateScreenSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun heroDetailStateLoading() = HeroDetailStateScreenLoadingSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun form() = FormScreenSample()

    @PreviewTest
    @ScreenPreviews
    @Composable
    fun formFooter() = FormScreenFooterSample()

    @PreviewTest
    @ScreenPreviews
    @Composable
    fun formCapped() = FormScreenCappedSample()

    @PreviewTest
    @ScreenPreviews
    @Composable
    fun stepFlow() = StepFlowScreenSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun stepFlowLoading() = StepFlowScreenLoadingSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun stepFlowLazyStep() = StepFlowScreenLazyStepSample()

    /** The insets a default frame cannot see: the bottom bar clears the nav bar, the rows clear the bar. */
    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun screenScaffoldBottomBarUnderSystemBars() = PreviewSystemBarInsets { BingeScreenScaffoldBottomBarSample() }

    /** The pinned bottom bar's button clears a side cutout as the rows above it do, not only the navigation bar. */
    @PreviewTest
    @Preview(name = "phone-land", device = PHONE_LANDSCAPE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun screenScaffoldBottomBarUnderSideCutout() =
        PreviewSystemBarInsets(cutout = PreviewCutout(PreviewEdge.Left)) { BingeScreenScaffoldBottomBarSample() }

    /**
     * A landscape phone with three-button navigation: the camera cutout on the left, the navigation bar on the right.
     * The bar and the rows clear both, so neither edge's content sits under the hardware.
     */
    @PreviewTest
    @Preview(name = "phone-land", device = PHONE_LANDSCAPE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun screenScaffoldUnderSideInsets() =
        PreviewSystemBarInsets(navigationBarEdge = PreviewEdge.Right, cutout = PreviewCutout(PreviewEdge.Left)) {
            BingeScreenScaffoldSample()
        }

    @PreviewTest
    @ScreenPreviews
    @Composable
    fun screenScaffoldLargeBar() = BingeScreenScaffoldLargeBarSample()
}

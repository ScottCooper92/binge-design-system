package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.PreviewSystemBarInsets
import com.binge.designsystem.preview.ScreenPreviews
import com.binge.designsystem.preview.ScreenStatePreview

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
}

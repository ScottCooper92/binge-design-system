package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.FOLDABLE_PORTRAIT_SPEC
import com.binge.designsystem.preview.SOURCE_LOCALE
import com.binge.designsystem.preview.TabletScreenPreview
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * Screenshot coverage for the detail page's action row, its skeleton and the people and poster rails: the row stacked on a phone and in
 * one row at the expanded width, at a large font, and the rails at the worst-case fonts.
 */
class DetailActionsRailsSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun actions() = DetailActionsSample()

    @PreviewTest
    @TabletScreenPreview
    @Preview(name = "foldable", device = FOLDABLE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun actionsWide() = DetailActionsSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun actionsTwo() = DetailActionsTwoSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun actionsFour() = DetailActionsFourSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun actionsSingle() = DetailActionsSingleSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun actionsTrailingMark() = DetailActionsTrailingMarkSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun actionsSkeleton() = DetailActionsSkeletonSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun peopleRail() = DetailPeopleRailSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun posterRail() = DetailPosterRailSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun peopleLine() = DetailPeopleLineSample()
}

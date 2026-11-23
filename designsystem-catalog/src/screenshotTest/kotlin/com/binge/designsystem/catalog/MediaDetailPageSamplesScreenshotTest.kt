package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.FOLDABLE_PORTRAIT_SPEC
import com.binge.designsystem.preview.PHONE_PORTRAIT_SPEC
import com.binge.designsystem.preview.SOURCE_LOCALE
import com.binge.designsystem.preview.ScreenStatePreview
import com.binge.designsystem.preview.ScreenWidthPreviews
import com.binge.designsystem.preview.TabletScreenPreview
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * Screenshot coverage for the media detail page pieces: the page single-column and at the expanded width (the foldable's and the
 * tablet's), at 1.3x and 2x font, its skeleton beside it, and the stat rows and the overview on their own.
 */
class MediaDetailPageSamplesScreenshotTest {
    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun page() = MediaHeroDetailPageSample()

    @PreviewTest
    @Preview(name = "phone-font13", device = PHONE_PORTRAIT_SPEC, fontScale = 1.3f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Preview(name = "phone-font20", device = PHONE_PORTRAIT_SPEC, fontScale = 2.0f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun pageLargeFont() = MediaHeroDetailPageSample()

    /** The expanded width's cinematic header: the tablet's, and the foldable's, which is where the 840dp breakpoint opens. */
    @PreviewTest
    @TabletScreenPreview
    @Preview(name = "foldable", device = FOLDABLE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun pageWide() = MediaHeroDetailPageWideSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun skeleton() = MediaPageSkeletonSample()

    @PreviewTest
    @Preview(name = "phone-font20", device = PHONE_PORTRAIT_SPEC, fontScale = 2.0f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun skeletonLargeFont() = MediaPageSkeletonSample()

    @PreviewTest
    @TabletScreenPreview
    @Composable
    fun skeletonWide() = MediaPageSkeletonSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun stats() = DetailStatsRowsSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun overview() = DetailOverviewSectionSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun info() = DetailInfoSectionSample()

    /** The heading in the reading column on a tablet: it starts where the facts do. */
    @PreviewTest
    @TabletScreenPreview
    @Composable
    fun infoWide() = DetailInfoSectionWideSample()
}

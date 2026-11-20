package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.PHONE_PORTRAIT_SPEC
import com.binge.designsystem.preview.SOURCE_LOCALE
import com.binge.designsystem.preview.ScreenStatePreview
import com.binge.designsystem.preview.TabletScreenPreview

/**
 * Screenshot coverage for the series template: a lean page, so the episodes section is in the frame, with its episodes, while a season
 * loads, when it failed, and for a show with one season, at a phone's width, at 2x font, and at the expanded width.
 */
class SeriesDetailScreenSamplesScreenshotTest {
    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun page() = SeriesDetailScreenSample()

    @PreviewTest
    @Preview(name = "phone-font20", device = PHONE_PORTRAIT_SPEC, fontScale = 2.0f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun pageLargeFont() = SeriesDetailScreenSample()

    @PreviewTest
    @TabletScreenPreview
    @Composable
    fun pageWide() = SeriesDetailScreenSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun episodesLoading() = SeriesDetailScreenEpisodesLoadingSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun episodesFailed() = SeriesDetailScreenEpisodesFailedSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun singleSeason() = SeriesDetailScreenSingleSeasonSample()
}

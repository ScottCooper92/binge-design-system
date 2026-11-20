package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.SOURCE_LOCALE
import com.binge.designsystem.preview.TabletScreenPreview

/** A phone's width on a window tall enough to show the hero and the episodes section under it. */
private const val TALL_PHONE_SPEC = "spec:width=411dp,height=1500dp,orientation=portrait"

/**
 * Screenshot coverage for the series template: a lean page, so the episodes section is in the frame, with its episodes, while a season
 * loads, when it failed, and for a show with one season, on a tall phone window and at the expanded width.
 */
class SeriesDetailScreenSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "tall", device = TALL_PHONE_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun page() = SeriesDetailScreenSample()

    @PreviewTest
    @TabletScreenPreview
    @Composable
    fun pageWide() = SeriesDetailScreenSample()

    @PreviewTest
    @Preview(name = "tall", device = TALL_PHONE_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun episodesLoading() = SeriesDetailScreenEpisodesLoadingSample()

    @PreviewTest
    @Preview(name = "tall", device = TALL_PHONE_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun episodesFailed() = SeriesDetailScreenEpisodesFailedSample()

    @PreviewTest
    @Preview(name = "tall", device = TALL_PHONE_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun singleSeason() = SeriesDetailScreenSingleSeasonSample()
}

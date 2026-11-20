package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** The Episodes tab's pieces. A frame dispatches no keys, so the pill and the card take `isFocused`; the walk across seasons is covered by `TvDetailEpisodesFocusTest`. */
class TvDetailEpisodesSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun seasonPillStates() = Frame { TvDetailSeasonPillSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun episodeCardStates() = Frame { TvDetailEpisodeCardSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun tabAcrossSeasons() = Frame { TvDetailEpisodesSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun tabBeforeEpisodesLoad() = Frame { TvDetailEpisodesEmptySample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) { Box(Modifier.fillMaxSize()) { content() } }
    }
}

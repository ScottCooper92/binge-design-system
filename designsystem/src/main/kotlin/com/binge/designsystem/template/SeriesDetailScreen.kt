package com.binge.designsystem.template

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.R
import com.binge.designsystem.component.DetailEpisodesSection
import com.binge.designsystem.component.DetailRoundAction
import com.binge.designsystem.component.EPISODES_SHOWN
import com.binge.designsystem.component.EpisodeListRowSkeleton
import com.binge.designsystem.component.EpisodeRow
import com.binge.designsystem.component.SeasonPickerSheet
import com.binge.designsystem.component.SeasonRow

/** What a series page's episodes are: loading, failed with the app's own [message], or the season's [episodes]. */
sealed interface EpisodesState {
    /** The season's episodes are being read; the section draws placeholder rows. */
    data object Loading : EpisodesState

    /** The read failed. [message] is the app's own; Try again is offered when [retryable]. */
    class Failed(
        val message: String,
        val retryable: Boolean = true,
    ) : EpisodesState

    /** The season's [episodes]. */
    class Ready(
        val episodes: List<EpisodeRow>,
    ) : EpisodesState
}

/**
 * What a series page adds to a [MediaDetail]: its [seasons] and which is [selectedSeason], the [episodes] of that season, and the words
 * around them. [episodesTitle] heads the section, [seasonSubtitle] is the line under it ("Season 1 · 9 episodes · 2022") and [viewAllLabel] is
 * the button that offers the rest of the season. An app that keys its seasons by text sets [selectedSeasonKey], which then decides which season is
 * shown, in place of [selectedSeason]. [shown] is how many episodes are listed before [viewAllLabel] takes over: null keeps five, and with every
 * episode shown there is no button. A show with one season has nothing to choose, so it draws no dropdown. The caller owns every word.
 */
@Immutable
class SeriesDetail(
    val seasons: List<SeasonRow>,
    val selectedSeason: Int,
    val episodes: EpisodesState,
    val episodesTitle: String,
    val seasonSubtitle: String? = null,
    val viewAllLabel: String? = null,
    val selectedSeasonKey: String? = null,
    val shown: Int? = null,
    val viewAllAlways: Boolean = false,
)

/**
 * The taps a series page adds: choosing a season, reading the season's episodes again, and opening all of the season's episodes. An app that
 * keys its seasons by text takes the season's key in [onSelectSeasonKey], which is called along with [onSelectSeason]. [onOpenEpisode] is handed the
 * key of a tapped episode; an episode with no key, or no callback, is not tapped.
 */
@Immutable
class SeriesDetailCallbacks(
    val onSelectSeason: (number: Int) -> Unit,
    val onRetryEpisodes: () -> Unit = {},
    val onViewAllEpisodes: () -> Unit = {},
    val onSelectSeasonKey: ((key: String) -> Unit)? = null,
    val onOpenEpisode: ((key: String) -> Unit)? = null,
)

/**
 * A series' page, assembled once for every app: everything [MovieDetailScreen] draws, with the episodes section after the people and the
 * first named slot. The section has the season's dropdown in its header (drawn when there is more than one season), which opens the season
 * picker as a sheet the page hosts, the subtitle, the first few episodes and a button for the rest; while a season's episodes load or have
 * failed it draws its own placeholder or message under the same header. [series] is the seasons and episodes, and [seriesCallbacks] their taps.
 * Everything else is as [MovieDetailScreen]: the state, the model, the slots and the callbacks.
 *
 * @need assemble a series' detail page from plain values, with its seasons, its episodes and its loading and failed states
 * @group detail
 * @replaces a hand-built series page that lists the detail sections, the season picker and the episodes
 * @sample SeriesDetailScreenSample
 */
@Composable
fun SeriesDetailScreen(
    state: DetailState,
    series: SeriesDetail?,
    callbacks: MediaDetailCallbacks,
    seriesCallbacks: SeriesDetailCallbacks,
    modifier: Modifier = Modifier,
    primary: (@Composable (Modifier) -> Unit)? = null,
    roundActions: List<DetailRoundAction> = emptyList(),
    topBarActions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    afterPeople: (@Composable () -> Unit)? = null,
    afterRails: (@Composable () -> Unit)? = null,
    atEnd: (@Composable () -> Unit)? = null,
    overlays: (@Composable () -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scrollState: ScrollState = rememberScrollState(),
) {
    var picking by rememberSaveable { mutableStateOf(false) }
    MediaDetailScreen(
        state = state,
        callbacks = callbacks,
        modifier = modifier,
        primary = primary,
        roundActions = roundActions,
        topBarActions = topBarActions,
        afterPeople = afterPeople,
        afterRails = afterRails,
        atEnd = atEnd,
        overlays = overlays,
        snackbarHostState = snackbarHostState,
        scrollState = scrollState,
        episodes = series?.let { { SeriesEpisodes(it, seriesCallbacks, onPick = { picking = true }) } },
    )
    if (picking && series != null && state is DetailState.Content) {
        SeasonPickerSheet(
            rows = series.seasons,
            selected = series.selectedSeason,
            onSelect = {
                picking = false
                seriesCallbacks.onSelectSeason(it)
            },
            onDismiss = { picking = false },
            selectedKey = series.selectedSeasonKey,
            onSelectKey = seriesCallbacks.onSelectSeasonKey,
        )
    }
}

/** The episodes section of a series: its header, subtitle and rows, with the placeholder or the failure in the rows' place. */
@Composable
private fun SeriesEpisodes(
    series: SeriesDetail,
    callbacks: SeriesDetailCallbacks,
    onPick: () -> Unit,
) {
    val season = series.seasons.firstOrNull { row ->
        if (series.selectedSeasonKey != null) row.key == series.selectedSeasonKey else row.number == series.selectedSeason
    }
    val ready = series.episodes as? EpisodesState.Ready
    DetailEpisodesSection(
        title = series.episodesTitle,
        episodes = ready?.episodes.orEmpty(),
        subtitle = series.seasonSubtitle,
        seasonLabel = season?.label.takeIf { series.seasons.size > 1 },
        onSeasonClick = onPick.takeIf { series.seasons.size > 1 },
        viewAllLabel = series.viewAllLabel,
        shown = series.shown ?: EPISODES_SHOWN,
        viewAllAlways = series.viewAllAlways,
        onViewAll = callbacks.onViewAllEpisodes,
        onOpenEpisode = callbacks.onOpenEpisode,
        stateContent = when (val episodes = series.episodes) {
            EpisodesState.Loading -> ({ repeat(PLACEHOLDER_EPISODES) { EpisodeListRowSkeleton() } })
            is EpisodesState.Failed -> ({ EpisodesFailure(episodes, callbacks.onRetryEpisodes) })
            is EpisodesState.Ready -> null
        },
    )
}

/** A season whose episodes did not load: the app's message, and Try again where another attempt can help. */
@Composable
private fun EpisodesFailure(state: EpisodesState.Failed, onRetry: () -> Unit) {
    Column(Modifier.padding(vertical = dimensionResource(R.dimen.padding_s))) {
        Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (state.retryable) {
            TextButton(onClick = onRetry) { Text(stringResource(R.string.error_kind_retry)) }
        }
    }
}

private const val PLACEHOLDER_EPISODES = 2

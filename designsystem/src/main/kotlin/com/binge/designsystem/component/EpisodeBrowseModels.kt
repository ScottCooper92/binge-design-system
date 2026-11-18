package com.binge.designsystem.component

import androidx.compose.runtime.Immutable

/**
 * One episode of a season, as [EpisodeBrowseRow] shows it. Plain values only: [airDate] arrives already
 * formatted for the reader, and the consumer decides what an absent [rating] or [runtimeMinutes] means.
 *
 * A [rating] of null or 0 draws no chip. The eyebrow is "E[number]", then [runtimeMinutes] when there is
 * one, else [airDate] when there is one. [number] is unique within a season: the list keys its rows by it.
 */
@Immutable
data class EpisodeBrowseUi(
    val number: Int,
    val name: String,
    val overview: String? = null,
    val stillUrl: String? = null,
    val rating: Float? = null,
    val runtimeMinutes: Int? = null,
    val airDate: String? = null,
)

/**
 * A season in [SeasonSectionsList]: a header made of [title] and [subtitle], and the rows [state] gives it.
 * [key] is stable and unique across the list; it keys the items and names the section in `openAt` and
 * `onSeasonNeeded`.
 */
@Immutable
data class SeasonSectionUi(
    val key: String,
    val title: String,
    val subtitle: String,
    val state: SeasonSectionState,
)

/** What a season's section holds below its header. */
sealed interface SeasonSectionState {
    /** The season's episodes, one row each. */
    @Immutable
    data class Loaded(
        val episodes: List<EpisodeBrowseUi>,
    ) : SeasonSectionState

    /** The season is on its way: [rowCount] skeleton rows hold its place, capped at [MAX_SEASON_SKELETON_ROWS]. */
    @Immutable
    data class Loading(
        val rowCount: Int,
    ) : SeasonSectionState

    /** The season failed: [message] and, when [onRetry] is set, a text button labelled [retryLabel]. */
    @Immutable
    data class Failed(
        val message: String,
        val retryLabel: String,
        val onRetry: (() -> Unit)? = null,
    ) : SeasonSectionState
}

/** A bad episode count from the data source can't build an unbounded skeleton. */
const val MAX_SEASON_SKELETON_ROWS = 30

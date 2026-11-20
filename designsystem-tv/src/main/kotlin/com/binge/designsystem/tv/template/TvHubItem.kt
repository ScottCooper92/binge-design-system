package com.binge.designsystem.tv.template

import com.binge.designsystem.component.HeroItem
import com.binge.designsystem.tv.component.TvImageKind

/**
 * One title in an expanding hub row, as the row draws it: the app supplies every value already worded and formatted.
 *
 * @property key the title's identity, stable across reloads and unique within a row and across rows that share extras (a movie and
 *   a series can share a number, so a key is more than a bare id). Focus position and the extras cache are keyed on it.
 * @property year the fact that follows the genres, "2024", or the episode's code when the card opens one episode.
 * @property progress how far into the title the viewer is, 0 to 1, drawn as a bar on the poster; null draws none.
 * @property posterKind what the poster stands in for in the See all card's fan when its image fails to load, which decides the glyph the
 *   plate draws. A row's own posters have no placeholder: they show the plain fill until their artwork arrives.
 * @property failure a line shown in place of the meta line in the error colour, for a card whose action just failed; null shows the meta.
 */
data class TvHubItem(
    val key: String,
    val title: String,
    val posterUrl: String?,
    val rating: Float?,
    val backdropUrl: String? = null,
    val genres: List<String> = emptyList(),
    val year: String? = null,
    val overview: String? = null,
    val progress: Float? = null,
    val failure: String? = null,
    val posterKind: TvImageKind = TvImageKind.Movie,
    /** The caller's own item this was drawn from, handed back by the hub's callbacks; the hub never reads it. */
    internal val source: Any? = null,
    /** Set on the item a hero slide is loaded as, so its extras come from the hero's own loader. */
    internal val heroSource: HeroItem? = null,
)

/** What a hub loads about a title once it is open: its logo, certification, synopsis and the facts the hero's meta line shows. */
data class TvTitleExtras(
    val logoUrl: String?,
    val certification: String?,
    val overview: String? = null,
    val year: String? = null,
    val runtimeMinutes: Int? = null,
    val seasons: Int? = null,
)

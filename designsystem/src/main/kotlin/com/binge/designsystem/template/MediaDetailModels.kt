package com.binge.designsystem.template

import androidx.compose.runtime.Immutable
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.component.DetailInfoEntry
import com.binge.designsystem.component.DetailPerson
import com.binge.designsystem.component.DetailPoster
import com.binge.designsystem.component.DetailStat
import com.binge.designsystem.component.ExternalScore

/**
 * One person in a [PeopleRail]. [key] is the app's own identity for the item, handed back when it is tapped. It is a String because a lazy
 * list keeps its items' keys across a configuration change, which needs a value that can be saved; the app can always find the item from a
 * text key. It need not be one person's id, so one person can be an actor and a director under two keys.
 */
@Immutable
class PersonItem(
    val key: String,
    val person: DetailPerson,
)

/** A titled row of people, such as a cast or a crew. [seeAll] makes the heading a link to the full list. */
@Immutable
class PeopleRail(
    val title: String,
    val people: List<PersonItem>,
    val seeAll: Boolean = false,
)

/** One title in a [PosterRail]. [key] is the app's own identity for the item, handed back when it is tapped; a String, for the reason given on [PersonItem]. */
@Immutable
class PosterItem(
    val key: String,
    val poster: DetailPoster,
)

/** A titled row of posters: a collection, similar titles, recommendations. [seeAll] adds the trailing see-all tile and makes the heading a link. */
@Immutable
class PosterRail(
    val title: String,
    val items: List<PosterItem>,
    val seeAll: Boolean = false,
)

/** One tag in a [TagsSection]. [key] is handed back when it is tapped. */
@Immutable
class TagItem(
    val key: String,
    val label: String,
)

/** A heading over a wrapping row of tags (keywords, themes). With [tappable] false they are plain labels. */
@Immutable
class TagsSection(
    val title: String,
    val tags: List<TagItem>,
    val tappable: Boolean = true,
)

/** A heading over a grid of labelled facts (original title, studios, premiere date). Entries with no value are left out. */
@Immutable
class DetailInfo(
    val heading: String,
    val entries: List<DetailInfoEntry>,
)

/**
 * What a movie's page shows, as plain values: no app, server or media-database type. Every URL is already built and every word is already
 * in the user's language. A part the app has nothing for is left empty and its section is not drawn.
 *
 * @property backdropUrl the image behind the hero; the template falls back to [posterUrl] where there is none, so the hero is never bare
 * @property scores ratings from other sources, shown with the hero
 * @property stats the stats under the hero (a rating, a year, a runtime, a certification), each already worded, so the template assumes no vote count
 * @property overviewHeading the heading over [overview]; defaults to this library's own word for it
 */
@Immutable
class MediaDetail(
    val title: String,
    val backdropUrl: String? = null,
    val posterUrl: String? = null,
    val tagline: String? = null,
    val genres: List<String> = emptyList(),
    val metaText: String = "",
    val scores: List<ExternalScore> = emptyList(),
    val stats: List<DetailStat> = emptyList(),
    val overview: String? = null,
    val overviewHeading: String? = null,
    val peopleRails: List<PeopleRail> = emptyList(),
    val posterRails: List<PosterRail> = emptyList(),
    val tags: TagsSection? = null,
    val info: DetailInfo? = null,
)

/** The three things a detail page can be: loading, showing, or failed. [MovieDetailScreen] draws each in the same frame. */
sealed interface DetailState {
    /**
     * Loading. The skeleton holds only what the loaded page promises, so it does not jump when the page arrives: [stats] is how many stats the
     * page will have, [hasPrimary] whether it has a primary action, [roundActions] how many round actions, and [rails] how many rails of people
     * or posters follow the overview (each a carousel placeholder; the default, none, leaves the rest of the page to grow). The round actions
     * stack under the primary at the same height from two up on a phone, but beside it at the expanded width two and three buttons can differ,
     * so give the count the loaded page will have.
     */
    class Loading(
        val stats: Int = 3,
        val roundActions: Int = 0,
        val hasPrimary: Boolean = true,
        val rails: Int = 0,
    ) : DetailState

    /** Showing [detail]. */
    class Content(
        val detail: MediaDetail,
    ) : DetailState

    /**
     * Failed. The failure replaces the page on the same frame as the loading page, so the back control does not move. [kind] picks the glyph,
     * the headline and, when [message] is null, the words; give a [message] where the app can say more than the kind does. Try again is
     * offered only when [retryable], where another attempt can help.
     */
    class Failed(
        val message: String? = null,
        val retryable: Boolean = true,
        val kind: ErrorKind = ErrorKind.Generic,
    ) : DetailState
}

/**
 * The taps of a detail page. Each item callback is handed the [key] the app put on the item, so the template knows nothing of the app's
 * ids. [onSeeAllPeople] and [onSeeAllPosters] are handed the rail's index in the model's list.
 */
@Immutable
class MediaDetailCallbacks(
    val onBack: () -> Unit,
    val onRetry: () -> Unit = {},
    val onOpenPerson: (key: String) -> Unit = {},
    val onOpenPoster: (key: String) -> Unit = {},
    val onOpenTag: (key: String) -> Unit = {},
    val onSeeAllPeople: (railIndex: Int) -> Unit = {},
    val onSeeAllPosters: (railIndex: Int) -> Unit = {},
)

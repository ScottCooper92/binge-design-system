package com.binge.designsystem.component

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.template.readingInset

/** What a [DetailPeopleRail] draws for one person: their [name], a one-line [subtitle] (a character, a job) and the [photoUrl], already built. */
@Immutable
class DetailPerson(
    val name: String,
    val subtitle: String?,
    val photoUrl: String?,
)

/**
 * What a [DetailPosterRail] draws for one title: the [posterUrl], already built, the [title] and its [rating], an optional [status] badge,
 * a [progress] bar and a one-line [subtitle] (a role, a year). The caller owns the words.
 */
@Immutable
class DetailPoster(
    val posterUrl: String?,
    val title: String,
    val rating: Float?,
    val status: MediaCardStatus? = null,
    val progress: Float? = null,
    val subtitle: String? = null,
)

/**
 * A detail page's row of people (a cast, a crew): a titled [MediaCarousel] of [PersonCard]s, drawn from [people] by [person]. [itemKey] is
 * each person's stable key, [onOpen] is handed the tapped item, and [onSeeAll], when given, makes the heading a link to the full list. The
 * row's side inset is the page's reading inset, so its first card lines up with the page's copy. Draws nothing for an empty list.
 *
 * @need show a detail page's cast or crew as a titled row of people
 * @group detail
 * @replaces a hand-built MediaCarousel of PersonCards
 * @sample DetailPeopleRailSample
 */
@Composable
fun <T> DetailPeopleRail(
    title: String,
    people: List<T>,
    itemKey: (T) -> Any,
    person: (T) -> DetailPerson,
    onOpen: (T) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
    horizontalPadding: Dp = readingInset(),
) {
    if (people.isEmpty()) return
    MediaCarousel(
        title = title,
        items = people,
        itemKey = itemKey,
        modifier = modifier,
        onMoreClick = onSeeAll,
        onEndTileClick = null,
        horizontalPadding = horizontalPadding,
    ) { _, item ->
        val shown = person(item)
        PersonCard(name = shown.name, subtitle = shown.subtitle, photoUrl = shown.photoUrl, onClick = { onOpen(item) })
    }
}

/**
 * A detail page's row of posters (similar titles, recommendations, a person's credits): a titled [MediaCarousel] of [MediaCard]s, drawn
 * from [items] by [poster]. [itemKey] is each item's stable key, [onOpen] is handed the tapped item, and [onSeeAll], when given, adds
 * the trailing see-all tile and makes the heading a link. The row's side inset is the page's reading inset. Draws nothing for an empty list.
 *
 * @need show a detail page's similar or related titles as a titled row of posters
 * @group detail
 * @replaces a hand-built MediaCarousel of MediaCards
 * @sample DetailPosterRailSample
 */
@Composable
fun <T> DetailPosterRail(
    title: String,
    items: List<T>,
    itemKey: (T) -> Any,
    poster: (T) -> DetailPoster,
    onOpen: (T) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
    horizontalPadding: Dp = readingInset(),
) {
    if (items.isEmpty()) return
    MediaCarousel(
        title = title,
        items = items,
        itemKey = itemKey,
        modifier = modifier,
        onMoreClick = onSeeAll,
        horizontalPadding = horizontalPadding,
    ) { _, item ->
        val shown = poster(item)
        MediaCard(
            posterUrl = shown.posterUrl,
            title = shown.title,
            rating = shown.rating,
            onClick = { onOpen(item) },
            modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
            status = shown.status,
            progress = shown.progress,
            subtitle = shown.subtitle,
        )
    }
}

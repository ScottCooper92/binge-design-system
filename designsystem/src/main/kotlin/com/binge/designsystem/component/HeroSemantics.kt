package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.R
import com.binge.designsystem.formatRating

/**
 * What a screen reader reads for the hub hero — the whole card, on the one node that opens it.
 *
 * The hero's copy renders in [HeroCopyOverlay], a *sibling* of the clickable backdrop rather than a
 * descendant, so nothing merges into the button on its own: the hub's primary call to action announced
 * as an unlabelled button. The image inside it cannot supply the name either — a
 * `SubcomposeAsyncImage` given `loading`/`error` slots keeps its `contentDescription` off the merged
 * node, which is the same shape [MediaCard] has and why that card names itself through its caption.
 *
 * So the button carries the copy and the overlay is cleared to match. Reading the card whole is the
 * point: the copy is otherwise six unlabelled text fragments to swipe through ("8.8", "2010", …), none
 * of which says what title it belongs to.
 */
@Composable
internal fun heroContentDescription(item: HeroItem): String {
    val separator = stringResource(R.string.list_separator)
    val trending = item.rank?.let { stringResource(R.string.hero_trending_today, it) }
    val rating = item.rating?.let { stringResource(R.string.cd_rating_out_of_ten, it.formatRating()) }
    val genres = item.genres
        .take(MAX_META_GENRES)
        .takeIf { it.isNotEmpty() }
        ?.joinToString(separator)
    return listOfNotNull(
        item.title,
        trending,
        item.tagline,
        rating,
        item.year,
        heroRuntimeOrSeasons(item),
        genres,
    ).joinToString(separator)
}

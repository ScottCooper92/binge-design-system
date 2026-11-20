package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.MediaCard
import com.binge.designsystem.component.MediaCardAction
import com.binge.designsystem.component.MediaCardSkeleton
import com.binge.designsystem.component.MediaCardStatus
import com.binge.designsystem.component.MediaTypeTagType
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.WithPreviewImage
import com.binge.designsystem.theme.BingeSentiment

/**
 * Shared public-sample convention — the worked example.
 *
 * Without it a component's preview drifts across two places: a `private` inline `@Preview` (IDE
 * only) and a `screenshotTest @PreviewTest @Preview` (CI only). This `…Samples` layer gives the
 * component one public sample that both call, so the IDE render and the asserted screenshot are
 * pixel-identical, and the committed baselines double as the browsable catalog
 * (`tools/screenshot-gallery`).
 *
 * The convention:
 * 1. **One `<Component>Samples.kt` per component** in this `catalog/` package, so a component's samples
 *    sit in one place.
 * 2. **No-arg public `@Composable` samples**, one representative state each, so every consumer calls
 *    them identically. Add one per visually distinct variant.
 * 3. **Wrap in [ScreenshotTheme]** for deterministic render; the light/dark axis is the caller's
 *    `@Preview(uiMode = …)` (the screenshot test stacks both).
 * 4. **Fold `*PreviewData` builders in** as the data source; inline a single literal rather than
 *    inventing a builder. Sample files are preview tooling.
 * 5. **Compact, space-free `name` tokens** on the screenshot `@Preview` (see `DevicePreviews.kt`).
 *
 * A media card is a fixed [R.dimen.card_width] poster cell, so the sample bounds its own width to
 * match how it renders inside a carousel/grid.
 */
@Composable
fun MediaCardRatedSample() {
    ScreenshotTheme {
        MediaCard(
            posterUrl = null,
            title = "The Dark Knight",
            rating = 9.0f,
            onClick = {},
            modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
        )
    }
}

/**
 * The placeholder the grids draw in a card's place, at the same width, so the catalogued pair shows what a
 * cell reserves against what it fills — the comparison the skeleton exists to survive.
 */
@Composable
fun MediaCardSkeletonSample() {
    ScreenshotTheme {
        MediaCardSkeleton(modifier = Modifier.width(dimensionResource(R.dimen.card_width)))
    }
}

/**
 * Library poster variant: the contextual top-end action (a remove X), a movie/TV type badge
 * top-start for mixed collections, and the user's own rating as an accent chip bottom-end — the
 * metadata that survives the row→poster switch.
 */
@Composable
fun MediaCardLibrarySample() {
    ScreenshotTheme {
        MediaCard(
            posterUrl = null,
            title = "Past Lives",
            rating = 7.8f,
            onClick = {},
            topEndAction = MediaCardAction(icon = Icons.Filled.Close, contentDescription = "Remove", onClick = {}),
            typeBadge = MediaTypeTagType.Movie,
            userRating = 9.0f,
            modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
        )
    }
}

/** Unrated card with a long title that wraps to the two-line cap — the reflow variant. */
@Composable
fun MediaCardUnratedSample() {
    ScreenshotTheme {
        MediaCard(
            posterUrl = null,
            title = "A Very Long Movie Title That Should Wrap Or Truncate",
            rating = null,
            onClick = {},
            modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
        )
    }
}

/**
 * A poster carrying a status as a dot top-end, the way a grid marks where a request for the title stands. The type badge sits top-start
 * on a surface that mixes kinds, so the sample shows both. The label is not drawn: it is in the card's semantics.
 */
@Composable
fun MediaCardStatusSample() {
    ScreenshotTheme {
        MediaCard(
            posterUrl = null,
            title = "Arrival",
            rating = 7.9f,
            onClick = {},
            typeBadge = MediaTypeTagType.Movie,
            status = MediaCardStatus(label = "Available", sentiment = BingeSentiment.Positive),
            modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
        )
    }
}

/** Every sentiment's status dot on the default poster, a row of five. The dot is the one a status chip draws, a little larger. */
@Composable
fun MediaCardStatusSentimentsSample() {
    ScreenshotTheme {
        StatusSentimentsRow(posterUrl = null)
    }
}

/** The same five dots on a near-white poster, where the thin scrim behind the halo is what keeps each at 3:1. */
@Composable
fun MediaCardStatusLightPosterSample() {
    ScreenshotTheme {
        WithPreviewImage(widthPx = LIGHT_POSTER_WIDTH_PX, heightPx = LIGHT_POSTER_HEIGHT_PX, color = LIGHT_POSTER_COLOR) {
            StatusSentimentsRow(posterUrl = "light-poster")
        }
    }
}

/** The same five dots on a near-black poster, where the halo reads on its own. */
@Composable
fun MediaCardStatusDarkPosterSample() {
    ScreenshotTheme {
        WithPreviewImage(widthPx = LIGHT_POSTER_WIDTH_PX, heightPx = LIGHT_POSTER_HEIGHT_PX, color = DARK_POSTER_COLOR) {
            StatusSentimentsRow(posterUrl = "dark-poster")
        }
    }
}

@Composable
private fun StatusSentimentsRow(posterUrl: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_sm)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs)),
    ) {
        BingeSentiment.entries.forEach { sentiment ->
            MediaCard(
                posterUrl = posterUrl,
                title = sentiment.name,
                rating = null,
                onClick = {},
                status = MediaCardStatus(label = sentiment.name, sentiment = sentiment),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** A status and a top-end action on one poster: the dot sits to the start of the action button, so neither covers the other. */
@Composable
fun MediaCardStatusWithActionSample() {
    ScreenshotTheme {
        MediaCard(
            posterUrl = null,
            title = "Arrival",
            rating = 7.9f,
            onClick = {},
            status = MediaCardStatus(label = "Requested", sentiment = BingeSentiment.Info),
            topEndAction = MediaCardAction(icon = Icons.Filled.Close, contentDescription = "Remove", onClick = {}),
            modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
        )
    }
}

private const val LIGHT_POSTER_WIDTH_PX = 300
private const val LIGHT_POSTER_HEIGHT_PX = 450
private const val LIGHT_POSTER_COLOR = 0xFFF4F1EA.toInt()
private const val DARK_POSTER_COLOR = 0xFF14110F.toInt()

/** A title the user is part-way through: the bar along the poster's bottom edge says how far, here 40%. */
@Composable
fun MediaCardProgressSample() {
    ScreenshotTheme {
        MediaCard(
            posterUrl = null,
            title = "Arrival",
            rating = 7.9f,
            onClick = {},
            progress = 0.4f,
            modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
        )
    }
}

/**
 * A series card that opens one episode: the title keeps one line and the episode takes the second, so the caption
 * stays the height of a plain card's. One with a bar is part-way through that episode; one without is the next to watch.
 */
@Composable
fun MediaCardEpisodeSample() {
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            MediaCard(
                posterUrl = null,
                title = "Severance",
                rating = 8.7f,
                onClick = {},
                progress = 0.7f,
                subtitle = "S2 E4",
                modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
            )
            MediaCard(
                posterUrl = null,
                title = "A Very Long Series Title That Should Truncate",
                rating = null,
                onClick = {},
                subtitle = "S2 E5",
                modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
            )
        }
    }
}

/** A favourite, marked by a small heart top-end: drawn and announced with the card, not tappable. The type badge sits top-start. */
@Composable
fun MediaCardFavouriteSample() {
    ScreenshotTheme {
        MediaCard(
            posterUrl = null,
            title = "Past Lives",
            rating = 7.8f,
            onClick = {},
            typeBadge = MediaTypeTagType.Movie,
            favourite = true,
            modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
        )
    }
}

/** A favourite with a status, and a favourite with a status and an action: the heart, the dot and the action sit side by side, never over one another. */
@Composable
fun MediaCardFavouriteWithStatusSample() {
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            MediaCard(
                posterUrl = null,
                title = "Arrival",
                rating = 7.9f,
                onClick = {},
                favourite = true,
                status = MediaCardStatus(label = "Available", sentiment = BingeSentiment.Positive),
                modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
            )
            MediaCard(
                posterUrl = null,
                title = "Arrival",
                rating = 7.9f,
                onClick = {},
                favourite = true,
                status = MediaCardStatus(label = "Requested", sentiment = BingeSentiment.Info),
                topEndAction = MediaCardAction(icon = Icons.Filled.Close, contentDescription = "Remove", onClick = {}),
                modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
            )
        }
    }
}

/** A favourite with each status, a row of five: the heart sits to the start of each sentiment's dot. */
@Composable
fun MediaCardFavouriteSentimentsSample() {
    ScreenshotTheme {
        FavouriteSentimentsRow(posterUrl = null)
    }
}

/** The same five on a near-white poster, where the scrim disc is what holds the heart at 3:1. */
@Composable
fun MediaCardFavouriteLightPosterSample() {
    ScreenshotTheme {
        WithPreviewImage(widthPx = LIGHT_POSTER_WIDTH_PX, heightPx = LIGHT_POSTER_HEIGHT_PX, color = LIGHT_POSTER_COLOR) {
            FavouriteSentimentsRow(posterUrl = "light-poster")
        }
    }
}

/** A favourite at the wider card a TV poster row draws, with a status: the marks keep their size and corner as the card grows. */
@Composable
fun MediaCardFavouriteWideSample() {
    ScreenshotTheme {
        MediaCard(
            posterUrl = null,
            title = "Arrival",
            rating = 7.9f,
            onClick = {},
            favourite = true,
            status = MediaCardStatus(label = "Available", sentiment = BingeSentiment.Positive),
            modifier = Modifier.width(dimensionResource(R.dimen.detail_cinematic_poster_width)),
        )
    }
}

@Composable
private fun FavouriteSentimentsRow(posterUrl: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_sm)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs)),
    ) {
        BingeSentiment.entries.forEach { sentiment ->
            MediaCard(
                posterUrl = posterUrl,
                title = sentiment.name,
                rating = null,
                onClick = {},
                favourite = true,
                status = MediaCardStatus(label = sentiment.name, sentiment = sentiment),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** A watched title: a tick top-end, alone and then beside a favourite heart and a status dot; they sit side by side, never over one another. */
@Composable
fun MediaCardWatchedSample() {
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            MediaCard(
                posterUrl = null,
                title = "Arrival",
                rating = 7.9f,
                onClick = {},
                watched = true,
                modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
            )
            MediaCard(
                posterUrl = null,
                title = "Arrival",
                rating = 7.9f,
                onClick = {},
                watched = true,
                favourite = true,
                status = MediaCardStatus(label = "Available", sentiment = BingeSentiment.Positive),
                modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
            )
        }
    }
}

/**
 * A series with episodes left: a count badge top-end. Three episodes, then a count above 99 that reads "99+", then, on a second row, the
 * badge beside a favourite heart, a status dot and an action.
 */
@Composable
fun MediaCardUnwatchedCountSample() {
    ScreenshotTheme {
        // Two cards a row, so each is the width it has in an app and the four-mark corner of the last one is shown at its real size.
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
                MediaCard(
                    posterUrl = null,
                    title = "Severance",
                    rating = 8.7f,
                    onClick = {},
                    unwatchedCount = 3,
                    modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
                )
                MediaCard(
                    posterUrl = null,
                    title = "Doctor Who",
                    rating = 8.0f,
                    onClick = {},
                    unwatchedCount = 120,
                    modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
                )
            }
            MediaCard(
                posterUrl = null,
                title = "Severance",
                rating = 8.7f,
                onClick = {},
                unwatchedCount = 3,
                favourite = true,
                status = MediaCardStatus(label = "Available", sentiment = BingeSentiment.Positive),
                topEndAction = MediaCardAction(icon = Icons.Filled.Close, contentDescription = "Remove", onClick = {}),
                modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
            )
        }
    }
}

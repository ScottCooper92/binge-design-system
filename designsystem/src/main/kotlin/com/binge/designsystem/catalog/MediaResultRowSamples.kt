@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultKind
import com.binge.designsystem.component.MediaResultRow
import com.binge.designsystem.component.MediaResultRowSkeleton
import com.binge.designsystem.component.StatusChip
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeSentiment

@Composable
private fun SampleRow(result: MediaResult, status: Boolean = false) {
    ScreenshotTheme {
        Column(Modifier.padding(dimensionResource(R.dimen.screen_content_inset))) {
            MediaResultRow(
                result = result,
                onClick = {},
                trailing = if (status) ({ StatusChip(label = "Available", sentiment = BingeSentiment.Positive) }) else null,
            )
        }
    }
}

/** A film: the poster, a kind and year, then the status, the rating and the genres in one wrapping row. */
@Composable
fun MediaResultRowPosterSample() =
    SampleRow(
        MediaResult("m1", MediaResultKind.Poster, null, "Past Lives", "Movie · 2023", rating = 7.8f, meta = "Drama, Romance"),
        status = true,
    )

/** A person: a round portrait, a name and a role, and no status line. */
@Composable
fun MediaResultRowAvatarSample() = SampleRow(MediaResult("p1", MediaResultKind.Avatar, null, "Greta Lee", "Actor"))

/** A studio or network: its logo on a tonal tile, with a name and nothing else. */
@Composable
fun MediaResultRowLogoSample() = SampleRow(MediaResult("s1", MediaResultKind.Logo, null, "A24", null))

/** An episode: a 16:9 still, then the show and its number, and a played mark in the trailing slot. */
@Composable
fun MediaResultRowStillSample() =
    SampleRow(
        MediaResult("e1", MediaResultKind.Still, null, "Pilot", "Severance · S1 E1", meta = "58 min"),
        status = true,
    )

/** A long name and a long subtitle at the row's width: the name wraps to two lines and the subtitle stays on one. */
@Composable
fun MediaResultRowLongTextSample() =
    ScreenshotTheme {
        Column(
            Modifier.padding(dimensionResource(R.dimen.screen_content_inset)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            MediaResultRow(
                result =
                    MediaResult(
                        "m2",
                        MediaResultKind.Poster,
                        null,
                        "The Lord of the Rings: The Fellowship of the Ring, Extended Edition",
                        "Movie · 2001 · a very long kind label that must stay on one line",
                        rating = 8.4f,
                        meta = "Adventure, Fantasy, Action",
                    ),
                onClick = {},
                trailing = { StatusChip(label = "Partially available", sentiment = BingeSentiment.Caution) },
            )
        }
    }

/** A result the user has marked: a heart and a tick beside the name, the same marks a poster card draws. */
@Composable
fun MediaResultRowMarksSample() =
    SampleRow(
        MediaResult(
            "m3",
            MediaResultKind.Poster,
            null,
            "Arrival",
            "Movie · 2016",
            rating = 7.9f,
            meta = "Drama, Sci-Fi",
            favourite = true,
            watched = true,
        ),
    )

/**
 * What a search draws while it loads: a plate for the picture and two for the name and the subtitle, in each kind's own shape and alignment: a
 * poster, an episode still, a round portrait and a logo tile.
 */
@Composable
fun MediaResultRowSkeletonSample() =
    ScreenshotTheme {
        Column(
            Modifier.padding(dimensionResource(R.dimen.screen_content_inset)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            MediaResultRowSkeleton()
            MediaResultRowSkeleton(kind = MediaResultKind.Still)
            MediaResultRowSkeleton(kind = MediaResultKind.Avatar)
            MediaResultRowSkeleton(kind = MediaResultKind.Logo)
        }
    }

/** A list whose rows carry a blurb of up to three lines, as a collection's parts do: the placeholder holds the same room, so the rows below do not shift when the list loads. */
@Composable
fun MediaResultRowSkeletonBlurbSample() =
    ScreenshotTheme {
        Column(
            Modifier.padding(dimensionResource(R.dimen.screen_content_inset)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            MediaResultRowSkeleton(blurbLines = 3)
            MediaResultRowSkeleton(blurbLines = 3)
        }
    }

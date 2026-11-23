@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultKind
import com.binge.designsystem.component.MediaResultRow
import com.binge.designsystem.component.MediaResultRowSkeleton
import com.binge.designsystem.component.NameWithSuffix
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

/**
 * The row's loading stand-in hinted at a long title, over the row it becomes: the name plate takes the lines the title takes (up to the row's two), at
 * every font size, so the rows below do not shift when the list loads.
 */
@Composable
fun MediaResultRowSkeletonHintedSample() =
    ScreenshotTheme {
        val title = "The Lord of the Rings: The Fellowship of the Ring"
        Column(
            Modifier.padding(dimensionResource(R.dimen.screen_content_inset)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            MediaResultRowSkeleton(expectedName = title)
            MediaResultRow(
                result = MediaResult("k", MediaResultKind.Poster, null, title, "Movie · 2001", rating = 8.4f),
                onClick = {},
            )
        }
    }

/** A byline line, as an avatar and a name with a time: drawn by the app, so the sample draws a small avatar beside text. */
@Composable
private fun SampleByline() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs)),
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.media_result_byline_avatar_size))
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        )
        Text(
            text = "Greta Lee · 2h",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * The slots a row can carry beyond its text: a byline drawn above the name (an avatar, a name and a time), and an end slot after the text, here a plain
 * label. A row that passes neither is drawn exactly as the others are.
 */
@Composable
fun MediaResultRowBylineAndEndSample() =
    ScreenshotTheme {
        Column(Modifier.padding(dimensionResource(R.dimen.screen_content_inset))) {
            MediaResultRow(
                result = MediaResult("m4", MediaResultKind.Poster, null, "Past Lives", "Movie · 2023", rating = 7.8f),
                onClick = {},
                byline = { SampleByline() },
                end = {
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            )
        }
    }

/** A name suffix after the name in the primary colour, and a very long name beside one: the name ellipsizes and the suffix stays whole. */
@Composable
fun MediaResultRowNameSuffixSample() =
    ScreenshotTheme {
        Column(
            Modifier.padding(dimensionResource(R.dimen.screen_content_inset)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            MediaResultRow(
                result = MediaResult("m5", MediaResultKind.Poster, null, "Past Lives", "Movie", nameSuffix = "(2023)"),
                onClick = {},
            )
            MediaResultRow(
                result =
                    MediaResult(
                        "m6",
                        MediaResultKind.Poster,
                        null,
                        "The Lord of the Rings: The Fellowship of the Ring, Extended Edition",
                        "Movie",
                        nameSuffix = "(2001)",
                        favourite = true,
                    ),
                onClick = {},
            )
        }
    }

/** A long suffix beside a long name: the suffix takes at most half of the width and wraps, and the name keeps the other half, at any font size. */
@Composable
fun MediaResultRowLongSuffixSample() =
    ScreenshotTheme {
        Column(Modifier.padding(dimensionResource(R.dimen.screen_content_inset))) {
            MediaResultRow(
                result =
                    MediaResult(
                        "m10",
                        MediaResultKind.Poster,
                        null,
                        "The Lord of the Rings: The Fellowship of the Ring, Extended Edition",
                        "Series",
                        nameSuffix = "Seasons 1, 2, 4–6",
                    ),
                onClick = {},
            )
        }
    }

/** Status inline: no subtitle line, and one wrapping row that reads the status, the subtitle text, the rating and the meta. */
@Composable
fun MediaResultRowStatusInlineSample() =
    ScreenshotTheme {
        Column(
            Modifier.padding(dimensionResource(R.dimen.screen_content_inset)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            MediaResultRow(
                result =
                    MediaResult(
                        "m7",
                        MediaResultKind.Poster,
                        null,
                        "Past Lives",
                        "Movie · 2023",
                        rating = 7.8f,
                        meta = "Drama, Romance",
                        statusInline = true,
                    ),
                onClick = {},
                trailing = { StatusChip(label = "Available", sentiment = BingeSentiment.Positive) },
            )
            MediaResultRow(
                result = MediaResult("m8", MediaResultKind.Poster, null, "Arrival", "Movie · 2016", statusInline = true),
                onClick = {},
            )
        }
    }

/**
 * A footer under the whole row (here a download's progress and a count of how often the poster was tapped) and a poster that is its own tap target: tap
 * the poster and the count goes up, tap the rest of the row and it does not.
 */
@Composable
fun MediaResultRowFooterAndPosterTapSample() {
    var posterTaps by remember { mutableIntStateOf(0) }
    ScreenshotTheme {
        Column(Modifier.padding(dimensionResource(R.dimen.screen_content_inset))) {
            MediaResultRow(
                result = MediaResult("m9", MediaResultKind.Poster, null, "Past Lives", "Movie · 2023", rating = 7.8f),
                onClick = {},
                footer = {
                    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs))) {
                        LinearProgressIndicator(progress = { SAMPLE_PROGRESS }, modifier = Modifier.fillMaxWidth())
                        Text(
                            text = "Downloading · 42% · poster taps: $posterTaps",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                onPosterClick = { posterTaps++ },
                posterClickLabel = "Open title",
            )
        }
    }
}

private const val SAMPLE_PROGRESS = 0.42f

/** The loading stand-in for a row with a byline and one with its status inline, each over the row it becomes. */
@Composable
fun MediaResultRowSkeletonSlotsSample() =
    ScreenshotTheme {
        val title = "The Lord of the Rings: The Fellowship of the Ring"
        Column(
            Modifier.padding(dimensionResource(R.dimen.screen_content_inset)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            MediaResultRowSkeleton(byline = true, expectedName = title)
            MediaResultRow(
                result = MediaResult("k1", MediaResultKind.Poster, null, title, "Movie · 2001", rating = 8.4f),
                onClick = {},
                byline = { SampleByline() },
            )
            MediaResultRowSkeleton(statusInline = true, expectedName = title)
            MediaResultRow(
                result = MediaResult("k2", MediaResultKind.Poster, null, title, "Movie · 2001", rating = 8.4f, statusInline = true),
                onClick = {},
            )
        }
    }

/** A title with a suffix after it, as a requests screen shows the seasons asked for: neither the title nor the suffix breaks a word, at any font size. */
@Composable
fun NameWithSuffixSample() =
    ScreenshotTheme {
        Column(
            Modifier.padding(dimensionResource(R.dimen.screen_content_inset)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            NameWithSuffix(name = "Peaky Blinders", suffix = "Seasons 1, 2, 4–6")
            NameWithSuffix(name = "Past Lives", suffix = "(2023)")
        }
    }

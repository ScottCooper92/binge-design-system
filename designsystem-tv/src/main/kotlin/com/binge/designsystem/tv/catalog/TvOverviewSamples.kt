@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.tv.template.TvOverviewScreen

private const val SAMPLE_POSTER_URL = "https://example.invalid/poster.jpg"
private const val SCROLLED_PX = 240
private const val LONG_SYNOPSIS_REPEATS = 6

private const val SHORT_TITLE = "Dune: Part Two"

/** Long enough to wrap to three `displaySmall` lines beside the portrait. */
private const val LONG_TITLE =
    "The Lord of the Rings: The Fellowship of the Ring — Extended Edition Remastered in 4K"

private const val SYNOPSIS =
    "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators " +
        "who destroyed his family. Facing a choice between the love of his life and the fate of the " +
        "known universe, he endeavors to prevent a terrible future only he can foresee. The " +
        "spice must flow, and with it the visions that show him every path the war might take."

private val ReaderEntries = listOf(
    "First review" to "A slow, sprawling opening that earns every minute once the desert arrives. $SYNOPSIS",
    "Second review" to "Louder than the first film and better for it. $SYNOPSIS",
    "Third review" to "Not for everyone, and not trying to be. $SYNOPSIS",
)

/** The reader at rest: a portrait beside the title and the text. BACK or the start-edge key closes it. */
@Composable
fun TvOverviewScreenSample() {
    TvOverviewScreen(
        title = SHORT_TITLE,
        overview = SYNOPSIS,
        backdropUrl = null,
        onDismiss = {},
        posterUrl = SAMPLE_POSTER_URL,
        artwork = { SampleWash() },
    )
}

/** A title long enough to wrap to three lines: it still clears the panel's top edge at rest. */
@Composable
fun TvOverviewScreenLongTitleSample() {
    TvOverviewScreen(
        title = LONG_TITLE,
        overview = SYNOPSIS,
        backdropUrl = null,
        onDismiss = {},
        posterUrl = SAMPLE_POSTER_URL,
        artwork = { SampleWash() },
    )
}

/**
 * Scrolled part-way through a long text, with the title still on screen: the overscan inset is content padding
 * inside the scroll, so the text travels into the overscan band rather than stopping a band short.
 */
@Composable
fun TvOverviewScreenScrolledSample() {
    TvOverviewScreen(
        title = LONG_TITLE,
        overview = List(LONG_SYNOPSIS_REPEATS) { SYNOPSIS }.joinToString(" "),
        backdropUrl = null,
        onDismiss = {},
        posterUrl = SAMPLE_POSTER_URL,
        scrollState = rememberScrollState(initial = SCROLLED_PX),
        artwork = { SampleWash() },
    )
}

/** A reader over a list: ← and → step between entries, and BACK is the only way out. */
@Composable
fun TvOverviewScreenReaderSample() {
    var index by remember { mutableIntStateOf(0) }
    val (title, text) = ReaderEntries[index]
    TvOverviewScreen(
        title = title,
        overview = text,
        backdropUrl = null,
        onDismiss = {},
        subtitle = "${index + 1} of ${ReaderEntries.size}",
        onPrevious = { index = (index - 1).coerceAtLeast(0) },
        onNext = { index = (index + 1).coerceAtMost(ReaderEntries.lastIndex) },
        footer = {
            Text(
                text = "← → step between reviews · BACK closes",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        artwork = { SampleWash() },
    )
}

/** Stands in for the artwork a caller supplies; a sample has no network. */
@Composable
private fun SampleWash() {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
}

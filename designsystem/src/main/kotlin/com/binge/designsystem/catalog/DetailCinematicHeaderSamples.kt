@file:OnePerScreen
@file:CatalogGroup("Detail headers")

package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.DetailCinematicHeader
import com.binge.designsystem.component.DetailStat
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [DetailCinematicHeader] — see the convention on
 * [MediaCardRatedSample]. The header is the expanded-width counterpart to [DetailHero]; the
 * catalog screenshot test renders this at an expanded (~900dp) canvas, since the shared
 * wrap-content multipreviews don't reach that width.
 */
@Composable
fun DetailCinematicHeaderSample() {
    ScreenshotTheme {
        DetailCinematicHeader(
            title = "The Dark Knight",
            genres = listOf("Action", "Crime", "Drama"),
            synopsis = "Batman raises the stakes in his war on crime with the help of Lt. Jim Gordon " +
                "and District Attorney Harvey Dent.",
            stats = listOf(
                DetailStat(Icons.Filled.Star, "9.0", "Rating"),
                DetailStat(Icons.Filled.Star, "2008", "Released"),
                DetailStat(Icons.Filled.Star, "2h 32m", "Runtime"),
            ),
            backdropUrl = null,
            posterUrl = null,
        )
    }
}

/**
 * [DetailCinematicHeader] with a tagline in place of the synopsis, for a screen that shows the
 * synopsis further down the page. The copy sits low, just above the facts row.
 */
@Composable
fun DetailCinematicHeaderTaglineSample() {
    ScreenshotTheme {
        DetailCinematicHeader(
            title = "The Dark Knight",
            genres = listOf("Action", "Crime", "Drama"),
            synopsis = null,
            tagline = "Why So Serious?",
            stats = listOf(
                DetailStat(Icons.Filled.Star, "9.0", "Rating"),
                DetailStat(Icons.Filled.Star, "2008", "Released"),
                DetailStat(Icons.Filled.Star, "2h 32m", "Runtime"),
            ),
            backdropUrl = null,
            posterUrl = null,
        )
    }
}

/** [DetailCinematicHeader.titleContent]: a fixed-height block standing in for a title logo. */
@Composable
fun DetailCinematicHeaderTitleContentSample() {
    ScreenshotTheme {
        DetailCinematicHeader(
            title = "The Dark Knight",
            genres = listOf("Action", "Crime", "Drama"),
            synopsis = "Batman raises the stakes in his war on crime with the help of Lt. Jim Gordon " +
                "and District Attorney Harvey Dent.",
            stats = listOf(
                DetailStat(Icons.Filled.Star, "9.0", "Rating"),
                DetailStat(Icons.Filled.Star, "2008", "Released"),
                DetailStat(Icons.Filled.Star, "2h 32m", "Runtime"),
            ),
            backdropUrl = null,
            posterUrl = null,
            titleContent = { TitleLogoStandIn() },
        )
    }
}

/**
 * [DetailCinematicHeader] with a synopsis long enough to overflow, seeded as overflowing so the "Show more" toggle
 * shows on the first frame rather than only after a measure pass.
 */
@Composable
fun DetailCinematicHeaderSynopsisOverflowSample() {
    ScreenshotTheme {
        DetailCinematicHeader(
            title = "The Dark Knight",
            genres = listOf("Action", "Crime", "Drama"),
            synopsis = "Batman raises the stakes in his war on crime with the help of Lt. Jim " +
                "Gordon and District Attorney Harvey Dent, as they team up to dismantle the " +
                "remaining criminal organizations that plague the city streets. However, they " +
                "soon find themselves prey to a reign of chaos unleashed by a rising criminal " +
                "mastermind known to the terrified citizens of Gotham as the Joker.",
            stats = listOf(
                DetailStat(Icons.Filled.Star, "9.0", "Rating"),
                DetailStat(Icons.Filled.Star, "2008", "Released"),
                DetailStat(Icons.Filled.Star, "2h 32m", "Runtime"),
            ),
            backdropUrl = null,
            posterUrl = null,
            synopsisInitiallyOverflowing = true,
        )
    }
}

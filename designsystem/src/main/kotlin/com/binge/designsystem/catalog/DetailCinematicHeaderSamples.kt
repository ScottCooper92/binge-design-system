package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.DetailCinematicHeader
import com.binge.designsystem.component.DetailStat
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [DetailCinematicHeader] (group `"Media"`) — see the convention on
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

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.DetailCinematicHeader
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
            tagline = "Why do we fall? So we can learn to pick ourselves up.",
            backdropUrl = null,
            posterUrl = null,
            actions = {
                BingeFilledButton(label = "Add to list", onClick = {})
            },
        )
    }
}

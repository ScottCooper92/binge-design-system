package com.binge.designsystem.component

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.R
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Reflow-sensitive: how many cards are visible changes with width, so the full matrix.
 * Cards take the `card_width` the hub/detail screens give them in production.
 */
class MediaCarouselScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Ready() {
        ScreenshotTheme {
            MediaCarousel(
                title = "Trending movies",
                items = listOf(
                    "The Dark Knight",
                    "Inception",
                    "Interstellar",
                    "Tenet",
                    "Dunkirk",
                    "Oppenheimer",
                ),
                itemKey = { it },
                onMoreClick = {},
            ) { title ->
                MediaCard(
                    posterUrl = null,
                    title = title,
                    rating = 8.5f,
                    onClick = {},
                    modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
                )
            }
        }
    }
}

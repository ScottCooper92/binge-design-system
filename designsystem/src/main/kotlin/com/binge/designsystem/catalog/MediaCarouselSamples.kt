package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.MediaCard
import com.binge.designsystem.component.MediaCarousel
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [MediaCarousel] (group `"Media"`) — a titled, horizontally-scrolling row of
 * poster cards with a trailing "see all" tile. See the convention on [MediaCardRatedSample].
 */
@Composable
fun MediaCarouselSample() {
    ScreenshotTheme {
        MediaCarousel(
            title = "Trending movies",
            items = listOf("The Dark Knight", "Inception", "Interstellar", "Tenet", "Dunkirk", "Oppenheimer"),
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

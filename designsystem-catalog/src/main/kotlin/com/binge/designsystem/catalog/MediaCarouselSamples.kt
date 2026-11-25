@file:CatalogGroup("Media carousel")

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
 * Public sample for [MediaCarousel] — a titled, horizontally-scrolling row of
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
        ) { _, title ->
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

/**
 * A poster card with a two-line caption and the row's see-all tile, to show where the default tile stands against the card: it lines up with the
 * posters, not with the captions under them, and at 1.3x and 2x only the captions grow. The row passes no `endTileHeight`. A row that wants
 * the tile to span the captions as well passes `Dp.Unspecified`.
 */
@Composable
fun MediaCarouselPosterAlignedEndTileSample() {
    ScreenshotTheme {
        MediaCarousel(
            title = "Trending movies",
            items = listOf("The Lord of the Rings: The Return of the King"),
            itemKey = { it },
            onMoreClick = {},
        ) { _, title ->
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

@file:OnePerScreen

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.HeroCarousel
import com.binge.designsystem.component.HeroItem
import com.binge.designsystem.preview.ScreenshotTheme

/** The hero carousel with three trending titles, each with its rank, a rating and genres, paging sideways. */
@Composable
fun HeroCarouselSample() {
    ScreenshotTheme {
        HeroCarousel(
            items = listOf(
                HeroItem(
                    id = 1,
                    imageUrl = null,
                    title = "Inception",
                    rating = 8.8f,
                    genres = listOf("Sci-Fi", "Action"),
                    rank = 1,
                ),
                HeroItem(
                    id = 2,
                    imageUrl = null,
                    title = "The Dark Knight",
                    rating = 9.0f,
                    genres = listOf("Action", "Crime"),
                    rank = 2,
                ),
                HeroItem(
                    id = 3,
                    imageUrl = null,
                    title = "Interstellar",
                    rating = 8.6f,
                    genres = listOf("Sci-Fi", "Drama"),
                    rank = 3,
                ),
            ),
            onItemClick = {},
        )
    }
}

/** The hero carousel over a list that is not a ranking, such as recent requests: no trending pill on any slide. */
@Composable
fun HeroCarouselUnrankedSample() {
    ScreenshotTheme {
        HeroCarousel(
            items = listOf(
                HeroItem(id = 1, imageUrl = null, title = "Inception", rating = 8.8f, genres = listOf("Sci-Fi", "Action")),
                HeroItem(id = 2, imageUrl = null, title = "The Dark Knight", rating = 9.0f, genres = listOf("Action", "Crime")),
            ),
            onItemClick = {},
        )
    }
}

/**
 * The hero carousel with every line of copy on its first slide: the trending pill, the title, a tagline and the
 * meta row with year and runtime. The second slide carries only a rating and genres.
 */
@Composable
fun HeroCarouselTaglineSample() {
    ScreenshotTheme {
        HeroCarousel(
            items = listOf(
                HeroItem(
                    id = 1,
                    imageUrl = null,
                    title = "Inception",
                    rating = 8.8f,
                    genres = listOf("Sci-Fi", "Action", "Thriller"),
                    year = "2010",
                    runtimeMinutes = 148,
                    tagline = "Your mind is the scene of the crime.",
                    rank = 1,
                ),
                HeroItem(
                    id = 2,
                    imageUrl = null,
                    title = "Interstellar",
                    rating = 8.6f,
                    genres = listOf("Sci-Fi", "Drama"),
                    rank = 2,
                ),
            ),
            onItemClick = {},
        )
    }
}

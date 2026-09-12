package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.HeroCarousel
import com.binge.designsystem.component.HeroItem
import com.binge.designsystem.preview.ScreenshotTheme

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
                ),
                HeroItem(
                    id = 2,
                    imageUrl = null,
                    title = "The Dark Knight",
                    rating = 9.0f,
                    genres = listOf("Action", "Crime"),
                ),
                HeroItem(
                    id = 3,
                    imageUrl = null,
                    title = "Interstellar",
                    rating = 8.6f,
                    genres = listOf("Sci-Fi", "Drama"),
                ),
            ),
            onItemClick = {},
        )
    }
}

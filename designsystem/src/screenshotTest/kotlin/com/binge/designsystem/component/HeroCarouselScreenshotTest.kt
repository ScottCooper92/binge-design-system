package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

private val heroSamples = listOf(
    HeroItem(
        id = 1,
        imageUrl = null,
        title = "Inception",
        rating = 8.8f,
        genres = listOf("Sci-Fi", "Action", "Thriller"),
        year = "2010",
        runtimeMinutes = 148,
        tagline = "Your mind is the scene of the crime.",
    ),
    HeroItem(
        id = 2,
        imageUrl = null,
        title = "Interstellar",
        rating = 8.6f,
        genres = listOf("Sci-Fi", "Drama"),
    ),
)

/**
 * Cinematic full-bleed hero — left-anchored copy (trending pill, title, tagline, meta row) and a dot
 * rail over a left-darkening scrim. The crossfade/autoplay settle on the first item for a stable frame.
 */
class HeroCarouselScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Ready() {
        ScreenshotTheme {
            HeroCarousel(items = heroSamples, onItemClick = {})
        }
    }

    /**
     * Expanded width: the cinematic hero across a wide window — the left-anchored copy and dot rail
     * hold their layout instead of stretching.
     */
    @PreviewTest
    @Preview(name = "wide-light", widthDp = 840, showBackground = true)
    @Composable
    fun Wide() {
        ScreenshotTheme {
            HeroCarousel(items = heroSamples, onItemClick = {})
        }
    }
}

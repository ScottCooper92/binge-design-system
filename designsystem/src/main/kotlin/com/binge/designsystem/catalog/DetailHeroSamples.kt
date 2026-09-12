package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.DetailHero
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [DetailHero] (group `"Media"`). See the convention KDoc on
 * [MediaCardRatedSample].
 *
 * The hero fixes its own height (`R.dimen.detail_hero_height`), so the sample passes no height
 * modifier; it renders top-anchored with the theme background filling below. A null backdrop URL
 * keeps the render deterministic without a network image.
 */
@Composable
fun DetailHeroSample() {
    ScreenshotTheme {
        DetailHero(
            title = "The Dark Knight",
            backdropUrl = null,
            tagline = "Why So Serious?",
            metaText = "9.0 · 2008 · 2h 32m",
            genres = listOf("Action", "Crime", "Drama"),
            onBack = {},
        )
    }
}

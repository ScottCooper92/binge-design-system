@file:OnePerScreen
@file:CatalogGroup("Detail headers")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.DetailHero
import com.binge.designsystem.component.RatingChip
import com.binge.designsystem.component.RatingChipTone
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

/** The [DetailHero.metaContent] slot, a row of chips drawn under the title instead of [DetailHero.metaText]. */
@Composable
fun DetailHeroMetaContentSample() {
    ScreenshotTheme {
        DetailHero(
            title = "The Dark Knight",
            backdropUrl = null,
            tagline = "Why So Serious?",
            metaText = "",
            genres = listOf("Action", "Crime", "Drama"),
            onBack = {},
            metaContent = {
                Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
                    RatingChip(rating = 9.0f)
                    RatingChip(rating = 8.5f, tone = RatingChipTone.Surface)
                }
            },
        )
    }
}

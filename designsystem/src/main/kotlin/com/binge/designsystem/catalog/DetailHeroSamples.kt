@file:OnePerScreen
@file:CatalogGroup("Detail headers")

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.DetailHero
import com.binge.designsystem.component.DetailHeroDefaults
import com.binge.designsystem.component.RatingChip
import com.binge.designsystem.component.RatingChipTone
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [DetailHero]. See the convention KDoc on
 * [MediaCardRatedSample].
 *
 * The hero sets its own height, at least `R.dimen.detail_hero_height`, so the sample passes no height
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

/** The [DetailHero.titleContent] slot, a fixed-height block standing in for a title logo. */
@Composable
fun DetailHeroTitleContentSample() {
    ScreenshotTheme {
        DetailHero(
            title = "The Dark Knight",
            backdropUrl = null,
            tagline = "Why So Serious?",
            metaText = "9.0 · 2008 · 2h 32m",
            genres = listOf("Action", "Crime", "Drama"),
            onBack = {},
            titleContent = { TitleLogoStandIn() },
        )
    }
}

/**
 * A [DetailHero.titleContent] slot that falls back to text, as a title logo does with no logo to show. The plain `Text`
 * inherits the hero's title style and colour from the slot, and takes its shrink from [DetailHeroDefaults].
 */
@Composable
fun DetailHeroTitleFallbackSample() {
    ScreenshotTheme {
        DetailHero(
            title = "The Dark Knight",
            backdropUrl = null,
            tagline = "Why So Serious?",
            metaText = "9.0 · 2008 · 2h 32m",
            genres = listOf("Action", "Crime", "Drama"),
            onBack = {},
            titleContent = { Text(text = "The Dark Knight", maxLines = 2, autoSize = DetailHeroDefaults.titleAutoSize()) },
        )
    }
}

/**
 * Every line of copy at its longest: a title that takes both of its lines, the genre eyebrow, a two-line tagline
 * and the meta line. At a large font scale on a landscape phone this is taller than the band, which grows to hold it
 * rather than running the title up under the back button.
 */
@Composable
fun DetailHeroFullCopySample() {
    ScreenshotTheme {
        DetailHero(
            title = "The Lord of the Rings: The Return of the King, Extended Edition",
            backdropUrl = null,
            tagline = "There can be no triumph without loss. No victory without suffering. No freedom without sacrifice.",
            metaText = "8.9 · 2003 · 4h 23m",
            genres = listOf("Adventure", "Fantasy", "Action"),
            onBack = {},
        )
    }
}

/** A stand-in for a title logo: a block of fixed height, as a consumer would pass. */
@Composable
internal fun TitleLogoStandIn() {
    Box(
        Modifier
            .size(
                width = dimensionResource(R.dimen.detail_title_logo_sample_width),
                height = dimensionResource(R.dimen.detail_title_logo_sample_height),
            ).background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
    )
}

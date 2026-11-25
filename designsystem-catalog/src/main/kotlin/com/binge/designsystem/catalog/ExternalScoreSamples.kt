@file:CatalogGroup("Detail headers")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.DetailCinematicHeader
import com.binge.designsystem.component.DetailHero
import com.binge.designsystem.component.DetailStat
import com.binge.designsystem.component.ExternalScore
import com.binge.designsystem.component.ExternalScoreKind
import com.binge.designsystem.component.ExternalScoreLine
import com.binge.designsystem.component.HeroCarousel
import com.binge.designsystem.component.HeroItem
import com.binge.designsystem.component.MediaCardStatus
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeSentiment

private val EnglishScores = listOf(
    ExternalScore(ExternalScoreKind.CriticsFresh, "97%", "Critics 97 percent"),
    ExternalScore(ExternalScoreKind.Audience, "95%", "Audience 95 percent"),
    ExternalScore(ExternalScoreKind.Imdb, "9.0", "IMDb 9.0"),
)

private val SpanishScores = listOf(
    ExternalScore(ExternalScoreKind.CriticsRotten, "34%", "Crítica 34 por ciento"),
    ExternalScore(ExternalScoreKind.Audience, "71%", "Público 71 por ciento"),
    ExternalScore(ExternalScoreKind.Imdb, "6.2", "IMDb 6.2"),
)

private val Stats = listOf(
    DetailStat(Icons.Filled.Star, "9.0", "Rating"),
    DetailStat(Icons.Filled.Star, "2008", "Released"),
    DetailStat(Icons.Filled.Star, "2h 32m", "Runtime"),
)

/** The score row on its own: a fresh tomato, popcorn and the IMDb-style badge, each with its value. */
@Composable
fun ExternalScoreLineSample() {
    ScreenshotTheme {
        ExternalScoreLine(
            scores = EnglishScores,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        )
    }
}

/** The score row with a rotten critics score (the splat), and Spanish descriptions. */
@Composable
fun ExternalScoreLineRottenSample() {
    ScreenshotTheme {
        ExternalScoreLine(
            scores = SpanishScores,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        )
    }
}

/** [DetailHero] with the scores on their own line after the tagline and the meta text. */
@Composable
fun DetailHeroScoresSample() {
    ScreenshotTheme {
        DetailHero(
            title = "The Dark Knight",
            backdropUrl = null,
            tagline = "Why So Serious?",
            metaText = "2008 · 2h 32m",
            genres = listOf("Action", "Crime", "Drama"),
            externalScores = EnglishScores,
            onBack = {},
        )
    }
}

/** [DetailHero] with Spanish genres and a rotten critics score. */
@Composable
fun DetailHeroScoresSpanishSample() {
    ScreenshotTheme {
        DetailHero(
            title = "El caballero oscuro",
            backdropUrl = null,
            tagline = "¿Por qué tan serio?",
            metaText = "2008 · 2 h 32 min",
            genres = listOf("Acción", "Crimen", "Drama"),
            externalScores = SpanishScores,
            onBack = {},
        )
    }
}

/**
 * [DetailHero] whose genre list alone fills the line, with scores on their own line below. At a large font the genres
 * wrap and the scores stay on the line after the meta text.
 */
@Composable
fun DetailHeroLongGenresSample() {
    ScreenshotTheme {
        DetailHero(
            title = "El caballero oscuro",
            backdropUrl = null,
            tagline = null,
            metaText = "2008 · 2 h 32 min",
            genres = listOf("Acción", "Crimen", "Drama", "Suspense", "Ciencia ficción", "Aventura"),
            externalScores = SpanishScores,
            onBack = {},
        )
    }
}

/** [DetailCinematicHeader] with the scores on their own line after the tagline. */
@Composable
fun DetailCinematicHeaderScoresSample() {
    ScreenshotTheme {
        DetailCinematicHeader(
            title = "The Dark Knight",
            genres = listOf("Action", "Crime", "Drama"),
            synopsis = null,
            tagline = "Why So Serious?",
            stats = Stats,
            backdropUrl = null,
            posterUrl = null,
            externalScores = EnglishScores,
        )
    }
}

/** [HeroCarousel] whose slide carries an Available status after the genres, and the rating with the scores together on their own last line. */
@Composable
fun HeroCarouselScoresSample() {
    ScreenshotTheme {
        HeroCarousel(
            items = listOf(
                HeroItem(
                    id = 1,
                    imageUrl = null,
                    title = "The Dark Knight",
                    rating = 9.0f,
                    genres = listOf("Action", "Crime"),
                    year = "2008",
                    runtimeMinutes = 152,
                    rank = 1,
                    externalScores = EnglishScores,
                    status = MediaCardStatus("Available", BingeSentiment.Positive),
                ),
            ),
            onItemClick = {},
        )
    }
}

/** [HeroCarousel] with a status and no scores: the status follows the rating and the genres. */
@Composable
fun HeroCarouselStatusSample() {
    ScreenshotTheme {
        HeroCarousel(
            items = listOf(
                HeroItem(
                    id = 1,
                    imageUrl = null,
                    title = "Dune",
                    rating = 7.6f,
                    genres = listOf("Science Fiction", "Action"),
                    status = MediaCardStatus("Requested", BingeSentiment.Caution),
                ),
            ),
            onItemClick = {},
        )
    }
}

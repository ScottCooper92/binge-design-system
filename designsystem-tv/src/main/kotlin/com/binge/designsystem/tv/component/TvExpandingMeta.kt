package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.R as TvR

/** The genres then the trailing facts (year, runtime), as one line: the first [genreCount] genres, dropped from the end. */
internal fun tvExpandingFacts(
    genres: List<String>,
    trailing: List<String>,
    genreCount: Int,
): String = (genres.take(genreCount) + trailing).filter { it.isNotBlank() }.joinToString(META_SEPARATOR)

/**
 * Genres and trailing facts, then the rating and certification badges, held to the width it is given. The badges are measured
 * first and always show; genres drop from the end until the rest fits, and the facts alone ellipsize if even they do not.
 */
@Composable
internal fun TvExpandingMeta(
    genres: List<String>,
    trailingFacts: List<String>,
    rating: Float?,
    certification: String?,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.titleMedium
    val color = MaterialTheme.colorScheme.onSurface
    val gap = dimensionResource(TvR.dimen.tv_detail_meta_gap)
    val measurer = rememberTextMeasurer()
    val shownGenres = genres.take(MAX_META_GENRES)
    SubcomposeLayout(modifier) { constraints ->
        val gapPx = gap.roundToPx()
        val badges = subcompose("badges") {
            Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
                TvRatingBadge(rating = rating)
                certification?.let { TvMetaCertification(it) }
            }
        }.map { it.measure(Constraints()) }
        val badgesWidth = badges.maxOfOrNull { it.width } ?: 0
        val available = (constraints.maxWidth - badgesWidth - if (badgesWidth > 0) gapPx else 0).coerceAtLeast(0)
        var genreCount = shownGenres.size
        while (genreCount > 0 && measurer.measure(tvExpandingFacts(shownGenres, trailingFacts, genreCount), style).size.width > available) {
            genreCount--
        }
        val text = tvExpandingFacts(shownGenres, trailingFacts, genreCount)
        val facts = if (text.isEmpty()) {
            emptyList()
        } else {
            subcompose("facts") {
                Text(text = text, style = style, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }.map { it.measure(Constraints(maxWidth = available)) }
        }
        // The badge is the line's tallest part, so its height is held even when a title has no rating: otherwise the line
        // shrinks and whatever sits below it moves as focus crosses rated and unrated titles.
        val reserved =
            subcompose("reserve") { TvRatingBadge(rating = RESERVE_RATING) }.maxOfOrNull { it.measure(Constraints()).height } ?: 0
        val factsWidth = facts.maxOfOrNull { it.width } ?: 0
        val height = maxOf(reserved, (facts + badges).maxOfOrNull { it.height } ?: 0)
        layout(constraints.maxWidth, height) {
            facts.forEach { it.placeRelative(0, (height - it.height) / 2) }
            val badgesX = if (factsWidth > 0) factsWidth + gapPx else 0
            badges.forEach { it.placeRelative(badgesX, (height - it.height) / 2) }
        }
    }
}

/** Any rating the badge would draw; only its height is used, to reserve the badge's room on an unrated title. */
private const val RESERVE_RATING = 9.9f

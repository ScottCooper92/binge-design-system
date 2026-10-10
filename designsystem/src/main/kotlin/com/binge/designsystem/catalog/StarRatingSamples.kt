@file:CatalogGroup("Ratings")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.StarRating
import com.binge.designsystem.preview.ScreenshotTheme
import kotlin.math.roundToInt

/**
 * Public samples for [StarRating] (group `"Ratings"`). See the convention KDoc on
 * [MediaCardRatedSample].
 *
 * Two variants: the compact read-only display row across the scale, and the larger interactive picker.
 * Each row shows its value beside it, so the samples say what they show.
 */
@Composable
fun StarRatingDisplaySample() {
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            DISPLAY_RATINGS.forEach { rating -> RatingLine(rating) { StarRating(rating = rating) } }
        }
    }
}

/**
 * The read-only row across the scale: none, an odd value's half star, a typical score, the values
 * either side of 7.5 (which rounds down to 3.5 stars), and full.
 */
private val DISPLAY_RATINGS = listOf(0f, 3f, 7f, 7.25f, 7.5f, 7.75f, 10f)

/** A rating beside the value it shows, so the sample names its own state. */
@Composable
private fun RatingLine(rating: Float, stars: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        stars()
        Text("${rating.label()} / 10", style = MaterialTheme.typography.bodyMedium)
    }
}

private fun Float.label(): String = if (this % 1f == 0f) roundToInt().toString() else toString()

/** Interactive picker at the larger tap size, on a half-star landing point. */
@Composable
fun StarRatingInteractiveSample() = InteractiveLine(initial = 5f)

/** The picker with nothing chosen yet. */
@Composable
fun StarRatingInteractiveEmptySample() = InteractiveLine(initial = 0f)

/** The lowest rating TMDB accepts, in the picker: a half star rather than an empty row that reads as unrated. */
@Composable
fun StarRatingInteractiveLowestSample() = InteractiveLine(initial = LOWEST_RATING)

private const val LOWEST_RATING = 0.5f

@Composable
private fun InteractiveLine(initial: Float) {
    var rating by remember { mutableFloatStateOf(initial) }
    ScreenshotTheme {
        RatingLine(rating) {
            StarRating(
                rating = rating,
                starSize = dimensionResource(R.dimen.star_rating_size_interactive),
                interactive = true,
                onRatingChange = { rating = it },
            )
        }
    }
}

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.StarRating
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [StarRating] (group `"Media"`). See the convention KDoc on
 * [MediaCardRatedSample].
 *
 * Two variants: the compact read-only display row and the larger interactive picker (which renders
 * an empty, tappable row).
 */
@Composable
fun StarRatingDisplaySample() {
    ScreenshotTheme {
        StarRating(rating = 7f)
    }
}

/** Interactive picker at the larger tap size — empty, with a half-star landing point. */
@Composable
fun StarRatingInteractiveSample() {
    var rating by remember { mutableFloatStateOf(5f) }
    ScreenshotTheme {
        StarRating(
            rating = rating,
            starSize = dimensionResource(R.dimen.star_rating_size_interactive),
            interactive = true,
            onRatingChange = { rating = it },
        )
    }
}

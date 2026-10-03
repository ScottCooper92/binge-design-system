@file:CatalogGroup("Ratings")

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.RatingCard
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [RatingCard] (group `"Cards"`) — see the convention on [MediaCardRatedSample].
 * Covers the card's three states: signed-in prompt (no rating yet), a recorded rating, and the
 * signed-out variant whose footer surfaces the community review count.
 */
@Composable
fun RatingCardUnratedSample() {
    var rating by remember { mutableStateOf<Float?>(null) }
    ScreenshotTheme {
        RatingCard(
            userRating = rating,
            isSignedIn = true,
            reviewCount = 7,
            averageReviewRating = 7.5f,
            onRate = { rating = it },
            onRemoveRating = { rating = null },
            onReviewsClick = {},
        )
    }
}

/** A recorded user rating — the star row collapses to the rated summary with an edit affordance. */
@Composable
fun RatingCardRatedSample() {
    var rating by remember { mutableStateOf<Float?>(9f) }
    ScreenshotTheme {
        RatingCard(
            userRating = rating,
            isSignedIn = true,
            reviewCount = 0,
            averageReviewRating = null,
            onRate = { rating = it },
            onRemoveRating = { rating = null },
            onReviewsClick = {},
        )
    }
}

/** Signed-out with reviews — no rating affordance, just the tappable community-reviews footer. */
@Composable
fun RatingCardSignedOutSample() {
    ScreenshotTheme {
        RatingCard(
            userRating = null,
            isSignedIn = false,
            reviewCount = 12,
            averageReviewRating = 7.2f,
            onRate = {},
            onRemoveRating = {},
            onReviewsClick = {},
        )
    }
}

/** [RatingCard.rateable] false — a title that can't yet be rated, stars non-interactive. */
@Composable
fun RatingCardNotYetRateableSample() {
    ScreenshotTheme {
        RatingCard(
            userRating = null,
            isSignedIn = true,
            rateable = false,
            reviewCount = 0,
            averageReviewRating = null,
            onRate = {},
            onRemoveRating = {},
            onReviewsClick = {},
        )
    }
}

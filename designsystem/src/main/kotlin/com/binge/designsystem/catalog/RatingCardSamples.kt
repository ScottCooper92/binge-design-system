package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.RatingCard
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [RatingCard] (group `"Cards"`) — see the convention on [MediaCardRatedSample].
 * Covers the card's three states: signed-in prompt (no rating yet), a recorded rating, and the
 * signed-out variant whose footer surfaces the community review count.
 */
@Composable
fun RatingCardUnratedSample() {
    ScreenshotTheme {
        RatingCard(
            userRating = null,
            isTv = false,
            isSignedIn = true,
            reviewCount = 7,
            averageReviewRating = 7.5f,
            onRate = {},
            onRemoveRating = {},
            onReviewsClick = {},
        )
    }
}

/** A recorded user rating — the star row collapses to the rated summary with an edit affordance. */
@Composable
fun RatingCardRatedSample() {
    ScreenshotTheme {
        RatingCard(
            userRating = 9f,
            isTv = false,
            isSignedIn = true,
            reviewCount = 0,
            averageReviewRating = null,
            onRate = {},
            onRemoveRating = {},
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
            isTv = false,
            isSignedIn = false,
            reviewCount = 12,
            averageReviewRating = 7.2f,
            onRate = {},
            onRemoveRating = {},
            onReviewsClick = {},
        )
    }
}

package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.theme.BingeExpressiveTheme

class RatingCardScreenshotTest {
    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun RatingCardUnratedLight() {
        BingeExpressiveTheme(darkTheme = false, dynamicColor = false) {
            RatingCard(
                userRating = null,
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

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun RatingCardUnratedDark() {
        BingeExpressiveTheme(darkTheme = true, dynamicColor = false) {
            RatingCard(
                userRating = null,
                isTv = true,
                isSignedIn = true,
                reviewCount = 0,
                averageReviewRating = null,
                onRate = {},
                onRemoveRating = {},
                onReviewsClick = {},
            )
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun RatingCardRatedLight() {
        BingeExpressiveTheme(darkTheme = false, dynamicColor = false) {
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

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun RatingCardRatedDark() {
        BingeExpressiveTheme(darkTheme = true, dynamicColor = false) {
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

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun RatingCardWithReviewsFooterLight() {
        BingeExpressiveTheme(darkTheme = false, dynamicColor = false) {
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

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun RatingCardWithReviewsFooterDark() {
        BingeExpressiveTheme(darkTheme = true, dynamicColor = false) {
            RatingCard(
                userRating = null,
                isTv = true,
                isSignedIn = true,
                reviewCount = 7,
                averageReviewRating = 7.5f,
                onRate = {},
                onRemoveRating = {},
                onReviewsClick = {},
            )
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun RatingCardRatedWithReviewsFooterLight() {
        BingeExpressiveTheme(darkTheme = false, dynamicColor = false) {
            RatingCard(
                userRating = 9f,
                isTv = false,
                isSignedIn = true,
                reviewCount = 12,
                averageReviewRating = null,
                onRate = {},
                onRemoveRating = {},
                onReviewsClick = {},
            )
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun RatingCardRatedWithReviewsFooterDark() {
        BingeExpressiveTheme(darkTheme = true, dynamicColor = false) {
            RatingCard(
                userRating = 9f,
                isTv = false,
                isSignedIn = true,
                reviewCount = 12,
                averageReviewRating = null,
                onRate = {},
                onRemoveRating = {},
                onReviewsClick = {},
            )
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun RatingCardSignedOutWithReviewsLight() {
        BingeExpressiveTheme(darkTheme = false, dynamicColor = false) {
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

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun RatingCardSignedOutWithReviewsDark() {
        BingeExpressiveTheme(darkTheme = true, dynamicColor = false) {
            RatingCard(
                userRating = null,
                isTv = true,
                isSignedIn = false,
                reviewCount = 3,
                averageReviewRating = null,
                onRate = {},
                onRemoveRating = {},
                onReviewsClick = {},
            )
        }
    }
}

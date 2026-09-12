package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme

class StarRatingScreenshotTest {
    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun StarRatingDisplayLight() {
        BingeExpressiveTheme(darkTheme = false, dynamicColor = false) {
            StarRating(rating = 7f)
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun StarRatingDisplayDark() {
        BingeExpressiveTheme(darkTheme = true, dynamicColor = false) {
            StarRating(rating = 7f)
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun StarRatingInteractive() {
        BingeExpressiveTheme(darkTheme = false, dynamicColor = false) {
            StarRating(
                rating = 0f,
                starSize = dimensionResource(R.dimen.star_rating_size_interactive),
                interactive = true,
                onRatingChange = {},
            )
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun StarRatingHalfStarLight() {
        BingeExpressiveTheme(darkTheme = false, dynamicColor = false) {
            StarRating(rating = 5f, starSize = dimensionResource(R.dimen.star_rating_size_interactive))
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun StarRatingHalfStarDark() {
        BingeExpressiveTheme(darkTheme = true, dynamicColor = false) {
            StarRating(rating = 5f, starSize = dimensionResource(R.dimen.star_rating_size_interactive))
        }
    }
}

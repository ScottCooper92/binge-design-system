package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * Screenshot coverage for the poster-card catalog samples, on the colour axis only. The samples bound their own card
 * width, so no `widthDp` is needed here.
 */
class MediaCardSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "rated", uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "rated-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Rated() {
        MediaCardRatedSample()
    }

    @PreviewTest
    @Preview(name = "unrated", uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "unrated-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun LongTitleUnrated() {
        MediaCardUnratedSample()
    }

    /**
     * The placeholder beside the card it stands in for: it has to reserve the two-line title band as well as
     * the poster, or every grid row below the first drops when the cards arrive.
     */
    @PreviewTest
    @Preview(name = "skeleton", uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "skeleton-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Skeleton() {
        MediaCardSkeletonSample()
    }

    @PreviewTest
    @Preview(name = "library", uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "library-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun LibraryAction() {
        MediaCardLibrarySample()
    }

    @PreviewTest
    @Preview(name = "status", uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "status-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Status() {
        MediaCardStatusSample()
    }

    @PreviewTest
    @Preview(name = "sentiments", uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "sentiments-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun StatusSentiments() {
        MediaCardStatusSentimentsSample()
    }

    @PreviewTest
    @Preview(name = "dark-poster", uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "dark-poster-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun StatusDarkPoster() {
        MediaCardStatusDarkPosterSample()
    }

    @PreviewTest
    @Preview(name = "light-poster", uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "light-poster-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun StatusLightPoster() {
        MediaCardStatusLightPosterSample()
    }

    @PreviewTest
    @Preview(name = "status-action", uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "status-action-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun StatusWithAction() {
        MediaCardStatusWithActionSample()
    }

    @PreviewTest
    @Preview(name = "progress", uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "progress-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Progress() {
        MediaCardProgressSample()
    }

    @PreviewTest
    @Preview(name = "episode", uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "episode-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Episode() {
        MediaCardEpisodeSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun Favourite() {
        MediaCardFavouriteSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun FavouriteWithStatus() {
        MediaCardFavouriteWithStatusSample()
    }

    @PreviewTest
    @Preview(name = "fav-sentiments", uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "fav-sentiments-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun FavouriteSentiments() {
        MediaCardFavouriteSentimentsSample()
    }

    @PreviewTest
    @Preview(name = "fav-light-poster", uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "fav-light-poster-dark", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun FavouriteLightPoster() {
        MediaCardFavouriteLightPosterSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun FavouriteWide() {
        MediaCardFavouriteWideSample()
    }
}

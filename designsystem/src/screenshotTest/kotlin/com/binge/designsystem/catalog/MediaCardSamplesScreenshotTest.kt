package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

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
}

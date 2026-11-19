package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.PHONE_PORTRAIT_SPEC
import com.binge.designsystem.preview.SOURCE_LOCALE

/** Screenshot coverage for the season-grouped episode list samples. */
class EpisodeBrowseSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun row() {
        EpisodeBrowseRowSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun rowAirDate() {
        EpisodeBrowseRowAirDateSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun rowSkeleton() {
        EpisodeBrowseRowSkeletonSample()
    }

    @PreviewTest
    @Preview(name = "phone", device = PHONE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Preview(name = "font15", device = PHONE_PORTRAIT_SPEC, fontScale = 1.5f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun list() {
        SeasonSectionsListSample()
    }

    @PreviewTest
    @Preview(name = "phone", device = PHONE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun listLoading() {
        SeasonSectionsListLoadingSample()
    }

    @PreviewTest
    @Preview(name = "phone", device = PHONE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun listFailed() {
        SeasonSectionsListFailedSample()
    }

    @PreviewTest
    @Preview(name = "es", device = PHONE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = "es")
    @Composable
    fun listSpanish() {
        SeasonSectionsListSpanishSample()
    }
}

package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.FOLDABLE_PORTRAIT_SPEC
import com.binge.designsystem.preview.PHONE_PORTRAIT_SPEC
import com.binge.designsystem.preview.SOURCE_LOCALE
import com.binge.designsystem.preview.ScreenStatePreview
import com.binge.designsystem.preview.ScreenWidthPreviews
import com.binge.designsystem.preview.TabletScreenPreview

/**
 * Screenshot coverage for the movie template: the loaded page at a phone's width and at 1.3x and 2x font, at the expanded width (the
 * tablet's and the foldable's, where the cinematic header takes over), its skeleton at the same sizes, and both failures.
 */
class MovieDetailScreenSamplesScreenshotTest {
    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun page() = MovieDetailScreenSample()

    @PreviewTest
    @Preview(name = "phone-font13", device = PHONE_PORTRAIT_SPEC, fontScale = 1.3f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Preview(name = "phone-font20", device = PHONE_PORTRAIT_SPEC, fontScale = 2.0f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun pageLargeFont() = MovieDetailScreenSample()

    @PreviewTest
    @Preview(name = "foldable", device = FOLDABLE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun pageWide() = MovieDetailScreenSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun loading() = MovieDetailScreenLoadingSample()

    @PreviewTest
    @Preview(name = "phone-font20", device = PHONE_PORTRAIT_SPEC, fontScale = 2.0f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun loadingLargeFont() = MovieDetailScreenLoadingSample()

    @PreviewTest
    @TabletScreenPreview
    @Composable
    fun loadingWide() = MovieDetailScreenLoadingSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun failed() = MovieDetailScreenFailedSample()

    @PreviewTest
    @Preview(name = "phone-font20", device = PHONE_PORTRAIT_SPEC, fontScale = 2.0f, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
    @Composable
    fun failedLargeFont() = MovieDetailScreenFailedSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun notFound() = MovieDetailScreenNotFoundSample()

    /** The failure at the expanded width: the Back control sits where the loading page's does, at the cinematic header's inset. */
    @PreviewTest
    @TabletScreenPreview
    @Composable
    fun failedWide() = MovieDetailScreenFailedSample()

    /** The skeleton of a title that promises little: no primary, no rails, so no carousel placeholder. */
    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun loadingBare() = MovieDetailScreenLoadingBareSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun roundActionsOnly() = MovieDetailScreenRoundActionsOnlySample()
}

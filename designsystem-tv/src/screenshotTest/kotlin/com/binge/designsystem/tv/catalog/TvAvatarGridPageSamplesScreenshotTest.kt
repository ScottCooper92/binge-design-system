package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvPreviewsOnBlack
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/**
 * The avatar grid page: the heading over the faces, the uncredited face focused, loading, a failure with and without Try again,
 * and the empty message. The fixtures carry no artwork URLs, so every face renders its person-glyph plate, the deterministic frame.
 */
class TvAvatarGridPageSamplesScreenshotTest {
    @PreviewTest
    @TvPreviewsOnBlack
    @Composable
    fun populatedWithFocusedCell() = Frame { TvAvatarGridPageSample() }

    @PreviewTest
    @TvPreviewsOnBlack
    @Composable
    fun blankRoleCellFocused() = Frame { TvAvatarGridPageBlankRoleSample() }

    @PreviewTest
    @TvPreviewsOnBlack
    @Composable
    fun loading() = Frame { TvAvatarGridPageLoadingSample() }

    @PreviewTest
    @TvPreviewsOnBlack
    @Composable
    fun error() = Frame { TvAvatarGridPageErrorSample() }

    @PreviewTest
    @TvPreviewsOnBlack
    @Composable
    fun errorRetryFocused() = Frame { TvAvatarGridPageErrorFocusedSample() }

    @PreviewTest
    @TvPreviewsOnBlack
    @Composable
    fun errorNoRetry() = Frame { TvAvatarGridPageErrorNoRetrySample() }

    @PreviewTest
    @TvPreviewsOnBlack
    @Composable
    fun empty() = Frame { TvAvatarGridPageEmptySample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun castCells() = TvScreenshotTheme(modifier = Modifier.fillMaxSize()) { TvCastCardSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun castLoadingAndErrorPlatesAgree() = TvScreenshotTheme(modifier = Modifier.fillMaxSize()) { TvCastCardPlatesSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun placeholderPostersByKind() = TvScreenshotTheme(modifier = Modifier.fillMaxSize()) { TvImagePlaceholderPostersSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun placeholderTitleLengths() = TvScreenshotTheme(modifier = Modifier.fillMaxSize()) { TvImagePlaceholderTitleLengthsSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun placeholderLandscapeSlots() = TvScreenshotTheme(modifier = Modifier.fillMaxSize()) { TvImagePlaceholderLandscapeSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun placeholderPersonCircleNoTitle() = TvScreenshotTheme(modifier = Modifier.fillMaxSize()) { TvImagePlaceholderPersonCircleSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        content()
    }
}

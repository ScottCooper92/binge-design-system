package com.binge.designsystem.tv.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviewsOnBlack

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

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        content()
    }
}

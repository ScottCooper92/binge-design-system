package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** Screenshot coverage for the TV page templates: one frame per sample, each a whole panel. */
class TvTemplateSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun twoPanePageSignIn() = Frame { TvTwoPanePageSignInSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun twoPanePageForm() = Frame { TvTwoPanePageFormSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun twoPanePageListDetail() = Frame { TvTwoPanePageListDetailSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun twoPanePageActionFirst() = Frame { TvTwoPanePageActionFirstSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun twoPanePageBalanced() = Frame { TvTwoPanePageBalancedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun twoPanePageBoard() = Frame { TvTwoPanePageBoardSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun stepFlow() = Frame { TvStepFlowSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun stepFlowLoading() = Frame { TvStepFlowLoadingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun stepFlowChromeOverContent() = Frame { TvStepFlowChromeOverContentSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun board() = Frame { TvBoardSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun boardOverlay() = Frame { TvBoardOverlaySample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun messagePage() = Frame { TvMessagePageSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun messagePageNoAction() = Frame { TvMessagePageNoActionSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun messagePageLoading() = Frame { TvMessagePageLoadingSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** A message plate in a lazy list, in intrinsic-size parents and in a filling box: each lays out with the copy and the action showing. */
class TvMessagePlateHostSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun inLazyColumn() = Frame { TvMessagePlateInLazyColumnSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun inIntrinsicMin() = Frame { TvMessagePlateInIntrinsicMinSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun inIntrinsicMax() = Frame { TvMessagePlateInIntrinsicMaxSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun inBox() = Frame { TvMessagePlateInBoxSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

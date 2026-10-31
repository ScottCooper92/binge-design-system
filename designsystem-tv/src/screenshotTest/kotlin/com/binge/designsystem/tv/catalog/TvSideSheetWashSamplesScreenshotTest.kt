package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

class TvSideSheetWashSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun wash() {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) { TvSideSheetWashSample() }
    }
}

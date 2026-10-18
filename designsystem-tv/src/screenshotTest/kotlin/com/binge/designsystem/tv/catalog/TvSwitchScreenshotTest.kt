package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** The destructive switch checked, focused above and unfocused below, so the pair reads as one colour (#911). */
class TvSwitchScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun destructiveChecked() = Frame { TvSwitchDestructiveCheckedSample() }
}

@Composable
private fun Frame(content: @Composable () -> Unit) {
    TvScreenshotTheme(modifier = Modifier.fillMaxSize()) { content() }
}

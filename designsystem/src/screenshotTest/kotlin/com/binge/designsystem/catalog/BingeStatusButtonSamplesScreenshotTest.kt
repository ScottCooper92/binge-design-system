package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.LocalePreviews
import com.binge.designsystem.preview.STANDARD_PHONE_WIDTH_DP
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for the [BingeStatusButton][com.binge.designsystem.component.BingeStatusButton] samples. */
class BingeStatusButtonSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun States() {
        BingeStatusButtonSample()
    }

    @PreviewTest
    @LocalePreviews
    @Preview(name = "es-font20", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 2.0f, locale = "es", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Spanish() {
        BingeStatusButtonSpanishSample()
    }
}

package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/** A tablet-width canvas, where the sheet is used. */
private const val SIDE_SHEET_DEVICE = "spec:width=840dp,height=480dp"

class BingeModalSideSheetSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "tablet-light", device = SIDE_SHEET_DEVICE, uiMode = UI_MODE_NIGHT_NO)
    @Preview(name = "tablet-dark", device = SIDE_SHEET_DEVICE, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Panel() {
        BingeModalSideSheetSample()
    }
}

package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.STANDARD_PHONE_WIDTH_DP

private const val TABLET_WIDTH_DP = 840

/** Screenshot coverage for the filter sheet's body, on a phone and a tablet, at the worst-case font scales and in Spanish. */
class FilterSheetSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "phone", widthDp = STANDARD_PHONE_WIDTH_DP)
    @Preview(name = "phone-dark", widthDp = STANDARD_PHONE_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
    @Preview(name = "phone-font13", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 1.3f)
    @Preview(name = "phone-font20-dark", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 2.0f, uiMode = UI_MODE_NIGHT_YES)
    @Preview(name = "tablet", widthDp = TABLET_WIDTH_DP)
    @Composable
    fun Sheet() {
        FilterSheetSample()
    }

    @PreviewTest
    @Preview(name = "es", widthDp = STANDARD_PHONE_WIDTH_DP, locale = "es")
    @Preview(name = "es-font13", widthDp = STANDARD_PHONE_WIDTH_DP, locale = "es", fontScale = 1.3f)
    @Preview(name = "es-font20-dark", widthDp = STANDARD_PHONE_WIDTH_DP, locale = "es", fontScale = 2.0f, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun SheetSpanish() {
        FilterSheetSpanishSample()
    }
}

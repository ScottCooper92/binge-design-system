package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.STANDARD_PHONE_WIDTH_DP

private const val TABLET_WIDTH_DP = 840

/** Screenshot coverage for a filter's own sheet: the date range, a range slider, and a footer accessory. */
class FilterSectionSheetSamplesScreenshotTest {
    @PreviewTest
    @Preview(name = "phone", widthDp = STANDARD_PHONE_WIDTH_DP)
    @Preview(name = "phone-dark", widthDp = STANDARD_PHONE_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
    @Preview(name = "phone-font13", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 1.3f)
    @Preview(name = "phone-font20-dark", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 2.0f, uiMode = UI_MODE_NIGHT_YES)
    @Preview(name = "tablet", widthDp = TABLET_WIDTH_DP)
    @Composable
    fun Dates() {
        FilterSectionDatesSample()
    }

    @PreviewTest
    @Preview(name = "es", widthDp = STANDARD_PHONE_WIDTH_DP, locale = "es")
    @Preview(name = "es-font20-dark", widthDp = STANDARD_PHONE_WIDTH_DP, locale = "es", fontScale = 2.0f, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun DatesSpanish() {
        FilterSectionDatesSpanishSample()
    }

    @PreviewTest
    @Preview(name = "phone", widthDp = STANDARD_PHONE_WIDTH_DP)
    @Preview(name = "phone-dark", widthDp = STANDARD_PHONE_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
    @Preview(name = "phone-font20", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 2.0f)
    @Composable
    fun Range() {
        FilterSectionRangeSample()
    }

    @PreviewTest
    @Preview(name = "phone", widthDp = STANDARD_PHONE_WIDTH_DP)
    @Preview(name = "phone-font20-dark", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 2.0f, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun Accessory() {
        FilterSectionAccessorySample()
    }
}

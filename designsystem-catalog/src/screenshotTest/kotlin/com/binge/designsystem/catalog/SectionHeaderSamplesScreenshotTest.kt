package com.binge.designsystem.catalog

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.STANDARD_PHONE_WIDTH_DP
import com.binge.designsystem.preview.WorstCaseFontPreviews

class SectionHeaderSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WithMore() {
        SectionHeaderWithMoreSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleOnly() {
        SectionHeaderTitleOnlySample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun LongTitle() {
        SectionHeaderLongTitleSample()
    }

    @PreviewTest
    @Preview(name = "es", widthDp = STANDARD_PHONE_WIDTH_DP, locale = "es")
    @Preview(name = "es-font13", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 1.3f, locale = "es", uiMode = UI_MODE_NIGHT_YES)
    @Preview(name = "es-font20", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 2.0f, locale = "es", uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun LongTitleSpanish() {
        SectionHeaderLongTitleSpanishSample()
    }
}

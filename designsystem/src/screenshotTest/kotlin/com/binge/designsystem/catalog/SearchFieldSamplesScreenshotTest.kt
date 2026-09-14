package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the [BingeSearchField] catalog samples — renders the shared samples, the
 * component's one public fixture.
 */
class SearchFieldSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Empty() {
        SearchFieldEmptySample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WithQuery() {
        SearchFieldWithQuerySample()
    }
}

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for [com.binge.designsystem.component.BingeFieldsSheetContent]: ready, empty, refused and submitting. */
class BingeFieldsSheetSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ready() = BingeFieldsSheetSample()

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun empty() = BingeFieldsSheetEmptySample()

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun refused() = BingeFieldsSheetErrorSample()

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun submitting() = BingeFieldsSheetSubmittingSample()
}

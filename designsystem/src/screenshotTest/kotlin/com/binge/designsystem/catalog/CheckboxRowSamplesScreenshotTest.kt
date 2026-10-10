package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [CheckboxRow][com.binge.designsystem.component.CheckboxRow] catalog samples. */
class CheckboxRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun row() {
        CheckboxRowSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun list() {
        CheckboxRowListSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun longLabel() {
        CheckboxRowLongLabelSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun people() {
        CheckboxRowPeopleSample()
    }
}

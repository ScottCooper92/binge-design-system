package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the [ReorderableHandleRow][com.binge.designsystem.component.ReorderableHandleRow]
 * primitive catalog samples — renders the shared samples, the primitive's one public fixture.
 */
class ReorderableHandleRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun row() {
        ReorderableHandleRowSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun firstRow() {
        ReorderableHandleRowFirstSample()
    }
}

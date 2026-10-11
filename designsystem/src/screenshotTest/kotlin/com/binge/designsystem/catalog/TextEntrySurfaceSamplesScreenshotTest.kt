package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the [TextEntrySurface] catalog samples — renders the shared samples, the
 * component's one public fixture.
 */
class TextEntrySurfaceSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Default() {
        TextEntrySurfaceSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Error() {
        TextEntrySurfaceErrorSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Counter() {
        TextEntrySurfaceCounterSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Masked() {
        TextEntrySurfaceMaskedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Empty() {
        TextEntrySurfaceEmptySample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Submitting() {
        TextEntrySurfaceSubmittingSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun LongContent() {
        TextEntrySurfaceLongContentSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WithTypePicker() {
        TextEntrySurfaceWithTypePickerSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Collapsible() {
        TextEntrySurfaceCollapsibleSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Expanded() {
        TextEntrySurfaceExpandedSample()
    }
}

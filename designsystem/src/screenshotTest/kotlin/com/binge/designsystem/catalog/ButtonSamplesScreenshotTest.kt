package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the combined button-family catalog sample. Renders the shared
 * [ButtonFamilySample], the group's one public fixture. This is the template a per-group
 * ticket follows when a group sample has no single-component screenshot test of its own.
 */
class ButtonSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Family() {
        ButtonFamilySample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Destructive() {
        OutlinedButtonDestructiveSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun DestructiveFilled() {
        FilledButtonDestructiveSample()
    }
}

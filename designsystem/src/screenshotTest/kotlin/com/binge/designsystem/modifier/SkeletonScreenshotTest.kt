package com.binge.designsystem.modifier

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.catalog.SkeletonSample
import com.binge.designsystem.preview.ComponentPreviews

/**
 * The `Modifier.skeleton` primitive across the shapes it commonly clips, rendered from [SkeletonSample]. Its
 * [ScreenshotTheme][com.binge.designsystem.preview.ScreenshotTheme] reduces motion, so the shimmer freezes at a
 * fixed offset and the baseline is deterministic.
 */
class SkeletonScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Shapes() = SkeletonSample()
}

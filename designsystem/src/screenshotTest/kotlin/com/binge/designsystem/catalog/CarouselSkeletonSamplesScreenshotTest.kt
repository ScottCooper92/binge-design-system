package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class CarouselSkeletonSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun CarouselSkeleton() {
        CarouselSkeletonSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun HubScreen() {
        HubScreenSkeletonSample()
    }
}

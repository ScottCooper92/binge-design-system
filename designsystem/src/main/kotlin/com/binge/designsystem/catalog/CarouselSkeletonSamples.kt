@file:CatalogGroup("Media carousel")

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.CarouselSkeleton
import com.binge.designsystem.preview.ScreenshotTheme

/** The media carousel while its titles load: a row of poster placeholders under a title bar. */
@Composable
fun CarouselSkeletonSample() {
    ScreenshotTheme {
        CarouselSkeleton()
    }
}

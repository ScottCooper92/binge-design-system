package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.CarouselSkeleton
import com.binge.designsystem.component.HubScreenSkeleton
import com.binge.designsystem.preview.ScreenshotTheme

@Composable
fun CarouselSkeletonSample() {
    ScreenshotTheme {
        CarouselSkeleton()
    }
}

/** `HubScreenSkeleton` is `fillMaxSize`, so the sample bounds the height. */
@Composable
fun HubScreenSkeletonSample() {
    ScreenshotTheme(modifier = Modifier.height(dimensionResource(R.dimen.catalog_screen_preview_height))) {
        HubScreenSkeleton()
    }
}

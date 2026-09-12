package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.DetailScreenSkeleton
import com.binge.designsystem.preview.ScreenshotTheme

/** `DetailScreenSkeleton` is `fillMaxSize`, so the sample bounds the height. */
@Composable
fun DetailScreenSkeletonSample() {
    ScreenshotTheme(modifier = Modifier.height(dimensionResource(R.dimen.catalog_screen_preview_height))) {
        DetailScreenSkeleton()
    }
}

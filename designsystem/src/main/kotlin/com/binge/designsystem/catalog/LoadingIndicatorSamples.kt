package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeLoadingIndicator
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * `BingeLoadingIndicator` wraps an indeterminate spinner; with `reduceMotion = true` the captured
 * frame is the sweep's resting position, which only reliably renders a visible arc when the indicator
 * is centred in a bounded box (a bare wrap-content layout can freeze at a near-zero sweep). The
 * sample mirrors how `LoadingScreen` hosts it so the catalogued cell is never blank.
 */
@Composable
fun LoadingIndicatorSample() {
    ScreenshotTheme {
        Box(
            modifier = Modifier.size(dimensionResource(R.dimen.state_icon_container_size)),
            contentAlignment = Alignment.Center,
        ) {
            BingeLoadingIndicator()
        }
    }
}

@file:CatalogGroup("Top app bars")

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.StatusBarScrim
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * [com.binge.designsystem.component.StatusBarScrim] over a bright backdrop, standing still: the wash is
 * darkest at the top edge and fades out at the status bar's own edge. The frame wraps it in
 * `PreviewSystemBarInsets`, because a preview reports the status bar as zero tall and would draw no wash.
 */
@Composable
fun StatusBarScrimSample() {
    ScreenshotTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.catalog_demo_artwork_height))
                .background(MaterialTheme.colorScheme.tertiaryContainer),
        ) {
            StatusBarScrim()
        }
    }
}

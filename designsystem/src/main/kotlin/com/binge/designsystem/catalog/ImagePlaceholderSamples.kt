package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.ImagePlaceholder
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [ImagePlaceholder] (group `"Media"`). See the convention KDoc on
 * [MediaCardRatedSample].
 *
 * The placeholder fills whatever bounds it is given, so the sample pins a poster-class square via
 * `R.dimen.card_width` rather than letting it stretch to the canvas — matching how it renders in a
 * poster cell while an image loads.
 */
@Composable
fun ImagePlaceholderSample() {
    ScreenshotTheme {
        ImagePlaceholder(Modifier.size(dimensionResource(R.dimen.card_width)))
    }
}

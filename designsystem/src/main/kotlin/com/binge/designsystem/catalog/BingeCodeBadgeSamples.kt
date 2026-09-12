package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeCodeBadge
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [BingeCodeBadge] — the bordered short-code badge (language / country /
 * certification codes). Catalog under `"Badges"`; see [MediaCardRatedSample] for the convention.
 */
@Composable
fun BingeCodeBadgeSample() {
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            BingeCodeBadge(label = "EN")
            BingeCodeBadge(label = "US")
            BingeCodeBadge(label = "PG-13")
        }
    }
}

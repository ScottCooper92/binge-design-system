package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.DiscoverQuickClearChip
import com.binge.designsystem.component.QuickClearTone
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [DiscoverQuickClearChip] — catalog under `"Chips"` (see [MediaCardRatedSample]
 * for the convention). Both [QuickClearTone]s share the cell: Include (secondary container) and
 * Exclude (error wash), each with its trailing clear affordance.
 */
@Composable
fun DiscoverQuickClearChipSample() {
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            DiscoverQuickClearChip(label = "Action", tone = QuickClearTone.Include, onClear = {})
            DiscoverQuickClearChip(label = "Horror", tone = QuickClearTone.Exclude, onClear = {})
        }
    }
}

@file:CatalogGroup("Chips")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    // Clearing a chip removes it, so once both are gone a button puts them back.
    var showAction by remember { mutableStateOf(true) }
    var showHorror by remember { mutableStateOf(true) }
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            if (showAction) {
                DiscoverQuickClearChip(label = "Action", tone = QuickClearTone.Include, onClear = { showAction = false })
            }
            if (showHorror) {
                DiscoverQuickClearChip(label = "Horror", tone = QuickClearTone.Exclude, onClear = { showHorror = false })
            }
            if (!showAction && !showHorror) {
                TextButton(onClick = {
                    showAction = true
                    showHorror = true
                }) { Text("Show the chips again") }
            }
        }
    }
}

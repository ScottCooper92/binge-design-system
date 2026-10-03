@file:CatalogGroup("Chips")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.IncludeExcludeChip
import com.binge.designsystem.component.IncludeExcludeState
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [IncludeExcludeChip] — catalog under `"Chips"` (see [MediaCardRatedSample] for
 * the convention). Every [IncludeExcludeState] gets a cell: Neutral (outlined), Include (check,
 * filled) and Exclude (cross, strikethrough).
 */
@Composable
fun IncludeExcludeChipStatesSample() {
    // Seeded with one chip per state. A tap toggles Include and a long press toggles Exclude, as
    // Binge's Discover filters do; the chip itself leaves that to its caller.
    val states = remember {
        mutableStateListOf(IncludeExcludeState.Neutral, IncludeExcludeState.Include, IncludeExcludeState.Exclude)
    }
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            listOf("Drama", "Action", "Horror").forEachIndexed { index, label ->
                IncludeExcludeChip(
                    label = label,
                    state = states[index],
                    onTap = { states[index] = states[index].toggled(IncludeExcludeState.Include) },
                    onLongPress = { states[index] = states[index].toggled(IncludeExcludeState.Exclude) },
                )
            }
        }
    }
}

private fun IncludeExcludeState.toggled(to: IncludeExcludeState) = if (this == to) IncludeExcludeState.Neutral else to

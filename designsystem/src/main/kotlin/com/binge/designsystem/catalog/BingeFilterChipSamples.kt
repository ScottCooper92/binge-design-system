@file:CatalogGroup("Chips")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilterChip
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for the filter-chip family — catalog under `"Chips"` (see the convention KDoc on
 * [MediaCardRatedSample]).
 *
 * The chip's variants — selected (primary fill), unselected (outlined), an optional leading icon and
 * a trailing count sub-pill — sit in one row so the contrast is catalogued in a single cell.
 */
@Composable
fun BingeFilterChipFamilySample() {
    // Seeded with the frame's selection, then each chip toggles on its own.
    var all by remember { mutableStateOf(true) }
    var action by remember { mutableStateOf(false) }
    var trending by remember { mutableStateOf(false) }
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            BingeFilterChip(label = "All", selected = all, onClick = { all = !all }, count = 142)
            BingeFilterChip(label = "Action", selected = action, onClick = { action = !action })
            BingeFilterChip(
                label = "Trending",
                selected = trending,
                onClick = { trending = !trending },
                leadingIcon = Icons.Filled.Star,
            )
        }
    }
}

/**
 * The chip while its form is saving: selected and unselected both dim, and neither takes a tap.
 * Its own cell, because a chip that swallows a tap silently looks the same as one that took it.
 */
@Composable
fun BingeFilterChipDisabledSample() {
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            BingeFilterChip(label = "All", selected = true, onClick = {}, count = 142, enabled = false)
            BingeFilterChip(label = "Action", selected = false, onClick = {}, enabled = false)
        }
    }
}

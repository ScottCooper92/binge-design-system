package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
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
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            BingeFilterChip(label = "All", selected = true, onClick = {}, count = 142)
            BingeFilterChip(label = "Action", selected = false, onClick = {})
            BingeFilterChip(
                label = "Trending",
                selected = false,
                onClick = {},
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

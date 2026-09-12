package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.TrendingSearchChip
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [TrendingSearchChip] — catalog under `"Chips"` (see [MediaCardRatedSample] for
 * the convention). Two ranked entries show the zero-padded rank prefix, and a third the unranked
 * history form the same row uses for the user's own past searches.
 */
@Composable
fun TrendingSearchChipSample() {
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            TrendingSearchChip(rank = 1, label = "Severance", onClick = {})
            TrendingSearchChip(rank = 12, label = "The Bear", onClick = {})
            TrendingSearchChip(label = "dune", leadingIcon = Icons.Filled.History, onClick = {})
        }
    }
}

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
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
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            IncludeExcludeChip(
                label = "Drama",
                state = IncludeExcludeState.Neutral,
                onTap = {},
                onLongPress = {},
            )
            IncludeExcludeChip(
                label = "Action",
                state = IncludeExcludeState.Include,
                onTap = {},
                onLongPress = {},
            )
            IncludeExcludeChip(
                label = "Horror",
                state = IncludeExcludeState.Exclude,
                onTap = {},
                onLongPress = {},
            )
        }
    }
}

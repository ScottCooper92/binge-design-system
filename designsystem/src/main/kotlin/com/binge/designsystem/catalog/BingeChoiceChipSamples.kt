package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeChoiceChip
import com.binge.designsystem.preview.ScreenshotTheme

/** A single choice as chips: the picked one is filled with a check, the others are outlined and lead with their icon. A tap moves the pick. */
@Composable
fun BingeChoiceChipSample() {
    var picked by remember { mutableStateOf(MOVIES) }
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.filter_sheet_chip_spacing))) {
            BingeChoiceChip(label = "Movies", selected = picked == MOVIES, onClick = { picked = MOVIES }, icon = Icons.Filled.Movie)
            BingeChoiceChip(label = "TV shows", selected = picked == SHOWS, onClick = { picked = SHOWS }, icon = Icons.Filled.Tv)
            BingeChoiceChip(label = "Either", selected = picked == EITHER, onClick = { picked = EITHER })
        }
    }
}

private const val MOVIES = 0
private const val SHOWS = 1
private const val EITHER = 2

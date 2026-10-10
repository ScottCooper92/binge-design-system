package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvConfirmDialogContent
import com.binge.designsystem.tv.component.TvSideSheetPanel
import com.binge.designsystem.tv.component.TvSideSheetRow
import com.binge.designsystem.tv.component.TvSideSheetTitle
import com.binge.designsystem.tv.R as TvR

/** Rows about one title each: a poster thumbnail and a second line, the first row focused and the second at rest. */
@Composable
fun TvSideSheetRowThumbnailSample() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        TvSideSheetPanel {
            TvSideSheetTitle("Add to a list")
            TvSideSheetRow(
                label = "Dune: Part Two",
                supportingText = "2024 · Film",
                onClick = {},
                leading = { PosterThumbnail() },
                initiallyFocused = true,
            )
            TvSideSheetRow(
                label = "Severance",
                supportingText = "2022 · Series",
                onClick = {},
                leading = { PosterThumbnail() },
            )
            TvSideSheetRow(
                label = "Remove from this list",
                supportingText = "It stays in your other lists.",
                onClick = {},
                destructive = true,
            )
        }
    }
}

/** A stand-in for a poster image: the size the leading slot is meant for, in a placeholder fill. */
@Composable
private fun PosterThumbnail() {
    Box(
        Modifier
            .size(
                width = dimensionResource(TvR.dimen.tv_side_sheet_thumbnail_width),
                height = dimensionResource(TvR.dimen.tv_side_sheet_thumbnail_height),
            ).clip(BingeShapes.TvListItem)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

/** A destructive confirm, as it first appears: focus on Cancel, so a stray OK does nothing. */
@Composable
fun TvConfirmDialogSample() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TvConfirmDialogContent(
            title = "Delete this request?",
            message = "It is removed from the server for everyone.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            destructive = true,
            onConfirm = {},
            onDismissRequest = {},
            initiallyDismissFocused = true,
        )
    }
}

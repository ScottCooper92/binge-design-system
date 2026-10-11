package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvCheckboxRow
import com.binge.designsystem.tv.component.TvConfirmDialogContent
import com.binge.designsystem.tv.component.TvSideSheetPanel
import com.binge.designsystem.tv.component.TvSideSheetRow
import com.binge.designsystem.tv.component.TvSideSheetTitle
import com.binge.designsystem.tv.R as TvR

/**
 * A choice whose rows carry a count, and one that has none: that row reserves the supporting line, so all three stand
 * at one height and the list reads as one set.
 */
@Composable
fun TvSideSheetRowReservedLineSample() {
    var selected by remember { mutableStateOf("Pending") }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        TvSideSheetPanel {
            TvSideSheetTitle("Show requests")
            TvSideSheetRow(
                label = "Pending",
                icon = Icons.Filled.Schedule,
                supportingText = "12 requests",
                onClick = { selected = "Pending" },
                selected = selected == "Pending",
                initiallyFocused = true,
            )
            TvSideSheetRow(
                label = "Approved",
                icon = Icons.Filled.CheckCircle,
                supportingText = "48 requests",
                onClick = { selected = "Approved" },
                selected = selected == "Approved",
            )
            TvSideSheetRow(
                label = "All",
                icon = Icons.AutoMirrored.Filled.List,
                onClick = { selected = "All" },
                selected = selected == "All",
                reserveSupportingLine = true,
            )
        }
    }
}

/** A row whose label is longer than the sheet is wide: it stops on one line with an ellipsis, beside its tick. */
@Composable
fun TvSideSheetRowLongLabelSample() {
    val longLabel = "The extended director's cut of the complete first season, remastered"
    val shortLabel = "Watch later"
    var selected by remember { mutableStateOf(longLabel) }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        TvSideSheetPanel {
            TvSideSheetTitle("Add to a list")
            TvSideSheetRow(
                label = longLabel,
                onClick = { selected = longLabel },
                selected = selected == longLabel,
                initiallyFocused = true,
            )
            TvSideSheetRow(
                label = shortLabel,
                onClick = { selected = shortLabel },
                selected = selected == shortLabel,
            )
        }
    }
}

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
                supportingText = "It stays in your other lists, and keeps its place in each of them.",
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

/** A confirm with an option it applies, between the message and the buttons: focus still arrives on Cancel. */
@Composable
fun TvConfirmDialogExtraContentSample() {
    var alsoBlock by remember { mutableStateOf(true) }
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
            extraContent = {
                TvCheckboxRow(label = "Also block this title", checked = alsoBlock, onCheckedChange = { alsoBlock = it })
            },
        )
    }
}

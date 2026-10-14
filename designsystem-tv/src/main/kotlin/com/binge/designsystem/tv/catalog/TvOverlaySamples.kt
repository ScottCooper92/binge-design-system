package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
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
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvCheckboxRow
import com.binge.designsystem.tv.component.TvConfirmDialogContent
import com.binge.designsystem.tv.component.TvInfoCard
import com.binge.designsystem.tv.component.TvSideSheetPanel
import com.binge.designsystem.tv.component.TvSideSheetReadout
import com.binge.designsystem.tv.component.TvSideSheetRow
import com.binge.designsystem.tv.component.TvSideSheetTitle
import com.binge.designsystem.tv.component.TvSwitch
import com.binge.designsystem.tv.focus.tvFocusContentColor
import com.binge.designsystem.R as DesR
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

/**
 * A page opened from a [com.binge.designsystem.tv.component.TvSideSheetStack]'s list: its own title and its own rows
 * on the same panel, the first focused. The stack's live behaviour, a page opening over another and focus returning to the
 * row that opened it, is [TvSideSheetStackDemo]'s.
 */
@Composable
fun TvSideSheetStackPageSample() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        TvSideSheetPanel {
            TvSideSheetTitle("Notifications")
            TvSideSheetRow(label = "New requests", supportingText = "On", onClick = {}, initiallyFocused = true)
            TvSideSheetRow(label = "Approved requests", supportingText = "On", onClick = {})
            TvSideSheetRow(label = "Issues", supportingText = "Off", onClick = {})
        }
    }
}

/**
 * A sheet that shows facts as well as offering controls: the read-outs stay out of the focus order at full contrast, and the
 * one control below them is focused.
 */
@Composable
fun TvSideSheetReadoutSample() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        TvSideSheetPanel {
            TvSideSheetTitle("Server")
            TvSideSheetReadout(label = "Address", supportingText = "http://media.local:5055", icon = Icons.Filled.Dns)
            TvSideSheetReadout(label = "Version", supportingText = "2.7.3", icon = Icons.Filled.Info)
            TvSideSheetRow(
                label = "Scan the library",
                supportingText = "Last run 2 hours ago",
                icon = Icons.Filled.Refresh,
                onClick = {},
                initiallyFocused = true,
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

/**
 * A row that ends in something of its own, here a chevron, beside a destructive row that is focused: the destructive row
 * fills with the error colour, as a destructive button does, not with the accent.
 */
@Composable
fun TvSideSheetRowTrailingSample() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        TvSideSheetPanel {
            TvSideSheetTitle("Account")
            TvSideSheetRow(
                label = "Notifications",
                supportingText = "Email and push",
                icon = Icons.Filled.Notifications,
                onClick = {},
            ) { isFocused ->
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = tvFocusContentColor(isFocused = isFocused, resting = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
            TvSideSheetRow(
                label = "Sign out",
                icon = Icons.AutoMirrored.Filled.Logout,
                onClick = {},
                destructive = true,
                initiallyFocused = true,
            )
        }
    }
}

/**
 * A sheet whose rows run to the screen's bottom edge: the panel keeps no bottom margin, and the scrolling rows carry the
 * bottom overscan inside their scroll, so the last one can still scroll clear of the edge.
 */
@Composable
fun TvSideSheetToBottomEdgeSample() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        TvSideSheetPanel(toBottomEdge = true) {
            TvSideSheetTitle("Maintenance")
            Column(
                modifier =
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = dimensionResource(TvR.dimen.tv_overscan_vertical)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
            ) {
                BottomEdgeJobs.forEachIndexed { index, job ->
                    TvSideSheetRow(label = job, supportingText = "Last run an hour ago", onClick = {}, initiallyFocused = index == 0)
                }
            }
        }
    }
}

private val BottomEdgeJobs =
    listOf("Scan the library", "Refresh the cache", "Sync watchlists", "Check availability", "Clean up downloads", "Rebuild posters")

/**
 * A sheet's switches, as rows that toggle: the focused one is on and inverts against the row's fill, and the resting rows
 * show on and off.
 */
@Composable
fun TvSwitchSample() {
    SwitchSheet(firstOn = true)
}

/** The same sheet with its focused switch off: an outline and a thumb at the start, on the row's fill. */
@Composable
fun TvSwitchOffSample() {
    SwitchSheet(firstOn = false)
}

@Composable
private fun SwitchSheet(firstOn: Boolean) {
    var notify by remember { mutableStateOf(firstOn) }
    var autoplay by remember { mutableStateOf(true) }
    var subtitles by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        TvSideSheetPanel {
            TvSideSheetTitle("This app")
            TvSideSheetRow(label = "Notify me", onClick = { notify = !notify }, initiallyFocused = true) { isFocused ->
                TvSwitch(checked = notify, isFocused = isFocused)
            }
            TvSideSheetRow(label = "Autoplay trailers", onClick = { autoplay = !autoplay }) { isFocused ->
                TvSwitch(checked = autoplay, isFocused = isFocused)
            }
            TvSideSheetRow(label = "Show subtitles", onClick = { subtitles = !subtitles }) { isFocused ->
                TvSwitch(checked = subtitles, isFocused = isFocused)
            }
        }
    }
}

/** Facts as tiles in a sheet: the focused one shows the ring a focused button does, with no fill; the one below rests. */
@Composable
fun TvInfoCardSample() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        TvSideSheetPanel {
            TvSideSheetTitle("Settings")
            TvInfoCard(
                title = "Media server · 2.7.3",
                lines = listOf("http://media.local:5055", "Connected", "Signed in as Ana"),
                icon = Icons.Filled.Dns,
                initiallyFocused = true,
            )
            TvInfoCard(title = "Services", lines = listOf("Main · Default", "Main · 4K"), icon = Icons.Filled.Info)
        }
    }
}

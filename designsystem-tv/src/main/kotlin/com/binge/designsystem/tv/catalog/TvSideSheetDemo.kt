package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.component.TvSideSheet
import com.binge.designsystem.tv.component.TvSideSheetConfirm
import com.binge.designsystem.tv.component.TvSideSheetRow
import com.binge.designsystem.tv.component.TvSideSheetRows
import com.binge.designsystem.tv.component.TvSideSheetStack
import com.binge.designsystem.tv.component.TvSideSheetStep
import com.binge.designsystem.tv.component.TvSideSheetStepFocus
import com.binge.designsystem.tv.component.TvSideSheetTitle
import com.binge.designsystem.tv.component.rememberTvSideSheetStackState
import com.binge.designsystem.tv.focus.rememberTvOverlayCloser
import com.binge.designsystem.R as DesR

private val DemoQualities = listOf("HD", "4K")

/**
 * The real side sheet over a scrim: the button opens it and focus lands on the first row. Picking a quality moves
 * the tick, Delete swaps to a confirm step whose Cancel brings the rows back, and BACK, or the key pointing away
 * from the panel, closes it and returns focus to the button.
 */
@Composable
fun TvSideSheetDemo() {
    var open by remember { mutableStateOf(false) }
    var quality by remember { mutableStateOf(DemoQualities.first()) }
    var confirming by remember { mutableStateOf(false) }
    val opener = remember { FocusRequester() }
    val closer =
        rememberTvOverlayCloser(restoreTo = opener) {
            open = false
            confirming = false
        }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TvButton(label = "Open sheet", onClick = { open = true }, modifier = Modifier.focusRequester(opener))
    }
    if (open) {
        TvSideSheet(onDismissRequest = closer::close, closer = closer) { entryFocus ->
            if (confirming) {
                TvSideSheetConfirm(
                    title = "Delete this request?",
                    message = "It is removed from the server for everyone.",
                    confirmLabel = "Delete",
                    onConfirm = closer::close,
                    onCancel = { confirming = false },
                    entryFocus = entryFocus,
                )
            } else {
                TvSideSheetTitle("Dune: Part Two")
                TvSideSheetRows {
                    DemoQualities.forEachIndexed { index, label ->
                        TvSideSheetRow(
                            label = label,
                            onClick = { quality = label },
                            selected = quality == label,
                            modifier = if (index == 0) Modifier.focusRequester(entryFocus) else Modifier,
                        )
                    }
                    TvSideSheetRow(
                        label = "Delete request",
                        onClick = { confirming = true },
                        icon = Icons.Filled.Delete,
                        destructive = true,
                    )
                }
                TvSideSheetStepFocus(entryFocus)
            }
        }
    }
}

/**
 * The real stacked sheet: Settings opens a list whose rows each open their own page. BACK, or the key pointing away from
 * the panel, goes back one page with focus on the row that opened the one just left, and from the list closes the sheet and returns
 * focus to the button. Each page under the list has a "Close sheet" row, so the sheet can be closed from any depth. "Reopen
 * where I was" opens it on the page it was closed on, with focus on the row that last had it (the Close row, when that is how
 * it was closed), from the path the stack's state kept.
 */
@Composable
fun TvSideSheetStackDemo() {
    var open by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf<List<TvSideSheetStep>>(emptyList()) }
    val opener = remember { FocusRequester() }
    val closer = rememberTvOverlayCloser(restoreTo = opener) { open = false }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m))) {
            TvButton(
                label = "Open settings",
                onClick = {
                    saved = emptyList()
                    open = true
                },
                modifier = Modifier.focusRequester(opener),
            )
            TvButton(label = "Reopen where I was", onClick = { open = true })
        }
    }
    if (open) {
        val state = rememberTvSideSheetStackState("settings", startPath = saved)
        DisposableEffect(state) { onDispose { saved = state.path } }
        TvSideSheetStack(state = state, onDismissRequest = closer::close, closer = closer) { nav ->
            when (nav.page) {
                "settings" -> {
                    TvSideSheetTitle("Settings")
                    TvSideSheetRows {
                        TvSideSheetRow(
                            "Notifications",
                            onClick = { nav.push("notifications", openedBy = "notifications") },
                            modifier = nav.row("notifications"),
                            opensPage = true,
                        )
                        TvSideSheetRow(
                            "Quality",
                            onClick = { nav.push("quality", openedBy = "quality") },
                            modifier = nav.row("quality"),
                            opensPage = true,
                        )
                    }
                }
                "notifications" -> {
                    TvSideSheetTitle("Notifications")
                    TvSideSheetRows {
                        TvSideSheetRow("New requests", onClick = {}, supportingText = "On", modifier = nav.row("new"))
                        TvSideSheetRow("Issues", onClick = {}, supportingText = "Off", modifier = nav.row("issues"))
                        TvSideSheetRow("Close sheet", onClick = closer::close, icon = Icons.Filled.Close, modifier = nav.row("close"))
                    }
                }
                else -> {
                    TvSideSheetTitle("Quality")
                    TvSideSheetRows {
                        DemoQualities.forEach { label -> TvSideSheetRow(label, onClick = {}, modifier = nav.row(label)) }
                        TvSideSheetRow("Close sheet", onClick = closer::close, icon = Icons.Filled.Close, modifier = nav.row("close"))
                    }
                }
            }
        }
    }
}

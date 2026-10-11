package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.component.TvSideSheet
import com.binge.designsystem.tv.component.TvSideSheetConfirm
import com.binge.designsystem.tv.component.TvSideSheetRow
import com.binge.designsystem.tv.component.TvSideSheetStepFocus
import com.binge.designsystem.tv.component.TvSideSheetTitle
import com.binge.designsystem.tv.focus.rememberTvOverlayCloser

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
        TvSideSheet(onDismissRequest = closer::close) { entryFocus ->
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
                TvSideSheetStepFocus(entryFocus)
            }
        }
    }
}

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.component.TvSideSheet
import com.binge.designsystem.tv.component.TvSideSheetRow
import com.binge.designsystem.tv.component.TvSideSheetRows
import com.binge.designsystem.tv.component.TvSideSheetTitle
import com.binge.designsystem.tv.focus.rememberTvOverlayCloser
import com.binge.designsystem.tv.focus.tryRequestFocus

private val BUTTON_GAP = 24.dp

/**
 * A sheet closed with no restore target: the closer releases the trap and reports the disposal, and places no focus. This
 * screen's own hand-off does that, so BACK or the row closes the sheet and focus lands on the second button, not the one that
 * opened it.
 */
@Composable
fun TvOverlayReleaseDemo() {
    var open by remember { mutableStateOf(false) }
    var wasOpen by remember { mutableStateOf(false) }
    val handOff = remember { FocusRequester() }
    val closer = rememberTvOverlayCloser(restoreTo = null) { open = false }
    LaunchedEffect(open) {
        if (wasOpen && !open) handOff.tryRequestFocus()
        wasOpen = open
    }
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(BUTTON_GAP, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TvButton(label = "Open sheet", onClick = { open = true })
        TvButton(label = "Focus lands here", onClick = {}, modifier = Modifier.focusRequester(handOff))
    }
    if (open) {
        TvSideSheet(onDismissRequest = closer::close, closer = closer) { entryFocus ->
            TvSideSheetTitle("Close without restoring")
            TvSideSheetRows {
                TvSideSheetRow(label = "Close", onClick = closer::close, modifier = Modifier.focusRequester(entryFocus))
            }
        }
    }
}

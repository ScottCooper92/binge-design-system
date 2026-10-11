package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.binge.designsystem.tv.component.TvConfirmDialog
import com.binge.designsystem.tv.focus.rememberTvOverlayCloser

/**
 * The real confirm dialog in its own window: the button opens it and focus lands on Cancel. Confirm, Cancel and
 * BACK all close it and return focus to the button.
 */
@Composable
fun TvConfirmDialogDemo() {
    var open by remember { mutableStateOf(false) }
    val opener = remember { FocusRequester() }
    val closer = rememberTvOverlayCloser(restoreTo = opener) { open = false }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TvButton(label = "Open dialog", onClick = { open = true }, modifier = Modifier.focusRequester(opener))
    }
    if (open) {
        TvConfirmDialog(
            title = "Delete this request?",
            message = "It is removed from the server for everyone.",
            confirmLabel = "Delete",
            onConfirm = closer::close,
            onDismissRequest = closer::close,
            destructive = true,
        )
    }
}

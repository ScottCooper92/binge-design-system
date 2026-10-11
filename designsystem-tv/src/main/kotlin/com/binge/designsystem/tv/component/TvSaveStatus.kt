package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.SaveStatus
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * The inline status line of a TV page that saves each change as it is made: "Saving…", then "Saved", or a
 * failure with a Retry button. Such a page has no Save button.
 *
 * [SaveStatus.Idle] draws nothing and takes no space, so place the line where its arrival moves nothing that
 * matters, such as under a top-aligned copy column. The other three states share one height, the button's, so
 * the line does not jump between them. A failure keeps the change on screen and puts Retry, calling the failure's
 * `onRetry`, in the line itself. This is the TV form of the phone's permanent Retry snackbar, since the TV module has no
 * snackbar. Retry is reachable on the D-pad but does not take focus, so a failure never pulls the user out of
 * the field they are on. Pressing Retry steps focus back towards the content before retrying, so the button
 * leaving the line as the status moves on does not send focus to the top of the page. The line is a polite live
 * region, so each change of state is announced.
 */
@Composable
fun TvSaveStatus(status: SaveStatus, modifier: Modifier = Modifier) {
    if (status == SaveStatus.Idle) return
    Row(
        modifier = modifier
            .heightIn(min = dimensionResource(TvR.dimen.tv_button_height))
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_sm)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (status) {
            SaveStatus.Idle -> Unit
            SaveStatus.Saving -> StatusText(stringResource(DesR.string.save_status_saving))
            SaveStatus.Saved -> {
                StatusGlyph(Icons.Filled.Check, MaterialTheme.colorScheme.onSurfaceVariant)
                StatusText(stringResource(DesR.string.save_status_saved))
            }
            is SaveStatus.Failed -> {
                StatusGlyph(Icons.Filled.ErrorOutline, MaterialTheme.colorScheme.error)
                StatusText(stringResource(DesR.string.save_status_failed), MaterialTheme.colorScheme.error)
                val focusManager = LocalFocusManager.current
                TvButton(
                    label = stringResource(DesR.string.save_status_retry),
                    onClick = {
                        // Retry leaves the line once the status moves on, and a focused node that leaves sends
                        // focus to the page's first item. Step off it first, back towards the content.
                        if (!focusManager.moveFocus(FocusDirection.Up)) focusManager.moveFocus(FocusDirection.Previous)
                        status.onRetry()
                    },
                )
            }
        }
    }
}

@Composable
private fun StatusGlyph(icon: ImageVector, tint: Color) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(dimensionResource(TvR.dimen.tv_save_status_icon)),
    )
}

@Composable
private fun StatusText(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, color = color)
}

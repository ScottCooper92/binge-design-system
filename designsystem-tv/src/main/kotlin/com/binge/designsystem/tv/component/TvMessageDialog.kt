package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.focus.TvOverlayArrivalFocusEffect
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.R as DesR

/**
 * A centred message dialog for the 10-foot screen, the TV's [BingeMessageDialog][com.binge.designsystem.component.BingeMessageDialog]: a [title]
 * saying what happened, a [message] with the cause and the next step, and one text button that acknowledges it. Focus arrives
 * on the button, and OK, Back and a press outside all call [onDismiss]. It is an acknowledgement, so it has one action; a
 * choice is a [TvConfirmDialog].
 */
@Composable
fun TvMessageDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = stringResource(DesR.string.action_ok),
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        TvMessageDialogContent(title = title, message = message, confirmLabel = confirmLabel, onDismiss = onDismiss, modifier = modifier)
    }
}

/**
 * The dialog's visible card, apart from the modal window, so a screenshot can render it. [initiallyFocused] draws the button
 * focused, the frame the user sees first: a static preview runs no arrival effect. Rendered inline, it pulls focus to the
 * button when it mounts.
 */
@Composable
fun TvMessageDialogContent(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyFocused: Boolean = false,
) {
    val focus = remember { FocusRequester() }
    TvOverlayArrivalFocusEffect(focus)
    TvDialogCard(modifier) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(text = message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(DesR.dimen.padding_s)),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvButton(
                label = confirmLabel,
                onClick = onDismiss,
                style = TvButtonStyle.Text,
                initiallyFocused = initiallyFocused,
                modifier = Modifier.focusRequester(focus),
            )
        }
    }
}

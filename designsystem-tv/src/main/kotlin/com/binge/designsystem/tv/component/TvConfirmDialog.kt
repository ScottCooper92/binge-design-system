package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.TvOverlayArrivalFocusEffect
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * A centred confirm dialog for the 10-foot screen: a title, a message, and a dismiss and confirm pair. Focus arrives
 * on dismiss, so a stray OK does nothing. [destructive] draws the confirm button in the error colour.
 *
 * For an action already inside a [TvSideSheet], its own confirm step reads better: see `TvSideSheetConfirm`.
 */
@Composable
fun TvConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = stringResource(DesR.string.action_cancel),
    destructive: Boolean = false,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        TvConfirmDialogContent(
            title = title,
            message = message,
            confirmLabel = confirmLabel,
            dismissLabel = dismissLabel,
            destructive = destructive,
            onConfirm = onConfirm,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
        )
    }
}

/**
 * The dialog's visible card, apart from the modal window, so a screenshot can render it. [initiallyDismissFocused]
 * draws the dismiss button focused, the frame the user sees first: a static preview runs no arrival effect.
 *
 * The content runs the arrival effect itself, so rendered inline, outside a dialog window, it pulls focus to dismiss
 * when it mounts.
 */
@Composable
fun TvConfirmDialogContent(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyDismissFocused: Boolean = false,
) {
    val dismissFocus = remember { FocusRequester() }
    TvOverlayArrivalFocusEffect(dismissFocus)
    Column(
        modifier =
            modifier
                .width(dimensionResource(TvR.dimen.tv_confirm_dialog_width))
                .clip(BingeShapes.AccountCard)
                .background(MaterialTheme.colorScheme.surface)
                .border(dimensionResource(TvR.dimen.tv_button_border_width), MaterialTheme.colorScheme.border, BingeShapes.AccountCard)
                .padding(dimensionResource(DesR.dimen.padding_l)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(text = message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.padding(top = dimensionResource(DesR.dimen.padding_s)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m), Alignment.End),
        ) {
            TvButton(
                label = dismissLabel,
                onClick = onDismissRequest,
                style = TvButtonStyle.Secondary,
                initiallyFocused = initiallyDismissFocused,
                modifier = Modifier.focusRequester(dismissFocus),
            )
            TvButton(
                label = confirmLabel,
                onClick = onConfirm,
                style = if (destructive) TvButtonStyle.Destructive else TvButtonStyle.Primary,
            )
        }
    }
}

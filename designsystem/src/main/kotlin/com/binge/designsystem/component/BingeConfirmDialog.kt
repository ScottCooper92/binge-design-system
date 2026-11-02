package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R

/**
 * Canonical Binge confirm dialog: a title, a body message, and Cancel and confirm as plain text buttons, the
 * platform's own dialog shape. Stateless — the caller owns the visibility flag and reacts to
 * [onConfirm]/[onDismissRequest].
 *
 * **At most two actions, always on one row**: Cancel, then the confirm. A long label or a large font wraps inside its button,
 * never onto a second row. Set [destructive] for irreversible actions: the confirm text takes the error colour. When the
 * dialog could do a milder or a more severe thing, the choice is a [SwitchRow] in [extraContent], off by default: the caller owns
 * its state and passes [confirmLabel] and [destructive] from it, and the body says what the switch adds. The visible body
 * is delegated to [BingeConfirmDialogContent] so screenshot tests can render it (the [AlertDialog] window does not capture).
 */
@Composable
fun BingeConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = stringResource(R.string.action_cancel),
    destructive: Boolean = false,
    extraContent: @Composable (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(title) },
        text = {
            if (extraContent == null) {
                Text(message)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
                    Text(message)
                    extraContent()
                }
            }
        },
        confirmButton = {
            ConfirmActions(
                confirmLabel = confirmLabel,
                dismissLabel = dismissLabel,
                destructive = destructive,
                onConfirm = onConfirm,
                onDismissRequest = onDismissRequest,
            )
        },
    )
}

/**
 * Stateless body of [BingeConfirmDialog], split out so screenshot tests can render the dialog's
 * visible content directly — the modal [AlertDialog] window itself does not capture in previews.
 */
@Composable
fun BingeConfirmDialogContent(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = stringResource(R.string.action_cancel),
    destructive: Boolean = false,
    extraContent: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_m)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        extraContent?.invoke()
        ConfirmActions(
            confirmLabel = confirmLabel,
            dismissLabel = dismissLabel,
            destructive = destructive,
            onConfirm = onConfirm,
            onDismissRequest = onDismissRequest,
        )
    }
}

@Composable
private fun ConfirmActions(
    confirmLabel: String,
    dismissLabel: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    TwoActionRow(
        gap = dimensionResource(R.dimen.padding_s),
        modifier = Modifier.fillMaxWidth(),
        first = { BingeTextButton(label = dismissLabel, onClick = onDismissRequest) },
        second = { BingeTextButton(label = confirmLabel, onClick = onConfirm, destructive = destructive) },
    )
}

/**
 * Two buttons on one row, end-aligned, whatever their labels. Each takes the width its label wants while both fit; past that
 * the shorter takes only what it needs, up to half the row, and the longer gets the rest, so a long label wraps inside its
 * button and a short one never does while the other has room to give.
 */
@Composable
private fun TwoActionRow(
    gap: Dp,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(content = {
        first()
        second()
    }, modifier = modifier) { measurables, constraints ->
        val gapPx = gap.roundToPx()
        val room = (constraints.maxWidth - gapPx).coerceAtLeast(0)
        val wantFirst = measurables[0].maxIntrinsicWidth(Constraints.Infinity)
        val wantSecond = measurables[1].maxIntrinsicWidth(Constraints.Infinity)
        val half = room / 2
        val firstWidth =
            when {
                wantFirst + wantSecond <= room -> wantFirst
                wantFirst <= half -> wantFirst
                wantSecond <= half -> room - wantSecond
                else -> half
            }
        val secondWidth = if (wantFirst + wantSecond <= room) wantSecond else room - firstWidth
        val firstPlaceable = measurables[0].measure(Constraints(maxWidth = firstWidth))
        val secondPlaceable = measurables[1].measure(Constraints(maxWidth = secondWidth))
        val height = maxOf(firstPlaceable.height, secondPlaceable.height)
        layout(constraints.maxWidth, height) {
            val startX = constraints.maxWidth - firstPlaceable.width - gapPx - secondPlaceable.width
            firstPlaceable.placeRelative(startX, (height - firstPlaceable.height) / 2)
            secondPlaceable.placeRelative(startX + firstPlaceable.width + gapPx, (height - secondPlaceable.height) / 2)
        }
    }
}

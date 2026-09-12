package com.binge.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R

private const val SNACKBAR_MAX_LINES = 2

/**
 * Binge's snackbar visual: a floating rounded-pill bar with an accent action. It follows the theme —
 * a light pill in light mode, a dark pill in dark mode — sitting a step above the background on the
 * raised surface-container tone. [actionLabel] (when non-null) renders an accent text button;
 * otherwise [showDismissAction] renders a trailing close icon. One style for every snackbar — kind
 * only drives duration upstream.
 */
@Composable
internal fun BingeSnackbar(
    message: String,
    actionLabel: String?,
    onActionClick: () -> Unit,
    showDismissAction: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasTrailing = actionLabel != null || showDismissAction
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = dimensionResource(R.dimen.snackbar_elevation),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.padding_m),
                top = dimensionResource(R.dimen.snackbar_padding_vertical),
                bottom = dimensionResource(R.dimen.snackbar_padding_vertical),
            ),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = SNACKBAR_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
                // A trailing button supplies the end gap; with none, the message needs its own so it
                // doesn't run into the rounded corner.
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (hasTrailing) {
                            Modifier
                        } else {
                            Modifier.padding(end = dimensionResource(R.dimen.padding_m))
                        },
                    ),
            )
            when {
                actionLabel != null ->
                    TextButton(onClick = onActionClick) {
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                showDismissAction ->
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.binge_snackbar_dismiss),
                        )
                    }
            }
        }
    }
}

/** Drop-in replacement for M3 `SnackbarHost` that renders [BingeSnackbar]. */
@Composable
fun BingeSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier) { data ->
        BingeSnackbar(
            message = data.visuals.message,
            actionLabel = data.visuals.actionLabel,
            onActionClick = { data.performAction() },
            showDismissAction = data.visuals.withDismissAction,
            onDismiss = { data.dismiss() },
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.padding_m),
                end = dimensionResource(R.dimen.padding_m),
                bottom = dimensionResource(R.dimen.padding_s),
            ),
        )
    }
}

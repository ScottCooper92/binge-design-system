package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

private const val TEXT_ENTRY_MIN_LINES = 3

/**
 * Caps the field height so a long entry scrolls internally instead of pushing the submit row
 * off-screen — the surface backs a content-height sheet with no bounded height to weight against.
 * Dropped when [expanded] gives the column a bounded height.
 */
private const val TEXT_ENTRY_MAX_LINES = 6

/**
 * A reusable, host-agnostic text-entry surface: a title, an optional [header] slot (e.g. a type
 * picker), a multi-line field with inline error / character-count support, and a cancel + submit
 * button row. Stateless — the caller owns the value, validation and submit behaviour — so it drops
 * equally into a [BingeBottomSheet] or a full-screen route. The host is responsible for window
 * insets (ime / navigation bars).
 *
 * Pass [onExpandToggle] (and drive [expanded]) to surface an expand/collapse affordance in the
 * title row. When [expanded] the column fills the available height and the field grows to fill it,
 * scrolling internally — so a long entry isn't edited through the 6-line window. The host is meant
 * to pair this with a locked [BingeBottomSheet] (`gesturesEnabled = false`) so field-scroll never
 * fights the sheet's drag; a host that never offers expansion leaves [onExpandToggle] null.
 *
 * @param submitEnabled whether submit is allowed; callers gate on "non-blank" (default) and, for
 *   edits, "changed". @param maxLength when set, caps input and shows an `n / max` counter.
 */
@Composable
fun TextEntrySurface(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    submitLabel: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    submitEnabled: Boolean = value.isNotBlank(),
    isSubmitting: Boolean = false,
    error: String? = null,
    maxLength: Int? = null,
    minLines: Int = TEXT_ENTRY_MIN_LINES,
    maxLines: Int = TEXT_ENTRY_MAX_LINES,
    expanded: Boolean = false,
    onExpandToggle: (() -> Unit)? = null,
    cancelLabel: String = stringResource(R.string.text_entry_cancel),
    header: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (expanded) Modifier.fillMaxHeight() else Modifier)
            .padding(dimensionResource(R.dimen.padding_m)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) {
        TextEntryTitleRow(title = title, expanded = expanded, onExpandToggle = onExpandToggle)
        header()
        OutlinedTextField(
            value = value,
            onValueChange = { updated -> if (maxLength == null || updated.length <= maxLength) onValueChange(updated) },
            modifier = Modifier
                .fillMaxWidth()
                // Expanded has a bounded column height to weight against, so the field fills the
                // space and scrolls internally; collapsed it wraps to its capped line count.
                .then(if (expanded) Modifier.weight(1f) else Modifier),
            enabled = !isSubmitting,
            isError = error != null,
            placeholder = hint?.let { { Text(it) } },
            shape = BingeShapes.Medium,
            minLines = minLines,
            maxLines = if (expanded) Int.MAX_VALUE else maxLines,
            supportingText = textEntrySupport(error, value.length, maxLength),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            BingeOutlinedButton(
                label = cancelLabel,
                onClick = onCancel,
                enabled = !isSubmitting,
                modifier = Modifier.weight(1f),
            )
            BingeFilledButton(
                label = submitLabel,
                onClick = onSubmit,
                enabled = submitEnabled && !isSubmitting,
                loading = isSubmitting,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Title plus, when the host opts in, the expand/collapse affordance that drives full-screen mode. */
@Composable
private fun TextEntryTitleRow(
    title: String,
    expanded: Boolean,
    onExpandToggle: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (onExpandToggle != null) {
            IconButton(onClick = onExpandToggle) {
                Icon(
                    imageVector = if (expanded) Icons.Outlined.CloseFullscreen else Icons.Outlined.OpenInFull,
                    contentDescription = stringResource(
                        if (expanded) R.string.text_entry_collapse else R.string.text_entry_expand,
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** The field's support slot: the error message takes precedence over the optional character counter. */
private fun textEntrySupport(
    error: String?,
    length: Int,
    maxLength: Int?,
): (@Composable () -> Unit)? =
    when {
        error != null -> {
            { Text(text = error, color = MaterialTheme.colorScheme.error) }
        }
        maxLength != null -> {
            {
                Text(
                    text = stringResource(R.string.text_entry_character_count, length, maxLength),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        else -> null
    }

package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.R

/**
 * The page of a sheet that collects several fields and then acts on them: a [title] with a [body] under it, the caller's
 * [fields], a [error] when the last submit failed, and Cancel beside the [submitLabel] button. The fields scroll when the
 * form is taller than the sheet, and the two buttons stay below them. In a side sheet the buttons are pinned through
 * [BingeSheetFooter] like any page's primary.
 *
 * It is for a form whose submit is the point: create a thing, or a change the server has to accept. A failed submit keeps
 * the form and its values and says why in [error]. A plain settings value saves as it is made and does not use this; it
 * is a text or choice row with its own sheet. A single field is [TextSettingContent] or `TextEntrySurface`.
 *
 * [submitEnabled] gates the button on the caller's own validation. [submitting] shows the button's busy state and locks
 * Cancel.
 *
 * @need collect several fields in a sheet and submit them together
 * @group sheets
 * @replaces a hand-built sheet page of fields with Cancel and submit buttons
 */
@Composable
fun BingeFieldsSheetContent(
    title: String,
    body: String,
    submitLabel: String,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    submitting: Boolean = false,
    error: String? = null,
    submitEnabled: Boolean = true,
    cancelLabel: String = stringResource(R.string.text_entry_cancel),
    fields: @Composable ColumnScope.() -> Unit,
) {
    val gap = dimensionResource(R.dimen.padding_m)
    Column(modifier = modifier.fillMaxWidth().imePadding()) {
        Column(
            modifier = Modifier.weight(1f, fill = false).bingeSheetScroll().padding(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            fields()
            error?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
        }
        BingeSheetFooter {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = gap).padding(bottom = gap),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            ) {
                BingeOutlinedButton(label = cancelLabel, onClick = onCancel, enabled = !submitting, modifier = Modifier.weight(1f))
                BingeFilledButton(
                    label = submitLabel,
                    onClick = onSubmit,
                    enabled = submitEnabled,
                    loading = submitting,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

/**
 * The label column [SwitchRow] and [CheckboxRow] share: the [label], then a [supportingText] of at most [supportingMaxLines]
 * lines, ellipsized, when there is one. It takes the width the row has left, and the label greys with [enabled].
 */
@Composable
internal fun RowScope.ToggleRowLabel(
    label: String,
    supportingText: String?,
    enabled: Boolean,
    supportingMaxLines: Int,
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!supportingText.isNullOrBlank()) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = supportingMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

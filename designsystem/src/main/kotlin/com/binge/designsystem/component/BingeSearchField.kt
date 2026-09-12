package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * Pill-shaped search field with leading magnifier, optional placeholder, and a
 * trailing clear-icon button that appears only when the query is non-empty.
 * Used by the search hub and the account watch-providers grid.
 *
 * Pass [focusRequester] to focus the field from outside — a caller cannot add its own
 * `Modifier.focusRequester` for this, because two of them on one node do not both bind and the
 * caller's is the one dropped. Omit it and the field keeps a private one for its own tap handling.
 */
@Composable
fun BingeSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSubmit: () -> Unit = {},
    imeAction: ImeAction = ImeAction.Search,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    focusRequester: FocusRequester? = null,
) {
    val internalFocusRequester = remember { FocusRequester() }
    val fieldFocus = focusRequester ?: internalFocusRequester
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = dimensionResource(R.dimen.search_bar_height))
            .clip(MaterialTheme.shapes.extraLarge)
            .background(containerColor)
            // Tapping anywhere on the pill — icon, padding, the empty area to the right —
            // focuses the field and opens the keyboard, not just the text itself.
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = null,
            ) { fieldFocus.requestFocus() }
            .padding(
                start = dimensionResource(R.dimen.padding_m),
                end = dimensionResource(R.dimen.search_bar_padding_end),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(dimensionResource(R.dimen.search_bar_icon_size)),
        )
        Spacer(Modifier.width(dimensionResource(R.dimen.search_bar_icon_spacing)))
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(fieldFocus),
                textStyle = LocalTextStyle.current.merge(
                    TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    ),
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }, onDone = { onSubmit() }),
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
            )
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (query.isNotEmpty()) {
            TrailingIconButton(
                icon = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_clear_query),
                onClick = onClear,
            )
        }
    }
}

@Composable
private fun TrailingIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    // The Box matches search_bar_trailing_size (48dp = Material's minimum tap
    // target). The icon inside is search_bar_icon_size (22dp) and is centred,
    // so the visible glyph is small but the touchable region clears 48dp.
    Box(
        modifier = Modifier
            .size(dimensionResource(R.dimen.search_bar_trailing_size))
            .clip(CircleShape)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(dimensionResource(R.dimen.search_bar_icon_size)),
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun PreviewBingeSearchFieldEmpty() {
    BingeExpressiveTheme(dynamicColor = false) {
        BingeSearchField(
            query = "",
            onQueryChange = {},
            onClear = {},
            placeholder = "Search services",
        )
    }
}

@Preview(showBackground = true, name = "With query")
@Composable
private fun PreviewBingeSearchFieldWithQuery() {
    BingeExpressiveTheme(dynamicColor = false) {
        BingeSearchField(
            query = "Netflix",
            onQueryChange = {},
            onClear = {},
            placeholder = "Search services",
        )
    }
}

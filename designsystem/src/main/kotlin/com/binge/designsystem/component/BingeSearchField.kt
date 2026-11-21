package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
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
 *
 * @need type a search query with a clear button
 * @group inputs
 * @sample SearchFieldEmptySample
 * @replaces a hand-built pill TextField with a magnifier and clear icon
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
    // The String overload of BasicTextField starts its selection at 0, so a field that opens with a query (a screen restored on Back)
    // would put the caret before it. Own the selection: it starts at the end, follows the user once they move it, and goes back to the
    // end when the query is changed from outside (a clear, a restore). A query the caller rewrites from what was typed keeps the caret
    // where it was.
    val caret = rememberFieldCaret(query, TextFieldValue(query, TextRange(query.length)))
    val shown = caret.shown(query)
    SearchFieldPill(
        modifier = modifier,
        containerColor = containerColor,
        // Tapping anywhere on the pill — icon, padding, the empty area to the right —
        // focuses the field and opens the keyboard, not just the text itself.
        onClick = { fieldFocus.requestFocus() },
        trailing = {
            if (query.isNotEmpty()) {
                TrailingIconButton(
                    icon = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.cd_clear_query),
                    onClick = onClear,
                )
            }
        },
    ) {
        BasicTextField(
            value = shown,
            onValueChange = { if (caret.edit(it, query)) onQueryChange(it.text) },
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
        if (query.isEmpty()) SearchFieldPlaceholder(placeholder)
    }
}

/**
 * The read-only form of [BingeSearchField]: the same pill, drawn by the same layout, as one tap target that opens search. It shows
 * [placeholder] where the field shows its query, takes no text input and raises no keyboard. Use it for an entry that leads to a
 * separate search page, so the entry has the field's height, shape, icon, inset and text style and cannot drift from it.
 *
 * It reads as a button to accessibility services, labelled with [placeholder]. Place it in a [BingeSearchFieldHeader] to give it the insets
 * the Search screen gives its field.
 *
 * @need a tap-to-search entry that looks exactly like the search field
 * @group inputs
 * @sample SearchFieldButtonSample
 * @replaces a hand-built copy of the search field's pill
 */
@Composable
fun BingeSearchFieldButton(
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
) {
    SearchFieldPill(
        modifier = modifier,
        containerColor = containerColor,
        onClick = onClick,
        role = Role.Button,
    ) {
        SearchFieldPlaceholder(placeholder)
    }
}

/**
 * The room the Search screen gives its field: the screen's content inset on each side, a gap below, and the search content's maximum
 * width, centred. Put [BingeSearchField] or [BingeSearchFieldButton] inside it for a screen that hosts the field the way the Search
 * screen does.
 *
 * @need place a search field or its tap-to-search entry the way the Search screen places its field
 * @group inputs
 * @sample SearchFieldButtonSample
 * @replaces hand-built insets around a search field
 */
@Composable
fun BingeSearchFieldHeader(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(
            Modifier
                .widthIn(max = dimensionResource(R.dimen.search_content_max_width))
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.screen_content_inset))
                .padding(bottom = dimensionResource(R.dimen.padding_m)),
        ) {
            content()
        }
    }
}

/** The pill both forms share: height, shape, ground, insets, the magnifier and its gap, and one tap target over all of it. */
@Composable
private fun SearchFieldPill(
    modifier: Modifier,
    containerColor: Color,
    onClick: () -> Unit,
    role: Role? = null,
    trailing: @Composable () -> Unit = {},
    middle: @Composable BoxScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = dimensionResource(R.dimen.search_bar_height))
            .clip(MaterialTheme.shapes.extraLarge)
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = null,
                role = role,
                onClick = onClick,
            ).padding(
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
        Box(modifier = Modifier.weight(1f), content = middle)
        trailing()
    }
}

@Composable
private fun SearchFieldPlaceholder(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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

@Preview(showBackground = true, name = "empty")
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

@Preview(showBackground = true, name = "with-query")
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

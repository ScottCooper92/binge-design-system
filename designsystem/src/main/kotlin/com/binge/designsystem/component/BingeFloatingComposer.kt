package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R

/** The most lines the field grows to before it scrolls inside the card. */
const val FLOATING_COMPOSER_MAX_LINES = 5

/**
 * A chat-style entry card that floats over a conversation: a rounded surface with a text field that grows
 * with what is typed, a row under it for [leadingActions], and a round Send at the end of that row.
 *
 * The field grows to [maxLines] lines and then scrolls inside the card. Send is enabled only while [value]
 * has text and [enabled] is set, and shows a spinner in place of its glyph while [sending] is set. The
 * caller owns the text: clear [value] when the send starts and the field keeps focus, because focus never
 * moves to the button; on a failure, put the text back and the card is as it was. [readOnly] holds the
 * field without dropping the keyboard, for a caller whose send or other action in the card is in flight.
 *
 * The card clears the system bars and the keyboard itself, by [windowInsets], with the window's own side and
 * bottom insets by default. A caller must not pad for the keyboard as well. The height it takes from the
 * bottom of its parent, insets included, comes back through [onOccupiedHeightChange] (padding the caller
 * adds to [modifier] outside the card is not part of it) so the content
 * scrolling under the card can end above it: add that to the content's bottom padding.
 *
 * Placeholder and send description are parameters, because the consumer owns the words.
 *
 * @need let someone type a message or comment inline, in a card floating over a list
 * @group inputs
 * @sample BingeFloatingComposerSample
 * @replaces a hand-built pinned bar with a text field and a send button
 */
@Composable
fun BingeFloatingComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    placeholder: String,
    sendDescription: String,
    modifier: Modifier = Modifier,
    leadingActions: (@Composable RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    sending: Boolean = false,
    maxLines: Int = FLOATING_COMPOSER_MAX_LINES,
    windowInsets: WindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    onOccupiedHeightChange: (Dp) -> Unit = {},
) {
    val density = LocalDensity.current
    val caret = rememberFieldCaret(value, TextFieldValue(value, TextRange(value.length)))
    val shown = caret.shown(value)
    val textStyle = LocalTextStyle.current.merge(
        TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { onOccupiedHeightChange(with(density) { it.height.toDp() }) }
            .windowInsetsPadding(windowInsets)
            .padding(horizontal = dimensionResource(R.dimen.padding_m), vertical = dimensionResource(R.dimen.padding_s)),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = dimensionResource(R.dimen.snackbar_elevation),
        ) {
            Column(
                modifier = Modifier.padding(
                    start = dimensionResource(R.dimen.padding_m),
                    end = dimensionResource(R.dimen.padding_s),
                    top = dimensionResource(R.dimen.padding_s),
                    bottom = dimensionResource(R.dimen.padding_s),
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = dimensionResource(R.dimen.min_touch_target))
                        .padding(end = dimensionResource(R.dimen.padding_s)),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicTextField(
                        value = shown,
                        onValueChange = { if (caret.edit(it, value)) onValueChange(it.text) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        readOnly = readOnly,
                        maxLines = maxLines,
                        textStyle = textStyle,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions.Default,
                    )
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                    ) { leadingActions?.invoke(this) }
                    ExpressiveIconButton(
                        onClick = onSend,
                        icon = Icons.AutoMirrored.Filled.Send,
                        contentDescription = sendDescription,
                        enabled = sending || (enabled && value.isNotBlank()),
                        tone = IconButtonTone.Accent,
                        loading = sending,
                    )
                }
            }
        }
    }
}

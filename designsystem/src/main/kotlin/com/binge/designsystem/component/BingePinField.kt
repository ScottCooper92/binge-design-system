package com.binge.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

/**
 * A short numeric code typed one digit to a box, such as the PIN a television shows beside its pairing code.
 *
 * It is one text field drawn as [length] boxes, so it behaves like a single field:
 * - a digit fills the next box;
 * - backspace clears the last filled box, which moves back a box;
 * - a pasted code fills them all.
 *
 * The cursor stays at the end, so a digit never lands in the middle. Anything that isn't a digit is dropped, and so is
 * anything past [length]. [onComplete] fires once the last box fills; a caller that wants a fresh try after a wrong
 * code clears [value]. [label] is what assistive technology reads for the field. [error] marks every box and is
 * announced with the field.
 */
@Composable
fun BingePinField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    length: Int = DEFAULT_PIN_LENGTH,
    enabled: Boolean = true,
    error: String? = null,
    autoFocus: Boolean = true,
    onComplete: (String) -> Unit = {},
) {
    val focus = remember { FocusRequester() }
    val interactions = remember { MutableInteractionSource() }
    val focused by interactions.collectIsFocusedAsState()
    if (autoFocus) LaunchedEffect(focus) { focus.requestFocus() }
    BasicTextField(
        value = TextFieldValue(value, selection = TextRange(value.length)),
        onValueChange = { typed ->
            val digits = typed.text.filter(Char::isDigit).take(length)
            if (digits == value) return@BasicTextField
            onValueChange(digits)
            if (digits.length == length) onComplete(digits)
        },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        interactionSource = interactions,
        modifier =
            modifier
                .focusRequester(focus)
                .semantics {
                    contentDescription = label
                    if (error != null) error(error)
                },
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
                repeat(length) { index ->
                    PinCell(
                        digit = value.getOrNull(index),
                        current = focused && enabled && index == value.length.coerceAtMost(length - 1),
                        error = error != null,
                    )
                }
            }
        },
    )
}

/** One box: its digit if it has one, outlined in the primary colour while it is the one being typed. */
@Composable
private fun PinCell(
    digit: Char?,
    current: Boolean,
    error: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    val outline =
        when {
            error -> colors.error
            current -> colors.primary
            else -> colors.outline
        }
    val thickness = dimensionResource(if (current || error) R.dimen.pin_cell_border_focused else R.dimen.hairline_thickness)
    Box(
        modifier =
            Modifier
                .size(dimensionResource(R.dimen.pin_cell_size))
                .border(thickness, outline, BingeShapes.ElementSmall),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = digit?.toString().orEmpty(),
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Monospace,
            color = colors.onSurface,
        )
    }
}

/** Four digits unless the caller passes its own length. */
private const val DEFAULT_PIN_LENGTH = 4

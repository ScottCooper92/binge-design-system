package com.binge.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.binge.designsystem.R

private const val DISABLED_ICON_ALPHA = 0.38f

/**
 * A settings text field: one behaviour for keyboard options, error text, a masked value and focus, so every form built from
 * it reads and acts the same. A field that is not [secret] and is not read-only shows a clear button while it is focused and
 * has text, unless [clearable] is false.
 *
 * @param secret masks the value, asks for a password keyboard and gives the field its own reveal toggle, so a long pasted
 *   value can be checked before it is saved. A masked field with no way out of it is the thing this prevents.
 * @param revealed with [onToggleReveal], makes a masked field's reveal state the caller's instead of the field's own.
 * @param autoCorrect off for a value another system has to match exactly, such as a username.
 * @param prose opts a free-text field (a name, a title) into starting each sentence capitalised. Off by default, because a
 *   locale, a path or an identifier is case-sensitive and a keyboard would otherwise change what was typed.
 * @param required marks the label with an asterisk and nothing more: the page reports a missing value through [isError] and
 *   [supporting].
 * @param contentType null by default, because most settings hold a server's secret and not the user's own credential, and a
 *   credential provider should only be offered the latter.
 * @param imeAction [ImeAction.Next] moves focus on, so the last field of a form passes [ImeAction.Done]. Done, and Go for a
 *   field that connects or signs in, drop the keyboard and then run [onDone]. A multi-line field keeps the keyboard's Enter,
 *   whatever this says.
 */
@Composable
fun BingeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    secret: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    autoCorrect: Boolean = true,
    prose: Boolean = false,
    placeholder: String? = null,
    supporting: String? = null,
    isError: Boolean = false,
    required: Boolean = false,
    contentType: ContentType? = null,
    icon: ImageVector? = null,
    clearable: Boolean = true,
    revealed: Boolean? = null,
    onToggleReveal: (() -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }
    // remember rather than rememberSaveable: a field left revealed comes back masked after the app is backgrounded, which is
    // a small leak closed for no loss.
    var localRevealed by remember { mutableStateOf(false) }
    val shown = revealed ?: localRevealed
    val trailing: (@Composable () -> Unit)? =
        when {
            secret -> {
                { RevealToggle(revealed = shown, enabled = enabled) { onToggleReveal?.invoke() ?: run { localRevealed = !localRevealed } } }
            }
            clearable && focused && enabled && !readOnly && value.isNotEmpty() -> {
                { ClearButton { onValueChange("") } }
            }
            else -> null
        }
    val submit = {
        focusManager.clearFocus()
        onDone?.invoke()
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(if (required) stringResource(R.string.field_required_label, label) else label) },
        placeholder = placeholder?.let { { Text(it) } },
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        isError = isError,
        supportingText = supporting?.let { { if (it.isNotEmpty()) Text(it) } },
        leadingIcon =
            icon?.let {
                {
                    Icon(
                        it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.let { primary ->
                            if (enabled) {
                                primary
                            } else {
                                primary.copy(
                                    alpha = DISABLED_ICON_ALPHA,
                                )
                            }
                        },
                    )
                }
            },
        trailingIcon = trailing,
        visualTransformation = if (secret && !shown) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = fieldKeyboardOptions(secret, keyboardType, autoCorrect, prose, fieldImeAction(imeAction, singleLine)),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) },
            onDone = { submit() },
            onGo = { submit() },
        ),
        modifier =
            modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused }
                .then(contentType?.let { type -> Modifier.semantics { this.contentType = type } } ?: Modifier),
    )
}

/** A multi-line field keeps the keyboard's Enter, so the action asked for applies to a single line only. */
internal fun fieldImeAction(requested: ImeAction, singleLine: Boolean): ImeAction = if (singleLine) requested else ImeAction.Default

/**
 * The keyboard a field asks for. Only a field the caller marks as [prose] (a name, a title, a message) starts each sentence
 * capitalised. Everything else, whether a URL, an email, a number, a secret or an identifier, is left exactly as typed, and
 * a non-text keyboard never capitalises even when asked.
 */
internal fun fieldKeyboardOptions(
    secret: Boolean,
    keyboardType: KeyboardType,
    autoCorrect: Boolean,
    prose: Boolean = false,
    imeAction: ImeAction = ImeAction.Unspecified,
): KeyboardOptions {
    val type = if (secret) KeyboardType.Password else keyboardType
    return KeyboardOptions(
        capitalization = if (prose && type == KeyboardType.Text) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
        autoCorrectEnabled = autoCorrect,
        keyboardType = type,
        imeAction = imeAction,
    )
}

/**
 * The X in a focused, non-empty field's trailing slot. Out of the focus order, as the eye is, so the keyboard's Next lands on
 * the next field rather than on the button beside this one.
 */
@Composable
private fun ClearButton(onClear: () -> Unit) {
    IconButton(onClick = onClear, modifier = Modifier.focusProperties { canFocus = false }) {
        Icon(imageVector = Icons.Filled.Clear, contentDescription = stringResource(R.string.field_clear))
    }
}

/**
 * The eye in a masked field's trailing slot. Its description names what the tap will do rather than what the field holds, so
 * a screen reader announces the action and the label is not read twice. It follows the field's own [enabled], which is off
 * only while a save is in flight.
 */
@Composable
internal fun RevealToggle(
    revealed: Boolean,
    enabled: Boolean = true,
    onToggle: () -> Unit,
) {
    IconButton(onClick = onToggle, enabled = enabled, modifier = Modifier.focusProperties { canFocus = false }) {
        Icon(
            imageVector = if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
            contentDescription = stringResource(if (revealed) R.string.field_secret_hide else R.string.field_secret_show),
        )
    }
}

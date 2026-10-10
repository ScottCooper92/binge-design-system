package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.DISABLED_ALPHA
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.TvArrivalFocus
import com.binge.designsystem.tv.focus.tvArrivalTarget
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** What a secret value shows as when it is not being edited: the mask PasswordVisualTransformation draws. */
private const val SECRET_MASK = '•'

/**
 * A text field for a remote, under its [label]. tv-material ships no text field, so this is the foundation one in the
 * TV theme. The remote lands on a frame that wears the focus ring and never changes fill, so the text stays legible.
 * Select starts editing; focus alone never raises the keyboard. ↑ and ↓ leave the field, ← and → move the cursor, and
 * [imeAction] hands focus back to the frame before [onImeAction] runs. [placeholder] shows while the field is empty.
 * A disabled field is dimmed by [DISABLED_ALPHA], label and frame alike.
 * [fillWidth] takes the parent's width rather than a form's. [initiallyFocused] seeds the ring for a frame.
 */
@Composable
fun TvTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String? = null,
    secret: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    autoCorrect: Boolean = true,
    contentType: ContentType? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
    fillWidth: Boolean = false,
    initiallyFocused: Boolean = false,
    arrival: TvArrivalFocus? = null,
) {
    Column(
        modifier = if (fillWidth) modifier else modifier.width(dimensionResource(TvR.dimen.tv_text_field_width)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_xs)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alpha(if (enabled) 1f else DISABLED_ALPHA),
        )
        TvFieldFrame(
            value = value,
            onValueChange = onValueChange,
            name = label,
            shape = BingeShapes.TvListItem,
            enabled = enabled,
            placeholder = placeholder,
            secret = secret,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, autoCorrectEnabled = autoCorrect, imeAction = imeAction),
            contentType = contentType,
            onImeAction = onImeAction,
            initiallyFocused = initiallyFocused,
            arrival = arrival,
        )
    }
}

/**
 * [TvTextField] as a search bar: a pill with a leading search icon and no label above it, as wide as its [modifier]
 * makes it. [label] is what a screen reader calls it, "Search" say, and [placeholder] what to type. The keyboard's
 * action is Search, and runs [onSearch] once focus is back on the frame. The frame, the ring, select to edit and the
 * vertical leave are the text field's.
 */
@Composable
fun TvSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    initiallyFocused: Boolean = false,
    arrival: TvArrivalFocus? = null,
) {
    TvFieldFrame(
        value = value,
        onValueChange = onValueChange,
        name = label,
        shape = BingeShapes.Pill,
        enabled = enabled,
        placeholder = placeholder,
        secret = false,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        contentType = null,
        onImeAction = onSearch,
        initiallyFocused = initiallyFocused,
        arrival = arrival,
        modifier = modifier,
        leading = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(dimensionResource(TvR.dimen.tv_search_field_icon)),
            )
        },
    )
}

/**
 * The frame both fields share. The frame is what the remote lands on; the input inside takes focus only once select
 * is pressed, because a TV raises the keyboard whenever a text input is focused, and walking a form would throw it up
 * at every field. [name] labels the frame and the input alike: while editing, a screen reader is on the input.
 */
@Composable
private fun TvFieldFrame(
    value: String,
    onValueChange: (String) -> Unit,
    name: String,
    shape: Shape,
    enabled: Boolean,
    placeholder: String?,
    secret: Boolean,
    keyboardOptions: KeyboardOptions,
    contentType: ContentType?,
    onImeAction: (() -> Unit)?,
    initiallyFocused: Boolean,
    arrival: TvArrivalFocus?,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    var editing by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val input = remember { FocusRequester() }
    val frame = remember { FocusRequester() }
    LaunchedEffect(editing) { if (editing) input.requestFocus() }
    val finish = {
        // Back to the frame first: an input that gives up focus with nowhere to go sends it to the page's first stop.
        frame.requestFocus()
        editing = false
        onImeAction?.invoke()
    }
    Box(
        modifier =
            modifier
                .alpha(if (enabled) 1f else DISABLED_ALPHA)
                .fillMaxWidth()
                .height(dimensionResource(TvR.dimen.tv_text_field_height))
                .tvFocusIndicator(isFocused = focused || editing, shape = shape)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(dimensionResource(TvR.dimen.tv_text_field_border_width), MaterialTheme.colorScheme.border, shape)
                .then(arrival?.let { Modifier.tvArrivalTarget(it) } ?: Modifier)
                .focusRequester(frame)
                .onFocusChanged { focused = it.isFocused }
                .editOnSelect(enabled = enabled && !editing) { editing = true }
                .focusable(enabled = enabled)
                .semantics { contentDescription = name },
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            visualTransformation = if (secret) PasswordVisualTransformation(SECRET_MASK) else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            keyboardActions = KeyboardActions(onAny = { finish() }),
            modifier =
                Modifier
                    .fillMaxSize()
                    .focusRequester(input)
                    .focusProperties { canFocus = editing }
                    .semantics { contentDescription = name }
                    .then(contentType?.let { type -> Modifier.semantics { this.contentType = type } } ?: Modifier)
                    // Leaving the input, by Back past the keyboard or a move, ends editing: the frame takes over again.
                    .onFocusChanged { if (!it.isFocused && editing) editing = false }
                    .leavesVertically(focusManager),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = dimensionResource(TvR.dimen.tv_text_field_padding_horizontal)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_sm)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leading?.invoke()
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        FieldText(value = value, placeholder = placeholder, secret = secret, editing = editing)
                        // Kept in place but unseen while not editing, so the cursor still sits where typing starts.
                        Box(modifier = Modifier.alpha(if (editing || value.isEmpty()) 1f else 0f)) { inner() }
                    }
                }
            },
        )
    }
}

/**
 * What the field reads while the input is not drawing: the placeholder while empty, else the value on one line with
 * an ellipsis. The input scrolls rather than ellipsising, so a long value or a long localised placeholder would
 * otherwise clip mid-word at a narrow width. The value is drawn for sight only; the input carries it for a reader.
 */
@Composable
private fun FieldText(
    value: String,
    placeholder: String?,
    secret: Boolean,
    editing: Boolean,
) {
    val style = MaterialTheme.typography.titleMedium
    when {
        value.isEmpty() && placeholder != null ->
            Text(
                text = placeholder,
                style = style,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        value.isNotEmpty() && !editing ->
            Text(
                text = if (secret) SECRET_MASK.toString().repeat(value.length) else value,
                style = style,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clearAndSetSemantics {},
            )
    }
}

/** Select (the remote's OK, or Enter) on the frame starts editing; the press is the frame's, so it types nothing. */
private fun Modifier.editOnSelect(enabled: Boolean, onEdit: () -> Unit): Modifier =
    onPreviewKeyEvent { event ->
        val select = enabled && event.isSelect()
        if (select && event.type == KeyEventType.KeyUp) onEdit()
        select
    }

private fun KeyEvent.isSelect(): Boolean = key == Key.DirectionCenter || key == Key.Enter || key == Key.NumPadEnter

/**
 * The input would otherwise swallow ↑ and ↓ as cursor moves it cannot make on one line, and the remote could never
 * leave it. ← and → stay the input's: they move within the typed text.
 */
private fun Modifier.leavesVertically(focusManager: FocusManager): Modifier =
    onPreviewKeyEvent { event ->
        when {
            event.type != KeyEventType.KeyDown -> false
            event.key == Key.DirectionDown -> focusManager.moveFocus(FocusDirection.Down)
            event.key == Key.DirectionUp -> focusManager.moveFocus(FocusDirection.Up)
            else -> false
        }
    }

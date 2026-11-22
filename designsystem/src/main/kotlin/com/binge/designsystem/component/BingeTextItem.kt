package com.binge.designsystem.component

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

private const val MULTILINE_MIN_LINES = 3
private const val MULTILINE_MAX_LINES = 8

/**
 * A text setting as a [ListItem] for an [ItemGroup]: its [title], and its value as the detail ([emptyLabel] while it is blank, or
 * [shown] where the row should not spell the value out, such as a key). A tap opens a sheet to edit it. The sheet belongs to this
 * call, so a page lists its rows and nothing else. A row that is not [enabled] dims, and closes its sheet if open.
 *
 * The setting saves as it is made: Done calls [onChange] with the value and closes the sheet at once, and a failure is the page's to
 * report on its own status line, so the sheet has no spinner and no error of a server's. A value [check] refuses keeps Done off.
 * The value is trimmed, unless it is a [secret], where a password's edge spaces are part of it.
 *
 * @param doneLabel the sheet's confirm button, "Done".
 * @param hint explains the value and stays in view while it is edited.
 * @param placeholder an example of a value, shown only while the field is empty.
 * @param requiredLabel makes the row required: while the value is blank the detail says this, in the error colour.
 * @param check names what is wrong with a value, or null. It keeps Done off, and a value it refuses says so in the detail too.
 * @param secret edits the value masked, with a reveal toggle.
 * @param multiline gives the field room for several lines.
 * @param keyboard the keyboard the sheet asks for.
 */
@Composable
fun bingeTextItem(
    icon: ImageVector,
    title: String,
    value: String,
    onChange: (String) -> Unit,
    emptyLabel: String,
    doneLabel: String,
    enabled: Boolean = true,
    hint: String? = null,
    placeholder: String? = null,
    requiredLabel: String? = null,
    check: (String) -> String? = { null },
    shown: String = value.ifBlank { emptyLabel },
    secret: Boolean = false,
    multiline: Boolean = false,
    keyboard: KeyboardOptions = KeyboardOptions.Default,
): ListItem {
    var open by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(enabled) { if (!enabled) open = false }
    if (open) {
        var draft by rememberSaveable { mutableStateOf(value) }
        val submitted = if (secret) draft else draft.trim()
        BingeAdaptiveSheet(kind = BingeSheetKind.Task, onDismissRequest = { open = false }) {
            TextSettingContent(
                title = title,
                draft = draft,
                onDraftChange = { draft = it },
                onSubmit = {
                    onChange(submitted)
                    open = false
                },
                onCancel = { open = false },
                doneLabel = doneLabel,
                secret = secret,
                hint = hint,
                placeholder = placeholder,
                problem = check(submitted),
                multiline = multiline,
                keyboard = keyboard,
            )
        }
    }
    val problem = rowProblem(value, requiredLabel, secret, check)
    return ListItem(
        icon = icon,
        label = title,
        detail = problem ?: shown,
        detailColor = if (problem != null) MaterialTheme.colorScheme.error else null,
        enabled = enabled,
        onClick = { open = true },
    )
}

/** What a text row says in place of its value when it is wrong: a required value left blank, or one the row's own check refuses. */
private fun rowProblem(
    value: String,
    requiredLabel: String?,
    secret: Boolean,
    check: (String) -> String?,
): String? =
    when {
        requiredLabel != null && value.isBlank() -> requiredLabel
        value.isNotBlank() -> check(if (secret) value else value.trim())
        else -> null
    }

/**
 * [bingeTextItem]'s sheet without its window: the text entry under [title], masked with a reveal toggle for a [secret]. [problem]
 * keeps Done off, and is shown under the field once something has been typed. [hint] stays readable under the title while the value
 * is edited, where [placeholder] is gone once the field holds a value. Use it to frame the sheet in a `@PreviewTest`, where a modal
 * window does not capture, or inside a sheet of the caller's own.
 *
 * @need frame a text setting's sheet in a preview or inside your own sheet
 * @group sheets
 */
@Composable
fun TextSettingContent(
    title: String,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    doneLabel: String,
    modifier: Modifier = Modifier,
    secret: Boolean = false,
    hint: String? = null,
    placeholder: String? = null,
    problem: String? = null,
    multiline: Boolean = false,
    keyboard: KeyboardOptions = KeyboardOptions.Default,
) {
    TextEntrySurface(
        title = title,
        value = draft,
        onValueChange = onDraftChange,
        onSubmit = onSubmit,
        onCancel = onCancel,
        submitLabel = doneLabel,
        hint = placeholder,
        header = { hint?.let { SheetHint(it) } },
        submitEnabled = problem == null,
        error = problem.takeIf { draft.isNotEmpty() },
        singleLine = !multiline,
        secret = secret,
        minLines = MULTILINE_MIN_LINES,
        maxLines = MULTILINE_MAX_LINES,
        keyboardOptions = keyboard,
        modifier = modifier.imePadding(),
    )
}

/** An explanation under a text sheet's title, readable while the field holds a value. */
@Composable
private fun SheetHint(text: String) =
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

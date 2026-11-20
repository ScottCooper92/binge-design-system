package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * What a text field reported up that its caller has not yet taken as its value: the [reported] text, and the value the caller held
 * when it was reported ([against]). While it is pending, a shown value that differs from the field's own text is the caller's answer to
 * that edit (a filter that rejected a character, or a value that arrives a frame late), not a change from outside.
 */
internal data class PendingEdit(
    val reported: String,
    val against: String,
)

/**
 * The value a field should show, given its own [field] (the text and caret the user made), the caller's [value] and the edit it is
 * [pending] an answer to.
 *
 * - They agree: the field's own value, caret and all.
 * - They differ and [field] is the pending edit: the caller answered it, so the caret stays where the user had it, moved by the
 *   difference in length (a rejected character in the middle puts the caret back one, not at the end) and held inside the new text.
 * - They differ otherwise: the value was changed from outside (a clear, a restore), so the caret goes to its end.
 */
internal fun reconcileCaret(
    field: TextFieldValue,
    value: String,
    pending: PendingEdit?,
): TextFieldValue =
    when {
        field.text == value -> field
        pending != null && field.text == pending.reported -> field.answeredWith(value)
        else -> TextFieldValue(value, TextRange(value.length))
    }

/** [this] with its text replaced by [value] and its selection moved back by the difference in length, held inside [value]. */
private fun TextFieldValue.answeredWith(value: String): TextFieldValue {
    val shift = text.length - value.length

    fun moved(index: Int) = (index - shift).coerceIn(0, value.length)
    return TextFieldValue(value, TextRange(moved(selection.start), moved(selection.end)))
}

/**
 * The text, caret and pending edit a text field owns so it can keep the caret where the user left it when the caller rewrites what
 * was typed. Shared by [BingeTextField] and [BingeSearchField], so the two cannot disagree about it.
 */
internal class FieldCaret(
    initial: TextFieldValue,
) {
    var field by mutableStateOf(initial)
        private set
    private var pending by mutableStateOf<PendingEdit?>(null)

    /** What to show for the caller's [value]; see [reconcileCaret]. */
    fun shown(value: String): TextFieldValue = reconcileCaret(field, value, pending)

    /** Takes an edit from the keyboard; returns whether it differs from [value] and so is to be reported to the caller. */
    fun edit(edited: TextFieldValue, value: String): Boolean {
        field = edited
        val report = edited.text != value
        pending = if (report) PendingEdit(edited.text, value) else null
        return report
    }

    /** Sets the text and caret from the field's own side, with nothing pending (focus arriving puts the caret at the end). */
    fun reset(value: TextFieldValue) {
        field = value
        pending = null
    }

    /**
     * Once the caller's [value] has moved off the one it held when the edit was reported, its answer has landed. The field takes what
     * it showed for that answer as its own, so a later recomposition shows the same thing, and nothing is pending any more.
     */
    fun settle(value: String) {
        val edit = pending ?: return
        if (value == edit.against) return
        field = reconcileCaret(field, value, edit)
        pending = null
    }
}

/** A [FieldCaret] for a field showing [value], settled after each composition. */
@Composable
internal fun rememberFieldCaret(value: String, initial: TextFieldValue): FieldCaret {
    val caret = remember { FieldCaret(initial) }
    SideEffect { caret.settle(value) }
    return caret
}

package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * How many frames a field waits for its caller to hand back a value that reflects an edit it reported. A caller that keeps its value in a view model
 * or behind a flow answers a frame or a few late, and the field keeps showing what was typed until then. A caller that rejects an edit by leaving its
 * value as it was never answers, so once the wait is over the field takes the unchanged value as that answer.
 */
internal const val ANSWER_GRACE_FRAMES = 4

/**
 * What a text field reported up that its caller has not yet taken as its value: the [reported] text, and the value the caller held
 * when the first of these edits was reported ([against]). [earlier] are the texts reported before [reported] and still unanswered, which a burst
 * of keystrokes between two frames leaves behind. While the edit is pending, the caller's value being [against] or one of the [earlier] texts
 * means it has not caught up yet; any other value that differs from the field's own text is its answer to the edit (a filter that rewrote a
 * character), or a change from outside.
 */
internal data class PendingEdit(
    val reported: String,
    val against: String,
    val earlier: Set<String> = emptySet(),
) {
    /** Whether [value] is the caller still showing something it held before it took [reported]. */
    fun isBehind(value: String): Boolean = value == against || value in earlier
}

/**
 * An edit the field gave up waiting on and then showed the caller's older value for: the [reported] text and the [selection] the user had in it.
 * If the caller later hands back exactly [reported], that is the answer arriving late, and it lands with the caret where the user typed.
 */
internal data class LateEdit(
    val reported: String,
    val selection: TextRange,
)

/**
 * The value a field should show, given its own [field] (the text and caret the user made), the caller's [value] and the edit it is
 * [pending] an answer to.
 *
 * - They agree: the field's own value, caret and all.
 * - The caller is behind [pending] ([PendingEdit.isBehind]) and the wait for it has not run out ([waiting]): the field's own value, so the next
 *   keystroke builds on what was typed and not on the older text the caller still holds.
 * - They differ and [field] is the pending edit: the caller answered it, so the caret stays where the user had it, moved by the
 *   difference in length (a rejected character in the middle puts the caret back one, not at the end) and held inside the new text.
 * - They differ otherwise: the value was changed from outside (a clear, a restore), so the caret goes to its end.
 */
internal fun reconcileCaret(
    field: TextFieldValue,
    value: String,
    pending: PendingEdit?,
    waiting: Boolean = false,
): TextFieldValue =
    when {
        field.text == value -> field
        pending != null && waiting && pending.isBehind(value) -> field
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
 * The text, caret and pending edit a text field owns so it keeps what the user typed while its caller catches up, and keeps the caret where the
 * user left it when the caller rewrites what was typed. Shared by [BingeTextField] and [BingeSearchField], so the two cannot disagree about it.
 */
internal class FieldCaret(
    initial: TextFieldValue,
) {
    var field by mutableStateOf(initial)
        private set
    private var pending by mutableStateOf<PendingEdit?>(null)

    private var late by mutableStateOf<LateEdit?>(null)

    /** Bumped by each edit and each step of the caller toward it, so the wait for an answer starts again. */
    var progress by mutableStateOf(0)
        private set

    /** Whether the field is waiting for its caller to take an edit it reported. */
    val isWaiting: Boolean get() = pending != null

    /** What to show for the caller's [value]; see [reconcileCaret]. */
    fun shown(value: String): TextFieldValue =
        late?.takeIf { it.reported == value && field.text != value }?.let { TextFieldValue(value, it.selection) }
            ?: reconcileCaret(field, value, pending, waiting = true)

    /**
     * Takes an edit from the keyboard; returns whether it is a text the caller does not yet have and so is to be reported. Edits that arrive before
     * the caller has answered the last one build on it: what the caller is compared with is the last text reported, not the older value it still holds.
     */
    fun edit(edited: TextFieldValue, value: String): Boolean {
        val previous = pending
        late = null
        field = edited
        val known = previous?.reported ?: value
        if (edited.text == known) return false
        pending =
            PendingEdit(
                reported = edited.text,
                against = previous?.against ?: value,
                earlier = if (previous == null) emptySet() else previous.earlier + previous.reported,
            )
        progress++
        return true
    }

    /** Sets the text and caret from the field's own side, with nothing pending (focus arriving puts the caret at the end). */
    fun reset(value: TextFieldValue) {
        field = value
        pending = null
        late = null
    }

    /**
     * Called after each composition with the caller's [value]. The caller reaching the last text reported is the answer arriving as expected, and
     * nothing is pending any more. One of the earlier texts is it still catching up, and the wait goes on. Any other value is its answer to the
     * edit, or a change from outside, and the field takes what it showed for that.
     */
    fun settle(value: String) {
        late?.let {
            if (value == it.reported) field = TextFieldValue(value, it.selection)
            if (value != field.text || value == it.reported) late = null
        }
        val edit = pending ?: return
        when {
            value == edit.reported -> pending = null
            value == edit.against -> Unit
            value in edit.earlier -> progress++
            else -> {
                field = reconcileCaret(field, value, edit, waiting = false)
                pending = null
            }
        }
    }

    /** The wait is over and the caller's [value] is still behind: it has answered by not changing, so the field shows that. */
    fun giveUp(value: String) {
        val edit = pending ?: return
        late = LateEdit(edit.reported, field.selection)
        field = reconcileCaret(field, value, edit, waiting = false)
        pending = null
    }
}

/** A [FieldCaret] for a field showing [value], settled after each composition. */
@Composable
internal fun rememberFieldCaret(value: String, initial: TextFieldValue): FieldCaret {
    val caret = remember { FieldCaret(initial) }
    SideEffect { caret.settle(value) }
    val latest by rememberUpdatedState(value)
    LaunchedEffect(caret.progress) {
        if (caret.isWaiting) {
            repeat(ANSWER_GRACE_FRAMES) { withFrameNanos { } }
            caret.giveUp(latest)
        }
    }
    return caret
}

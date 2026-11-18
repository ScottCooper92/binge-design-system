package com.binge.designsystem.component

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

/** The rule that decides where a text field's caret goes when the caller's value differs from what was typed (#1158). */
class FieldCaretTest {
    private fun typed(text: String, caret: Int) = TextFieldValue(text, TextRange(caret))

    @Test
    fun `a value that agrees with the typed text keeps the field as it is`() {
        val field = typed("8096", 2)

        assertEquals(field, reconcileCaret(field, "8096", pending = null))
    }

    @Test
    fun `a character the caller removed puts the caret back by one, not at the end`() {
        // "80x96" with the caret after the x, answered by "8096".
        val shown = reconcileCaret(typed("80x96", 3), "8096", PendingEdit(reported = "80x96", against = "8096"))

        assertEquals(typed("8096", 2), shown)
    }

    @Test
    fun `a value that arrives late keeps the caret where the user left it`() {
        // Typed "c" at the end of "ab", shown against the old value "ab" until "abc" arrives.
        val shown = reconcileCaret(typed("abc", 3), "ab", PendingEdit(reported = "abc", against = "ab"))

        assertEquals(typed("ab", 2), shown)
    }

    @Test
    fun `a caret moved past the end of a shorter answer is held inside it`() {
        val shown = reconcileCaret(typed("abcdef", 6), "ab", PendingEdit(reported = "abcdef", against = "ab"))

        assertEquals(typed("ab", 2), shown)
    }

    @Test
    fun `a selection keeps its length through the answer`() {
        val shown = reconcileCaret(TextFieldValue("80x96", TextRange(1, 4)), "8096", PendingEdit("80x96", "8096"))

        assertEquals(TextRange(0, 3), shown.selection)
    }

    @Test
    fun `a value changed from outside with nothing pending puts the caret at its end`() {
        val shown = reconcileCaret(typed("gpus", 2), "transcoding", pending = null)

        assertEquals(typed("transcoding", 11), shown)
    }

    @Test
    fun `a value that is not the pending edit's answer counts as from outside`() {
        // The field's own text is no longer the one it reported, so the new value cannot be an answer to it.
        val shown = reconcileCaret(typed("abx", 1), "transcoding", PendingEdit(reported = "abc", against = "ab"))

        assertEquals(typed("transcoding", 11), shown)
    }
}

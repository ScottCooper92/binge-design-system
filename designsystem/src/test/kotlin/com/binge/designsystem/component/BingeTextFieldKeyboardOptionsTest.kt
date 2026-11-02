package com.binge.designsystem.component

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import org.junit.Assert.assertEquals
import org.junit.Test

class BingeTextFieldKeyboardOptionsTest {
    @Test
    fun `a text field is not capitalised unless it opts in`() {
        val options = fieldKeyboardOptions(secret = false, keyboardType = KeyboardType.Text, autoCorrect = true)
        assertEquals(KeyboardCapitalization.None, options.capitalization)
    }

    @Test
    fun `prose starts sentences capitalised`() {
        val options = fieldKeyboardOptions(secret = false, keyboardType = KeyboardType.Text, autoCorrect = true, prose = true)
        assertEquals(KeyboardCapitalization.Sentences, options.capitalization)
        assertEquals(KeyboardType.Text, options.keyboardType)
    }

    @Test
    fun `an identifier is left as typed`() {
        val options = fieldKeyboardOptions(secret = false, keyboardType = KeyboardType.Text, autoCorrect = false)
        assertEquals(KeyboardCapitalization.None, options.capitalization)
    }

    @Test
    fun `urls emails and numbers are not capitalised`() {
        listOf(KeyboardType.Uri, KeyboardType.Email, KeyboardType.Number).forEach { type ->
            val options = fieldKeyboardOptions(secret = false, keyboardType = type, autoCorrect = true, prose = true)
            assertEquals(KeyboardCapitalization.None, options.capitalization)
            assertEquals(type, options.keyboardType)
        }
    }

    @Test
    fun `a secret is a password keyboard and never capitalised`() {
        val options = fieldKeyboardOptions(secret = true, keyboardType = KeyboardType.Text, autoCorrect = true, prose = true)
        assertEquals(KeyboardType.Password, options.keyboardType)
        assertEquals(KeyboardCapitalization.None, options.capitalization)
    }

    @Test
    fun `a multi-line field keeps Enter whatever action is asked for`() {
        assertEquals(ImeAction.Default, fieldImeAction(ImeAction.Next, singleLine = false))
        assertEquals(ImeAction.Next, fieldImeAction(ImeAction.Next, singleLine = true))
        assertEquals(ImeAction.Done, fieldImeAction(ImeAction.Done, singleLine = true))
        assertEquals(ImeAction.Go, fieldImeAction(ImeAction.Go, singleLine = true))
    }
}

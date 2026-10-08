package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingePinFieldTest {
    @get:Rule
    val rule = createComposeRule()

    private var shown = ""
    private val completed = mutableListOf<String>()

    private fun show(initial: String = "") =
        rule.setContent {
            var pin by remember { mutableStateOf(initial) }
            shown = pin
            BingePinField(
                value = pin,
                onValueChange = { pin = it },
                label = "PIN",
                autoFocus = false,
                onComplete = { completed += it },
            )
        }

    @Test
    fun `digits fill the boxes in turn, and the last one completes the PIN once`() {
        show()

        rule.onNodeWithContentDescription("PIN").performTextInput("48")
        rule.waitForIdle()
        assertEquals("48", shown)
        assertEquals(emptyList<String>(), completed)

        rule.onNodeWithContentDescription("PIN").performTextInput("21")
        rule.waitForIdle()
        assertEquals("4821", shown)
        assertEquals(listOf("4821"), completed)
    }

    @Test
    fun `a pasted code keeps only its digits, up to the length`() {
        show()

        rule.onNodeWithContentDescription("PIN").performTextReplacement("PIN: 4 8-2 1 9")
        rule.waitForIdle()

        assertEquals("4821", shown)
        assertEquals(listOf("4821"), completed)
    }

    @Test
    fun `clearing a box steps back without completing`() {
        show(initial = "482")

        rule.onNodeWithContentDescription("PIN").performTextReplacement("48")
        rule.waitForIdle()

        assertEquals("48", shown)
        assertEquals(emptyList<String>(), completed)
    }
}

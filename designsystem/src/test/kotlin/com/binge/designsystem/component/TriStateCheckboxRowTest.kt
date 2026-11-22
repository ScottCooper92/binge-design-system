package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A [TriStateCheckboxRow] announces its state, including the in-between one, and reports a tap without choosing the next state. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TriStateCheckboxRowTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun hasState(state: ToggleableState) = SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, state)

    @Test
    fun `each state is announced as such`() {
        var state by mutableStateOf(ToggleableState.On)
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) { TriStateCheckboxRow(label = "All", state = state, onClick = {}) }
        }

        listOf(ToggleableState.On, ToggleableState.Off, ToggleableState.Indeterminate).forEach { next ->
            rule.runOnIdle { state = next }
            rule.onNodeWithText("All").assert(hasState(next))
        }
    }

    @Test
    fun `a tap calls onClick once and leaves the state to the caller`() {
        var clicks = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                TriStateCheckboxRow(label = "All", state = ToggleableState.Indeterminate, onClick = { clicks++ })
            }
        }

        rule.onNodeWithText("All").performClick()

        assertEquals(1, clicks)
        rule.onNodeWithText("All").assert(hasState(ToggleableState.Indeterminate))
    }

    @Test
    fun `a disabled row does not click`() {
        var clicks = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                TriStateCheckboxRow(label = "All", state = ToggleableState.Off, onClick = { clicks++ }, enabled = false)
            }
        }

        rule.onNodeWithText("All").assertIsNotEnabled().performClick()

        assertEquals(0, clicks)
    }
}

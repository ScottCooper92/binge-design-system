package com.binge.designsystem.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The chips' taps are `onClick` and `onLongClick`, and a positional call is not ambiguous. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ChipClickNamesTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a positional call with four arguments reaches the new function`() {
        var clicks = 0
        rule.setContent {
            BingeExpressiveTheme {
                IncludeExcludeChip("Action", IncludeExcludeState.Neutral, { clicks++ }, {})
            }
        }
        rule.onNodeWithText("Action").performClick()
        assertEquals(1, clicks)
    }
}

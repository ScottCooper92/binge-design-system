package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A row of a single-select list says whether it is the chosen one; a plain row stays a button. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ListRowSelectedSemanticsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun role(role: Role) = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

    @Test
    fun `a selected row is a radio button that reads selected and one that is not reads not selected`() {
        var taps = 0
        composeTestRule.setContent {
            TestTheme {
                Column {
                    ListRow(onClick = { taps++ }, selected = true) { modifier -> Text("Season 1", modifier) }
                    ListRow(onClick = {}, selected = false) { modifier -> Text("Season 2", modifier) }
                }
            }
        }
        composeTestRule
            .onNode(hasText("Season 1"))
            .assert(role(Role.RadioButton))
            .assertIsSelected()
            .assertHasClickAction()
            .performClick()
        composeTestRule.onNode(hasText("Season 2")).assert(role(Role.RadioButton)).assertIsNotSelected()
        assertEquals(1, taps)
    }

    @Test
    fun `a row with no selected state is still a button`() {
        composeTestRule.setContent {
            TestTheme {
                ListRow(onClick = {}) { modifier -> Text("Season 1", modifier) }
            }
        }
        composeTestRule
            .onNode(
                hasText("Season 1"),
            ).assert(role(Role.Button))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
    }
}

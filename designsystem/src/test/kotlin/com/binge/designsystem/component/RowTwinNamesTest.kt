package com.binge.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * `CheckboxRow` and `BingeChoice` name their callback and second line the way the TV rows do.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class RowTwinNamesTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a checkbox row takes onCheckedChange and supportingText`() {
        var checked: Boolean? = null
        rule.setContent {
            CheckboxRow(
                label = "New",
                checked = false,
                onCheckedChange = { checked = it },
                supportingText = "Says what it does",
            )
        }

        rule.onNodeWithText("Says what it does").assertIsDisplayed()
        rule.onNodeWithText("New").performClick()

        assertEquals(true, checked)
    }

    @Test
    fun `a checkbox row called with its first three arguments by position resolves and toggles`() {
        var checked: Boolean? = null
        rule.setContent { CheckboxRow("Positional", false, { checked = it }) }

        rule.onNodeWithText("Positional").performClick()

        assertEquals(true, checked)
    }

    @Test
    fun `a choice built with supportingText or a positional third argument carries it`() {
        assertEquals("New line", BingeChoice("a", "Alpha", supportingText = "New line").supportingText)
        assertEquals("Positional", BingeChoice("a", "Alpha", "Positional").supportingText)
    }
}

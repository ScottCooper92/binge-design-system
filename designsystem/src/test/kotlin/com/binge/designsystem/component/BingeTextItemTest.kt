package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeTextItemTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var value by mutableStateOf("")
    private var enabled by mutableStateOf(true)
    private var saved: String? = null

    private fun show(
        secret: Boolean = false,
        requiredLabel: String? = null,
        check: (String) -> String? = { null },
        shown: String? = null,
    ) {
        rule.setContent {
            ItemGroup(
                title = "Server",
                rows =
                    listOf(
                        bingeTextItem(
                            icon = Icons.Filled.Dns,
                            title = "Address",
                            value = value,
                            onChange = {
                                saved = it
                                value = it
                            },
                            emptyLabel = "Not set",
                            doneLabel = "Done",
                            enabled = enabled,
                            requiredLabel = requiredLabel,
                            check = check,
                            shown = shown ?: value.ifBlank { "Not set" },
                            secret = secret,
                        ),
                    ),
            )
        }
    }

    private fun openSheet() {
        rule.onNodeWithText("Address").performClick()
        rule.waitForIdle()
    }

    @Test
    fun theRowNamesTheValueOrItsEmptyLabel() {
        value = ""
        show()
        rule.onNodeWithText("Not set").assertIsDisplayed()

        value = "media.local"
        rule.waitForIdle()
        rule.onNodeWithText("media.local").assertIsDisplayed()
    }

    @Test
    fun doneSavesTheTrimmedValueAndClosesTheSheetAtOnce() {
        show()
        openSheet()

        rule.onNode(hasSetTextAction()).performTextInput("  media.local  ")
        rule.onNodeWithText("Done").performClick()
        rule.waitForIdle()

        assertEquals("media.local", saved)
        assertEquals("the sheet is gone", 0, rule.onAllNodesWithTextCount("Done"))
    }

    @Test
    fun aSecretKeepsItsEdgeSpaces() {
        show(secret = true)
        openSheet()

        rule.onNode(hasSetTextAction()).performTextInput(" pass ")
        rule.onNodeWithText("Done").performClick()
        rule.waitForIdle()

        assertEquals(" pass ", saved)
    }

    @Test
    fun aValueTheCheckRefusesKeepsDoneOffAndSaysSo() {
        show(check = { if (it.contains(' ')) "No spaces" else null })
        openSheet()

        rule.onNode(hasSetTextAction()).performTextInput("a b")
        rule.waitForIdle()

        rule.onNodeWithText("Done").assertIsNotEnabled()
        rule.onNodeWithText("No spaces").assertIsDisplayed()
        assertNull(saved)

        rule.onNode(hasSetTextAction()).performTextClearance()
        rule.onNode(hasSetTextAction()).performTextInput("ab")
        rule.waitForIdle()
        rule.onNodeWithText("Done").assertIsEnabled()
    }

    @Test
    fun aRequiredRowLeftBlankSaysSoInItsDetail() {
        value = ""
        show(requiredLabel = "Required")

        rule.onNodeWithText("Required").assertIsDisplayed()
    }

    @Test
    fun aSavedValueTheCheckRefusesSaysSoInTheRow() {
        value = "a b"
        show(check = { if (it.contains(' ')) "No spaces" else null })

        rule.onNodeWithText("No spaces").assertIsDisplayed()
    }

    @Test
    fun theRowShowsWhatItIsToldToShowInsteadOfTheValue() {
        value = "secret-key-123"
        show(shown = "••••••••")

        rule.onNodeWithText("••••••••").assertIsDisplayed()
        assertEquals(0, rule.onAllNodesWithTextCount("secret-key-123"))
    }

    @Test
    fun aRowThatIsDisabledClosesItsSheet() {
        show()
        openSheet()
        rule.onNodeWithText("Done").assertIsDisplayed()

        enabled = false
        rule.waitForIdle()

        assertEquals(0, rule.onAllNodesWithTextCount("Done"))
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.onAllNodesWithTextCount(text: String) =
        onAllNodesWithText(text).fetchSemanticsNodes().size
}

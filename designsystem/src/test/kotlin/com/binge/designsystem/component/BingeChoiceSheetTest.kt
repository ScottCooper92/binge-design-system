package com.binge.designsystem.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeChoiceSheetTest {
    @get:Rule
    val rule = createComposeRule()

    private val many = BingeChoiceList.Ready((1..100).map { BingeChoice(it, "Choice $it") })

    @Test
    fun `the last choice of a long list is reachable and picks`() {
        var picked: Int? = null
        rule.setContent { SingleChoiceList(choices = many, selected = 1, modifier = Modifier.heightIn(max = 300.dp)) { picked = it } }

        // A long list is lazy, so the row is composed only once scrolled to.
        rule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Choice 100"))
        rule
            .onNodeWithText("Choice 100")
            .assertIsDisplayed()
            .performClick()

        assertEquals(100, picked)
    }

    @Test
    fun `chosen values lead, by the selection the sheet opened with, and a tick does not move a row`() {
        val choices = BingeChoiceList.Ready(listOf(BingeChoice("a", "Alpha"), BingeChoice("b", "Bravo"), BingeChoice("c", "Charlie")))
        rule.setContent {
            var chosen by remember { mutableStateOf(setOf("c")) }
            MultiChoiceList(
                choices = choices,
                chosen = chosen,
                leading = setOf("c"),
                filterPlaceholder = null,
                onToggle = { value, on -> chosen = if (on) chosen + value else chosen - value },
            )
        }
        rule.onNodeWithText("Bravo").performClick()

        rule.onNodeWithText("Bravo").assertIsOn()
        val labels = listOf("Charlie", "Alpha", "Bravo").map {
            rule
                .onNodeWithText(it)
                .fetchSemanticsNode()
                .boundsInRoot.top
        }
        assertEquals(labels.sorted(), labels)
    }

    @Test
    fun `the filter narrows the rows by label`() {
        rule.setContent {
            MultiChoiceList(
                choices = BingeChoiceList.Ready(
                    listOf(BingeChoice("de", "German"), BingeChoice("ja", "Japanese"), BingeChoice("es", "Spanish")),
                ),
                chosen = emptySet(),
                leading = emptySet(),
                filterPlaceholder = "Filter",
                onToggle = { _, _ -> },
            )
        }

        rule.onNode(hasSetTextAction()).performTextInput("an")

        rule.onNodeWithText("German").assertIsDisplayed()
        rule.onNodeWithText("Japanese").assertIsDisplayed()
        rule.onNodeWithText("Spanish").assertIsDisplayed()
        rule.onNode(hasSetTextAction()).performTextInput("ese")
        assertEquals(0, rule.onAllNodesWithText("German").fetchSemanticsNodes().size)
        rule.onNodeWithText("Japanese").assertIsDisplayed()
    }

    @Test
    fun `a part-way sheet opens fully while the keyboard is up, so the keyboard does not cover the filter`() {
        var dock: BingeSheetDock? = null
        var keyboard by mutableStateOf(false)
        rule.setContent {
            BingeBottomSheet(onDismissRequest = {}, skipPartiallyExpanded = false, dockable = true) {
                dock = LocalBingeSheetDock.current
                MultiChoiceList(
                    choices = many,
                    chosen = emptySet(),
                    leading = emptySet(),
                    filterPlaceholder = "Filter",
                    onToggle = { _, _ -> },
                    imeVisible = keyboard,
                )
            }
        }
        rule.waitForIdle()
        assertTrue(dock!!.canExpand)

        keyboard = true
        rule.waitForIdle()

        assertFalse(dock!!.canExpand)
        assertTrue(dock!!.canCollapse)
    }

    @Test
    fun `a failed list offers its retry`() {
        var retried = false
        rule.setContent {
            SingleChoiceList<String>(BingeChoiceList.Failed("No list.", "Try again") { retried = true }, selected = null, onSelect = {})
        }

        rule.onNodeWithText("Try again").performClick()

        assertTrue(retried)
    }

    @Test
    fun `a long or loading list opens part-way and a short or failed one does not`() {
        assertTrue(many.opensPartWay())
        assertTrue(BingeChoiceList.Loading.opensPartWay())
        assertFalse(BingeChoiceList.Ready((1..PEEK_THRESHOLD).map { BingeChoice(it, "$it") }).opensPartWay())
        assertFalse(BingeChoiceList.Failed("x", "y") {}.opensPartWay())
    }

    @Test
    fun `a loading row names the saved selection, not the empty label, and tapping reports the open`() {
        var opened = 0
        rule.setContent {
            ItemGroup(
                title = "Group",
                rows =
                    listOf(
                        bingeChoiceItem(
                            icon = Icons.Filled.Public,
                            title = "Region",
                            choices = BingeChoiceList.Loading,
                            selected = "jp",
                            emptyLabel = "Not set",
                            onSelect = {},
                            onOpen = { opened++ },
                            selectedLabel = "Japan",
                        ),
                    ),
            )
        }

        rule.onNodeWithText("Japan").assertIsDisplayed()
        assertTrue(rule.onAllNodesWithText("Not set").fetchSemanticsNodes().isEmpty())
        rule.onNodeWithText("Region").performClick()
        rule.waitForIdle()
        assertEquals(1, opened)
    }

    @Test
    fun a_row_restored_open_runs_onOpen_again() {
        val restoration = StateRestorationTester(rule)
        var opened = 0
        restoration.setContent {
            ItemGroup(
                title = "Group",
                rows =
                    listOf(
                        bingeChoiceItem(
                            icon = Icons.Filled.Public,
                            title = "Region",
                            choices = BingeChoiceList.Loading,
                            selected = null,
                            emptyLabel = "Not set",
                            onSelect = {},
                            onOpen = { opened++ },
                        ),
                    ),
            )
        }

        rule.onNodeWithText("Region").performClick()
        rule.waitForIdle()
        assertEquals(1, opened)
        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()
        assertEquals(2, opened)
    }

    private fun ticksAcrossRestore(draftSaver: Saver<Set<String>, out Any>?): Set<String>? {
        val restoration = StateRestorationTester(rule)
        var done: Set<String>? = null
        val choices = BingeChoiceList.Ready(listOf(BingeChoice("a", "Alpha"), BingeChoice("b", "Bravo")))
        restoration.setContent {
            ItemGroup(
                title = "Group",
                rows =
                    listOf(
                        bingeMultiChoiceItem(
                            icon = Icons.Filled.Public,
                            title = "Languages",
                            choices = choices,
                            selected = setOf("a"),
                            emptyLabel = "None",
                            doneLabel = "Done",
                            clearLabel = "Clear",
                            onDone = { done = it },
                            draftSaver = draftSaver,
                        ),
                    ),
            )
        }

        rule.onNodeWithText("Languages").performClick()
        rule.onNodeWithText("Bravo").performClick()
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Done").performClick()
        return done
    }

    @Test
    fun a_multi_choice_sheet_with_a_draft_saver_keeps_its_ticks_across_a_restore() {
        assertEquals(setOf("a", "b"), ticksAcrossRestore(Saver(save = { ArrayList(it) }, restore = { it.toSet() })))
    }

    @Test
    fun a_multi_choice_sheet_without_a_draft_saver_reopens_from_the_selection() {
        assertEquals(setOf("a"), ticksAcrossRestore(draftSaver = null))
    }
}

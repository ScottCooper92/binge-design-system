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
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
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
    fun `a short list's marks show, and a pinned value leads even what was already chosen`() {
        val choices =
            BingeChoiceList.Ready(
                listOf(
                    BingeChoice("any", "Any language"),
                    BingeChoice("de", "German", mark = "DE"),
                    BingeChoice("ja", "Japanese", mark = "JA"),
                ),
            )
        rule.setContent {
            var chosen by remember { mutableStateOf(setOf("ja")) }
            MultiChoiceList(
                choices = choices,
                chosen = chosen,
                leading = setOf("ja"),
                filterPlaceholder = null,
                onToggle = { value, on -> chosen = if (on) chosen + value else chosen - value },
                pinned = listOf("any"),
            )
        }
        rule.onNodeWithText("DE", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("German").performClick()

        rule.onNodeWithText("German").assertIsOn()
        val labels = listOf("Any language", "Japanese", "German").map {
            rule
                .onNodeWithText(it)
                .fetchSemanticsNode()
                .boundsInRoot.top
        }
        assertEquals(labels.sorted(), labels)
    }

    @Test
    fun `a trailing text shows once beside its choice in a single choice list`() {
        val choices =
            BingeChoiceList.Ready(listOf(BingeChoice("a", "Backdrops", trailingText = "24"), BingeChoice("b", "Logos")))
        rule.setContent {
            SingleChoiceList(choices = choices, selected = "a", onSelect = {})
        }
        rule.onNodeWithText("24").assertIsDisplayed()
        assertEquals(1, rule.onAllNodesWithText("24").fetchSemanticsNodes().size)
    }

    @Test
    fun `a trailing text shows in a multi choice list`() {
        rule.setContent {
            MultiChoiceList(
                choices = BingeChoiceList.Ready(listOf(BingeChoice("a", "Backdrops", trailingText = "24"), BingeChoice("b", "Logos"))),
                chosen = emptySet(),
                leading = emptySet(),
                filterPlaceholder = "Filter",
                onToggle = { _, _ -> },
            )
        }
        rule.onNodeWithText("24").assertIsDisplayed()
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
        assertFalse(BingeChoiceList.Ready((1 until LONG_LIST_THRESHOLD).map { BingeChoice(it, "$it") }).opensPartWay())
        // The same count that sections a list opens its sheet part-way, so a sectioned list is never in a short sheet.
        assertTrue(BingeChoiceList.Ready((1..LONG_LIST_THRESHOLD).map { BingeChoice(it, "$it") }).opensPartWay())
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

    /** The restored sheet state was saved under the decision it opened with, so a recreation keeps it (#441). */
    @Test
    fun `the part-way decision survives a recreation after the list changes length`() {
        val restoration = StateRestorationTester(rule)
        var choices: BingeChoiceList<Int> by mutableStateOf(BingeChoiceList.Ready(listOf(BingeChoice(1, "One"))))
        var partWay: Boolean? = null
        restoration.setContent { partWay = rememberOpensPartWay(choices) }
        rule.waitForIdle()
        assertEquals(false, partWay)

        choices = many
        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        assertEquals(false, partWay)
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

    @Test
    fun a_multi_choice_sheet_that_applies_as_picked_reports_each_tick_and_clear_and_shows_no_done() {
        val applied = mutableListOf<Set<String>>()
        val choices = BingeChoiceList.Ready(listOf(BingeChoice("a", "Alpha"), BingeChoice("b", "Bravo")))
        rule.setContent {
            var selected by remember { mutableStateOf(setOf("a")) }
            ItemGroup(
                title = "Group",
                rows =
                    listOf(
                        bingeMultiChoiceItem(
                            icon = Icons.Filled.Public,
                            title = "Languages",
                            choices = choices,
                            selected = selected,
                            emptyLabel = "None",
                            doneLabel = "Done",
                            clearLabel = "Clear",
                            onDone = {
                                applied += it
                                selected = it
                            },
                            applyAsPicked = true,
                        ),
                    ),
            )
        }

        rule.onNodeWithText("Languages").performClick()
        assertTrue(rule.onAllNodesWithText("Done").fetchSemanticsNodes().isEmpty())
        rule.onNodeWithText("Bravo").performClick()
        rule.onNodeWithText("Clear").performClick()

        assertEquals(listOf(setOf("a", "b"), emptySet<String>()), applied)
    }

    @Test
    fun a_multi_choice_sheet_that_applies_as_picked_keeps_its_row_order_when_a_row_is_ticked() {
        val choices =
            BingeChoiceList.Ready(
                listOf(BingeChoice("a", "Alpha"), BingeChoice("b", "Bravo"), BingeChoice("c", "Charlie")),
            )
        rule.setContent {
            var selected by remember { mutableStateOf(setOf("a")) }
            ItemGroup(
                title = "Group",
                rows =
                    listOf(
                        bingeMultiChoiceItem(
                            icon = Icons.Filled.Public,
                            title = "Languages",
                            choices = choices,
                            selected = selected,
                            emptyLabel = "None",
                            doneLabel = "Done",
                            clearLabel = "Clear",
                            onDone = { selected = it },
                            applyAsPicked = true,
                        ),
                    ),
            )
        }

        rule.onNodeWithText("Languages").performClick()

        fun tops() = listOf("Alpha", "Bravo", "Charlie").map { rule.onNode(hasText(it) and isToggleable()).getBoundsInRoot().top }
        val before = tops()
        rule.onNode(hasText("Charlie") and isToggleable()).performClick()

        assertEquals(before, tops())
    }
}

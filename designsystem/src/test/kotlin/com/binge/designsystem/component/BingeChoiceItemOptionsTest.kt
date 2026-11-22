package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val REGIONS =
    BingeChoiceList.Ready(
        listOf(
            BingeChoice("any", "Any region"),
            BingeChoice("jp", "Japan"),
            BingeChoice("nz", "New Zealand"),
        ),
    )

/** The sheet options [bingeChoiceItem] and [bingeMultiChoiceItem] hand to their sheets, and the row's own tint and key. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeChoiceItemOptionsTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `a single choice row's sheet takes its own title, subtitle, caption and filter, and a pick reaches the caller`() {
        var picked: String? = null
        rule.setContent {
            TestTheme {
                ItemGroup(
                    title = null,
                    rows =
                        listOf(
                            bingeChoiceItem(
                                icon = Icons.Filled.Public,
                                title = "Region",
                                choices = REGIONS,
                                selected = "jp",
                                emptyLabel = "Not set",
                                onSelect = { picked = it },
                                sheetTitle = "Pick a region",
                                subtitle = "Where it is released",
                                caption = "Applies to new requests",
                                filterPlaceholder = "Search regions",
                            ),
                        ),
                )
            }
        }

        rule.onNodeWithText("Region").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Pick a region").assertIsDisplayed()
        rule.onNodeWithText("Where it is released").assertIsDisplayed()
        rule.onNodeWithText("Applies to new requests").assertIsDisplayed()
        rule.onNodeWithText("Search regions").assertIsDisplayed()
        rule.onNodeWithText("New Zealand").performClick()
        rule.waitForIdle()
        assertEquals("nz", picked)
    }

    @Test
    fun `a row with no options draws the sheet as it did, titled by the row`() {
        rule.setContent {
            TestTheme {
                ItemGroup(
                    title = null,
                    rows =
                        listOf(
                            bingeChoiceItem(
                                icon = Icons.Filled.Public,
                                title = "Region",
                                choices = REGIONS,
                                selected = "jp",
                                emptyLabel = "Not set",
                                onSelect = {},
                            ),
                        ),
                )
            }
        }

        rule.onNodeWithText("Region").performClick()
        rule.waitForIdle()

        // The row and the sheet both say "Region" now: two nodes, where a custom sheet title would have left one.
        assertEquals(2, rule.onAllNodesWithTextCount("Region"))
    }

    @Test
    fun `a pinned value leads the sheet's list even when it is last in the list's own order`() {
        rule.setContent {
            TestTheme {
                ItemGroup(
                    title = null,
                    rows =
                        listOf(
                            bingeChoiceItem(
                                icon = Icons.Filled.Public,
                                title = "Region",
                                choices = REGIONS,
                                selected = "jp",
                                emptyLabel = "Not set",
                                onSelect = {},
                                pinned = listOf("nz"),
                            ),
                        ),
                )
            }
        }

        rule.onNodeWithText("Region").performClick()
        rule.waitForIdle()

        val pinned = rule
            .onNodeWithText("New Zealand")
            .fetchSemanticsNode()
            .boundsInRoot.top
        val other = rule
            .onNodeWithText("Any region")
            .fetchSemanticsNode()
            .boundsInRoot.top
        assertTrue("the pinned value, last in the list's own order, is drawn above the rest", pinned < other)
    }

    @Test
    fun `the row carries its icon tint and key, and a multi-choice row its sheet's title and caption`() {
        lateinit var single: ListItem
        lateinit var multi: ListItem
        rule.setContent {
            TestTheme {
                single =
                    bingeChoiceItem(
                        icon = Icons.Filled.Public,
                        title = "Region",
                        choices = REGIONS,
                        selected = "jp",
                        emptyLabel = "Not set",
                        onSelect = {},
                        iconTint = Color.Red,
                        key = "region",
                    )
                multi =
                    bingeMultiChoiceItem(
                        icon = Icons.Filled.Public,
                        title = "Regions",
                        choices = REGIONS,
                        selected = setOf("jp"),
                        emptyLabel = "None",
                        doneLabel = "Done",
                        clearLabel = "Clear",
                        onDone = {},
                        sheetTitle = "Pick regions",
                        caption = "Several allowed",
                        iconTint = Color.Blue,
                        key = "regions",
                    )
                ItemGroup(title = null, rows = listOf(single, multi))
            }
        }

        assertEquals(Color.Red, single.iconTint)
        assertEquals("region", single.key)
        assertEquals(Color.Blue, multi.iconTint)
        assertEquals("regions", multi.key)
        rule.onNodeWithText("Regions").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Pick regions").assertIsDisplayed()
        rule.onNodeWithText("Several allowed").assertIsDisplayed()
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTextCount(text: String): Int =
        onAllNodes(
            androidx.compose.ui.test
                .hasText(text),
            useUnmergedTree = true,
        ).fetchSemanticsNodes().size
}

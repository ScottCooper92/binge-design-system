package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val KEY_QUALITY = "quality"
private const val ROW_QUALITY = "Default quality"
private const val OPTION_HD = "HD"
private const val OPTION_UHD = "4K"
private const val DETAIL_HD = "HD detail"
private const val GROUP_NOTE = "Changed from your phone."

/**
 * A [TvListPaneBoard] pane's options are one selectable group (#678), as [com.binge.designsystem.tv.component.TvOptionGroup]'s
 * and [com.binge.designsystem.tv.component.TvChoiceRow]'s are, so a screen reader announces them as a set. The list's
 * rows are not: moving along them describes a row, it chooses nothing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvListPaneSemanticsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val inSelectableGroup = hasAnyAncestor(SemanticsMatcher.keyIsDefined(SemanticsProperties.SelectableGroup))

    @Test
    fun `the pane's options sit in one selectable group`() {
        setBoard()

        composeTestRule.onNode(hasText(OPTION_HD) and inSelectableGroup).assertExists()
        composeTestRule.onNode(hasText(OPTION_UHD) and inSelectableGroup).assertExists()
    }

    @Test
    fun `the list's rows are not a selectable group`() {
        setBoard()

        // The pane's own heading carries the row's name too, so only the focusable list row is checked.
        composeTestRule.onNode(hasText(ROW_QUALITY) and isFocusable() and inSelectableGroup).assertDoesNotExist()
    }

    @Test
    fun `a row with a detail shows it under the name in the list`() {
        setBoard(detail = DETAIL_HD)

        composeTestRule.onNode(hasText(DETAIL_HD)).assertExists()
    }

    @Test
    fun `a row with no detail shows no extra line`() {
        setBoard()

        composeTestRule.onNode(hasText(DETAIL_HD)).assertDoesNotExist()
    }

    @Test
    fun `a group note reads under its title and never takes focus`() {
        setBoard(groupNote = GROUP_NOTE)

        composeTestRule.onNode(hasText(GROUP_NOTE)).assertExists()
        composeTestRule.onNode(hasText(GROUP_NOTE) and isFocusable()).assertDoesNotExist()
    }

    @Test
    fun `a group with no note shows none`() {
        setBoard()

        composeTestRule.onNode(hasText(GROUP_NOTE)).assertDoesNotExist()
    }

    @Test
    fun `a long option list composes only the options in view`() {
        val labels = (1..300).map { "Option $it" }
        setBoard(options = labels)

        val composed = labels.count { label -> composeTestRule.onAllNodes(hasText(label)).fetchSemanticsNodes().isNotEmpty() }
        assertTrue("composed $composed of 300 options", composed in 1..40)
        composeTestRule.onNode(hasText("Option 1") and isFocusable()).assertExists()
        composeTestRule.onNode(hasText("Option 300")).assertDoesNotExist()
    }

    private fun setBoard(
        detail: String? = null,
        groupNote: String? = null,
        options: List<String>? = null,
    ) {
        composeTestRule.setContent {
            BingeTvTheme {
                TvListPaneBoard(
                    title = "Settings",
                    groups =
                        listOf(
                            TvPaneGroup(
                                title = "Playback",
                                note = groupNote,
                                rows =
                                    listOf(
                                        TvPaneRow(
                                            key = KEY_QUALITY,
                                            label = ROW_QUALITY,
                                            body = "The quality a new request asks for.",
                                            detail = detail,
                                            options =
                                                options?.map { TvPaneOption(label = it, onSelect = {}) }
                                                    ?: listOf(
                                                        TvPaneOption(label = OPTION_HD, selected = true, onSelect = {}),
                                                        TvPaneOption(label = OPTION_UHD, onSelect = {}),
                                                    ),
                                        ),
                                    ),
                            ),
                        ),
                    focusedKey = KEY_QUALITY,
                    onFocusRow = {},
                )
            }
        }
        composeTestRule.waitForIdle()
    }
}

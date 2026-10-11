package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val KEY_QUALITY = "quality"
private const val ROW_QUALITY = "Default quality"
private const val OPTION_HD = "HD"
private const val OPTION_UHD = "4K"

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

        composeTestRule.onNode(hasText(ROW_QUALITY) and inSelectableGroup).assertDoesNotExist()
    }

    private fun setBoard() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvListPaneBoard(
                    title = "Settings",
                    groups =
                        listOf(
                            TvPaneGroup(
                                title = "Playback",
                                rows =
                                    listOf(
                                        TvPaneRow(
                                            key = KEY_QUALITY,
                                            label = ROW_QUALITY,
                                            body = "The quality a new request asks for.",
                                            options =
                                                listOf(
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

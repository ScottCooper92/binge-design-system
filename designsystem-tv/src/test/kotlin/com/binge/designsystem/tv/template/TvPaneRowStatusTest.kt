package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val KEY = "companions"
private const val LABEL = "Companion apps"
private const val COUNT = 3
private const val COUNT_DESCRIPTION = "3 new"
private const val ALERT_DESCRIPTION = "Needs attention"
private const val IN_PROGRESS = "In progress"

/**
 * A [TvPaneRow]'s status mark speaks as words, not as its glyph: the count as how many are new, the alert as needing
 * attention, and a loading row as in progress on the row itself. Loading wins over the alert, the alert over the count.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvPaneRowStatusTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val inProgress = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, IN_PROGRESS)

    @Test
    fun `a count is described as how many are new, not as its number`() {
        setRow(badgeCount = COUNT)

        composeTestRule.onNode(hasContentDescription(COUNT_DESCRIPTION), useUnmergedTree = true).assertExists()
        composeTestRule.onNode(hasText(COUNT.toString()), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `a zero count draws no mark`() {
        setRow(badgeCount = 0)

        composeTestRule.onNode(hasText("0"), useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNode(hasContentDescription("0 new"), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `an alert is described as needing attention, and wins over a count`() {
        setRow(badgeCount = COUNT, badgeAlert = true)

        composeTestRule.onNode(hasContentDescription(ALERT_DESCRIPTION), useUnmergedTree = true).assertExists()
        composeTestRule.onNode(hasContentDescription(COUNT_DESCRIPTION), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `a loading row says it is in progress, and wins over the badges`() {
        setRow(badgeCount = COUNT, badgeAlert = true, loading = true)

        composeTestRule.onNode(hasText(LABEL) and inProgress).assertExists()
        composeTestRule.onNode(hasContentDescription(ALERT_DESCRIPTION), useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNode(hasContentDescription(COUNT_DESCRIPTION), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `a row that is not loading says nothing about progress`() {
        setRow(badgeCount = COUNT)

        composeTestRule.onNode(hasText(LABEL) and inProgress).assertDoesNotExist()
    }

    private fun setRow(
        badgeCount: Int? = null,
        badgeAlert: Boolean = false,
        loading: Boolean = false,
    ) {
        composeTestRule.setContent {
            BingeTvTheme {
                TvListPaneBoard(
                    title = "Settings",
                    groups =
                        listOf(
                            TvPaneGroup(
                                title = "Apps",
                                rows =
                                    listOf(
                                        TvPaneRow(
                                            key = KEY,
                                            label = LABEL,
                                            body = "The apps that add to Binge.",
                                            badgeCount = badgeCount,
                                            badgeAlert = badgeAlert,
                                            loading = loading,
                                        ),
                                    ),
                            ),
                        ),
                    focusedKey = KEY,
                    onFocusRow = {},
                )
            }
        }
    }
}

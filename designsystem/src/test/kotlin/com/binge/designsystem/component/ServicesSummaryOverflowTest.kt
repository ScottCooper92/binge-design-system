package com.binge.designsystem.component

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private fun providers(count: Int) = (1..count).map { WatchProviderUi(id = it, name = "Service $it", logoUrl = "") }

/**
 * Guards the cap that keeps [ServicesSummary]'s logo row from overflowing its container with a large
 * selection (binge-design-system#98). A selection at or under the cap still renders one logo per
 * provider — the pre-fix, still-correct case; past it, the row stops growing and folds the remainder
 * into a "+N" badge that carries its own accessible name rather than the raw provider count.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ServicesSummaryOverflowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setSelection(count: Int) {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ServicesSummary(
                    selectedProviders = providers(count),
                    leadText = "Your services",
                    emptyText = "Pick the streaming services you subscribe to.",
                )
            }
        }
    }

    @Test
    fun `a selection at the cap renders every logo and no overflow badge`() {
        setSelection(5)

        composeTestRule.onAllNodesWithContentDescription("Service", substring = true).assertCountEquals(5)
        composeTestRule.onAllNodesWithContentDescription("more service", substring = true).assertCountEquals(0)
    }

    @Test
    fun `a selection past the cap stops adding logos and shows the remainder as a badge`() {
        setSelection(9)

        composeTestRule.onAllNodesWithContentDescription("Service", substring = true).assertCountEquals(5)
        composeTestRule.onNodeWithContentDescription("4 more services selected").assertExists()
        // The badge is a single accessible node — its "+4" glyph is intentionally not a separate
        // reading, or a screen reader would announce both the description and the raw digits.
        composeTestRule.onAllNodesWithText("+4").assertCountEquals(0)
    }

    @Test
    fun `a single remaining provider gets the singular description`() {
        setSelection(6)

        composeTestRule.onNodeWithContentDescription("1 more service selected").assertExists()
    }
}

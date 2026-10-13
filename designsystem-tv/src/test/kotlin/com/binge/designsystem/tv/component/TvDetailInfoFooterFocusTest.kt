package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.template.TvDetailPage
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val HERO = "hero-marker"
private const val HEADER = "Details"

private val DETAILS_ANCHOR = TvLayoutAnchors.entry(TV_DETAILS_SECTION_KEY)

/**
 * The Details footer on a real [TvDetailPage]: ↓ from the row above lands on it as one focus stop, and [detailsSection]
 * registers it only when the footer would draw something.
 *
 * The blank-groups case is the one worth having. The footer drops blank values before deciding whether to render, so a
 * list of groups that all hold blanks is non-empty to the section and draws nothing in the footer. Registered anyway, it
 * would leave an empty entry and gap at the end of the page. The hero is asserted beside every negative, so "no details"
 * cannot pass on a page that drew nothing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvDetailInfoFooterFocusTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `down from the row above lands on the footer as one focus stop`() {
        setPage(listOf(TvInfoGroup("Requested by", "Scott"), TvInfoGroup("Status", "Pending")))
        composeTestRule.onNodeWithTag(HERO).requestFocus()
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().performKeyInput { pressKey(Key.DirectionDown) }
        composeTestRule.waitForIdle()

        composeTestRule.onNode(isFocused() and hasAnyDescendant(hasText(HEADER))).assertIsDisplayed()
    }

    @Test
    fun `groups with values register the section`() {
        setPage(listOf(TvInfoGroup(label = "Director", value = "Jane Director")))

        composeTestRule.onNodeWithTag(DETAILS_ANCHOR).assertIsDisplayed()
    }

    @Test
    fun `no groups register no section`() {
        setPage(emptyList())

        composeTestRule.onNodeWithTag(HERO).assertIsDisplayed()
        composeTestRule.onNodeWithTag(DETAILS_ANCHOR).assertDoesNotExist()
    }

    @Test
    fun `groups that all hold blank values register no section`() {
        setPage(listOf(TvInfoGroup(label = "Director", value = "  "), TvInfoGroup(label = "Network", value = "")))

        composeTestRule.onNodeWithTag(HERO).assertIsDisplayed()
        composeTestRule.onNodeWithTag(DETAILS_ANCHOR).assertDoesNotExist()
    }

    private fun setPage(groups: List<TvInfoGroup>) {
        composeTestRule.setContent {
            BingeTvTheme(reduceMotion = true) {
                TvDetailPage {
                    hero { Box(Modifier.testTag(HERO).size(120.dp).focusable()) }
                    detailsSection(header = HEADER, groups = groups)
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}

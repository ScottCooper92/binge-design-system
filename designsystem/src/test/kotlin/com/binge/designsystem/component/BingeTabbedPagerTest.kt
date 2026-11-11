package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val PAGE_TAG = "tabbed-page"

/**
 * [BingeTabbedPager]: a tap and a swipe move the same selection, and no tab's label is cut at any font size.
 *
 * The crop check compares each tab's laid-out width with its label's measured width, at 1x, 1.3x and 2x, on the sample's
 * five tabs: the row scrolls, so a tab keeps its own width however long the row is. It reads layout size and never
 * `hasVisualOverflow`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = android.app.Application::class)
class BingeTabbedPagerTest {
    @get:Rule
    val composeTestRule = createTakeDownComposeRule()

    private val tabs = listOf("All (42)", "Pending (4)", "Downloading (5)", "Available (28)", "Declined (2)")

    @Test
    fun `a settled swipe forward then back reports both selections`() {
        val reported = mutableListOf<Int>()
        composeTestRule.setContent {
            BingeExpressiveTheme {
                var selected by remember { mutableStateOf(0) }
                BingeTabbedPager(
                    tabs = listOf("Movies", "TV"),
                    selectedIndex = selected,
                    onSelectedIndexChange = {
                        selected = it
                        reported += it
                    },
                ) { _, page ->
                    Text(text = "page $page", modifier = Modifier.fillMaxSize().testTag(PAGE_TAG))
                }
            }
        }
        // Only the pager has a page as a descendant, which tells it from the scrollable tab row.
        val pager = composeTestRule.onNode(hasScrollToIndexAction() and hasAnyDescendant(hasTestTag(PAGE_TAG)))

        pager.performScrollToIndex(1)
        composeTestRule.waitForIdle()
        pager.performScrollToIndex(0)
        composeTestRule.waitForIdle()

        assertEquals(listOf(1, 0), reported)
    }

    @Test
    fun `tapping a tab selects it and shows its page`() {
        composeTestRule.setContent {
            BingeExpressiveTheme {
                var selected by remember { mutableStateOf(0) }
                BingeTabbedPager(
                    tabs = listOf("Movies", "TV"),
                    selectedIndex = selected,
                    onSelectedIndexChange = { selected = it },
                ) { _, page ->
                    Text(text = "page $page")
                }
            }
        }
        composeTestRule.onNodeWithText("page 0").assertExists()

        composeTestRule.onNodeWithText("TV").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("page 1").assertExists()
        composeTestRule.onNodeWithText("TV").assertIsSelected()
    }

    private fun assertNoTabLabelIsCut(fontScale: Float) {
        val measured = mutableMapOf<String, Int>()
        composeTestRule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale)) {
                BingeExpressiveTheme {
                    val measurer = rememberTextMeasurer()
                    val style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                    tabs.forEach { measured[it] = measurer.measure(it, style, softWrap = false, maxLines = 1).size.width }
                    BingeTabbedPager(tabs = tabs, selectedIndex = 0, onSelectedIndexChange = {}) { _: PaddingValues, page: Int ->
                        Text(text = "page $page")
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        tabs.forEach { label ->
            val laidOut = composeTestRule
                .onNodeWithText(label)
                .fetchSemanticsNode()
                .layoutInfo.width
            assertTrue(
                "$label is laid out $laidOut px wide, its text needs ${measured.getValue(label)}",
                laidOut >= measured.getValue(label) - 1,
            )
        }
    }

    @Test
    fun `no tab label is cut at 1x font`() = assertNoTabLabelIsCut(1f)

    @Test
    fun `no tab label is cut at 1_3x font`() = assertNoTabLabelIsCut(1.3f)

    @Test
    fun `no tab label is cut at 2x font`() = assertNoTabLabelIsCut(2f)
}

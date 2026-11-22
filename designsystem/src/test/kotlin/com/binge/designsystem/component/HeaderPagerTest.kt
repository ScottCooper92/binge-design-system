package com.binge.designsystem.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The pager behind the chip and tab pagers: its selector row follows the page that is shown, and its header and pages are both drawn. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class HeaderPagerTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val reported = mutableListOf<Int>()

    private fun show(selected: Int = 0) =
        rule.setContent {
            TestTheme {
                HeaderPager(
                    pageCount = 3,
                    selectedIndex = selected,
                    onSelectedIndexChange = { reported += it },
                    modifier = Modifier.fillMaxSize(),
                    header = { Text("header") },
                    headerBackground = Color.Transparent,
                    scrimFraction = null,
                    pageScrolled = { false },
                    scrimColor = Color.Black,
                    beyondViewportPageCount = 0,
                    selector = { shown -> Text("shown $shown") },
                ) { _, page -> Text("page $page") }
            }
        }

    @Test
    fun `the caller's header, the selector and the page are all drawn, the selector on the selected page`() {
        show(selected = 1)
        rule.waitForIdle()

        rule.onNodeWithText("header").assertIsDisplayed()
        rule.onNodeWithText("shown 1").assertIsDisplayed()
        rule.onNodeWithText("page 1").assertIsDisplayed()
    }

    @Test
    fun `a swipe the caller does not take still moves the selector to the page that is shown, and is reported`() {
        show(selected = 0)
        rule.waitForIdle()

        rule.onNode(hasScrollAction()).performTouchInput { swipeLeft() }
        rule.waitForIdle()

        // The caller keeps selecting page 0, so only the shown page can account for the selector reading 1.
        rule.onNodeWithText("shown 1").assertIsDisplayed()
        assertEquals(listOf(1), reported)
    }
}

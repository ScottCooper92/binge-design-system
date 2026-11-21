package com.binge.designsystem.component

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [BingeTabbedPager]'s header is transparent and the scrim behind it follows the page that is shown: 0 with that page at its top, above 0 once it is
 * scrolled, and up straight away after a move to a page that is already scrolled. A scrim fraction the caller passes wins.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeTabbedPagerScrimTest {
    @get:Rule
    val composeTestRule = createTakeDownComposeRule()

    private val scrims = mutableListOf<Float>()
    private val states = mutableListOf<androidx.compose.foundation.lazy.LazyListState>()
    private lateinit var scope: CoroutineScope

    private fun show(scrimFraction: Float? = null) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalPagerScrimProbe provides { scrims += it }) {
                BingeExpressiveTheme {
                    var selected by remember { mutableStateOf(0) }
                    val listStates = List(2) { rememberLazyListState() }
                    scope = rememberCoroutineScope()
                    states.clear()
                    states += listStates
                    BingeTabbedPager(
                        tabs = listOf("Cast", "Crew"),
                        selectedIndex = selected,
                        onSelectedIndexChange = { selected = it },
                        scrimFraction = scrimFraction,
                        pageScrolled = { listStates[it].canScrollBackward },
                    ) { padding, page ->
                        LazyColumn(state = listStates[page], contentPadding = padding) {
                            items((0 until ROWS).toList()) { Text("row $page-$it") }
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun scrim(): Float {
        composeTestRule.waitForIdle()
        return scrims.last()
    }

    private fun scrollTo(page: Int, index: Int) {
        composeTestRule.runOnIdle { scope.launch { states[page].scrollToItem(index) } }
        composeTestRule.waitForIdle()
    }

    private fun scroll(page: Int) {
        scrollTo(page, SCROLLED_TO)
    }

    @Test
    fun `the scrim is 0 with the shown page at its top`() {
        show()

        assertEquals(0f, scrim(), 0f)
    }

    @Test
    fun `the scrim is up once the shown page is scrolled, and back to 0 at the top`() {
        show()

        scroll(page = 0)
        assertTrue("the scrim is ${scrim()} with the page scrolled", scrim() > 0f)

        scrollTo(0, 0)
        assertEquals(0f, scrim(), 0f)
    }

    @Test
    fun `after a move to a page that is already scrolled the scrim is up, and back down on the way back`() {
        show()
        scroll(page = 1)
        assertEquals("page 0 is at its top", 0f, scrim(), 0f)

        composeTestRule.onNodeWithText("Crew").performClick()
        assertTrue("the scrim is ${scrim()} on the scrolled page", scrim() > 0f)

        composeTestRule.onNodeWithText("Cast").performClick()
        assertEquals(0f, scrim(), 0f)
    }

    @Test
    fun `a scrim fraction the caller passes is used as it is`() {
        show(scrimFraction = PASSED)

        assertEquals(PASSED, scrim(), 0f)
    }

    private companion object {
        const val ROWS = 60
        const val SCROLLED_TO = 20
        const val PASSED = 0.4f
    }
}

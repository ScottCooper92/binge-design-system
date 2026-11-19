package com.binge.designsystem.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.InMemoryStateRestorationTester
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The selection a caller owns drives [BingeFilterChipPager]; the pager reports a swipe back, and nothing else. Each case here is a way the
 * pager's own page could overwrite the caller's selection: a row composed on a later page, one restored after a recreate, and a selection
 * made while the first scroll is still pending.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeFilterChipPagerSelectionTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    /** The caller's selection, held outside the composition so it survives a recreate the way a view model's does. */
    private var selected by mutableIntStateOf(0)
    private var items by mutableStateOf(listOf("All", "Pending", "Done").map { FilterChipItem(it) })
    private val reports = mutableListOf<Int>()

    private val content: @androidx.compose.runtime.Composable () -> Unit = {
        BingeExpressiveTheme {
            BingeFilterChipPager(
                items = items,
                selectedIndex = selected,
                onSelectedIndexChange = {
                    reports += it
                    selected = it
                },
            ) { _, page -> Text(text = "page $page", modifier = Modifier.fillMaxSize()) }
        }
    }

    @Test
    fun `a row composed on a later page keeps that selection`() {
        selected = 2
        rule.setContent(content)
        rule.waitForIdle()

        assertEquals("the caller's selection was overwritten: $reports", 2, selected)
        assertEquals(emptyList<Int>(), reports)
    }

    @Test
    fun `a chip tap sticks after a recreate`() {
        val tester = InMemoryStateRestorationTester(rule)
        tester.setContent(content)
        rule.waitForIdle()
        rule.onNodeWithText("Pending").performClick()
        rule.waitForIdle()
        assertEquals(1, selected)

        tester.emulateSavedInstanceStateRestore()
        rule.waitForIdle()
        assertEquals("the restore overwrote the selection: $reports", 1, selected)

        rule.onNodeWithText("Done").performClick()
        rule.waitForIdle()
        assertEquals("a tap after the restore did not stick: $reports", 2, selected)
        rule.onNodeWithText("page 2").assertExists()
    }

    @Test
    fun `a selection that moved while the page state was saved wins after the restore`() {
        val tester = InMemoryStateRestorationTester(rule)
        tester.setContent(content)
        rule.waitForIdle()
        rule.onNodeWithText("Pending").performClick()
        rule.waitForIdle()

        // The caller's selection changes while the content is down, as a view model's can: the pager's saved page is now stale.
        selected = 2
        tester.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        assertEquals("the restored pager page overwrote the caller's selection: $reports", 2, selected)
        rule.onNodeWithText("page 2").assertExists()
    }

    @Test
    fun `a selection made right after composition is not overwritten by the first scroll`() {
        rule.setContent(content)
        selected = 1
        rule.waitForIdle()

        assertEquals("the pager overwrote the selection: $reports", 1, selected)
        rule.onNodeWithText("page 1").assertExists()
    }

    @Test
    fun `a selection made before the items arrive survives their arrival`() {
        items = emptyList()
        selected = 2
        rule.setContent(content)
        rule.waitForIdle()

        items = listOf("All", "Pending", "Done").map { FilterChipItem(it) }
        rule.waitForIdle()

        assertEquals("the empty pager overwrote the selection: $reports", 2, selected)
        rule.onNodeWithText("page 2").assertExists()
    }
}

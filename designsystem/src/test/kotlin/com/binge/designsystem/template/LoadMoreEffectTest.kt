package com.binge.designsystem.template

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The one rule behind the paged grid, the search results and the credit rows: when a lazy list asks for its next page. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class LoadMoreEffectTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var asks = 0
    private var lastVisible by mutableIntStateOf(0)
    private var total by mutableIntStateOf(20)
    private var itemCount by mutableIntStateOf(20)
    private var hasMore by mutableStateOf(true)
    private var appending by mutableStateOf(false)
    private var appendFailed by mutableStateOf(false)

    private fun show(ahead: Int = 3) =
        rule.setContent {
            LoadMoreEffect(
                key = "list",
                itemCount = itemCount,
                hasMore = hasMore,
                appending = appending,
                appendFailed = appendFailed,
                ahead = ahead,
                lastVisibleIndex = { lastVisible },
                totalCount = { total },
                onLoadMore = { asks++ },
            )
        }

    private fun settle() = rule.waitForIdle()

    @Test
    fun `it asks once when the last visible item comes within the distance of the end`() {
        show(ahead = 3)
        settle()
        assertEquals("nothing is near the end yet", 0, asks)

        lastVisible = 16
        settle()
        assertEquals("one short of the distance still does not ask", 0, asks)

        lastVisible = 17
        settle()
        assertEquals(1, asks)

        lastVisible = 18
        settle()
        assertEquals("still near the end is still the one ask", 1, asks)
    }

    @Test
    fun `a distance of one asks later than a distance of three`() {
        show(ahead = 1)
        lastVisible = 17
        settle()
        assertEquals(0, asks)

        lastVisible = 19
        settle()
        assertEquals(1, asks)
    }

    @Test
    fun `it does not ask while a page is in flight, after one failed, or when there is no more`() {
        lastVisible = 19
        appending = true
        show()
        settle()
        assertEquals("a page is in flight", 0, asks)

        appending = false
        appendFailed = true
        settle()
        assertEquals("the last page failed, and Retry asks then", 0, asks)

        appendFailed = false
        hasMore = false
        settle()
        assertEquals("the app says there is no more", 0, asks)
    }

    @Test
    fun `it asks again when a page that added nothing ends and there is still more`() {
        lastVisible = 19
        appending = true
        show()
        settle()
        assertEquals(0, asks)

        appending = false
        settle()
        assertEquals("the page ended and the list is still near its end", 1, asks)
    }

    @Test
    fun `an ask is not made before anything has been laid out`() {
        total = 0
        lastVisible = 0
        show()
        settle()

        assertEquals(0, asks)
    }
}

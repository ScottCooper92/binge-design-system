package com.binge.designsystem.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LIST = "list"
private const val ROWS = 30

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingePullToRefreshTest {
    @get:Rule
    val rule = createComposeRule()

    private var refreshes = 0

    private fun setList() {
        rule.setContent {
            BingePullToRefresh(isRefreshing = false, onRefresh = { refreshes++ }) {
                LazyColumn(Modifier.fillMaxSize().testTag(LIST)) { items(ROWS) { Text("Row $it") } }
            }
        }
    }

    @Test
    fun `a pull past the threshold refreshes`() {
        setList()

        rule.onNodeWithTag(LIST).performTouchInput { swipeDown(startY = top, endY = bottom) }
        rule.waitForIdle()

        assertEquals(1, refreshes)
    }

    @Test
    fun `a short pull does not`() {
        setList()

        rule.onNodeWithTag(LIST).performTouchInput { swipeDown(startY = top, endY = top + height / 20f) }
        rule.waitForIdle()

        assertEquals(0, refreshes)
    }
}

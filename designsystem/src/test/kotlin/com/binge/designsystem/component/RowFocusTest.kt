package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class RowFocusTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(initial: String?) {
        rule.setContent {
            var key by remember { mutableStateOf(initial) }
            val state = rememberLazyListState()
            ProvideRowFocus(key = key, listState = state) {
                LazyColumn(state = state, modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    items((0 until GROUPS).toList()) { group ->
                        ItemGroup(
                            title = "Group $group",
                            rows = List(
                                ROWS,
                            ) { row -> ListItem(icon = Icons.Filled.Settings, label = "Row $group.$row", key = "r-$group-$row") },
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `a row far down a lazy list is scrolled to and shown`() {
        show("r-30-1")
        rule.waitForIdle()
        rule.onNodeWithText("Row 30.1").assertIsDisplayed()
    }

    @Test
    fun `a row in the first group needs no scrolling`() {
        show("r-0-2")
        rule.waitForIdle()
        rule.onNodeWithText("Row 0.2").assertIsDisplayed()
    }

    @Test
    fun `a key no row has leaves the list at the top`() {
        show("nothing-here")
        rule.waitForIdle()
        rule.onNodeWithText("Row 0.0").assertIsDisplayed()
    }

    @Test
    fun `no key does nothing`() {
        show(null)
        rule.waitForIdle()
        rule.onNodeWithText("Row 0.0").assertIsDisplayed()
    }

    @Test
    fun `a row far down a plain scrolling column is scrolled to and shown`() {
        rule.setContent {
            ProvideRowFocus(key = "r-30-1") {
                Column(modifier = Modifier.fillMaxWidth().height(300.dp).verticalScroll(rememberScrollState())) {
                    repeat(GROUPS) { group ->
                        ItemGroup(
                            title = "Group $group",
                            rows = List(
                                ROWS,
                            ) { row -> ListItem(icon = Icons.Filled.Settings, label = "Row $group.$row", key = "r-$group-$row") },
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
        rule.onNodeWithText("Row 30.1").assertIsDisplayed()
    }

    @Test
    fun `a row that appears after the screen loads is still scrolled to`() {
        // The wait for the row runs on the test clock: hold it, load the screen a moment in, then let it run.
        rule.mainClock.autoAdvance = false
        var loaded by mutableStateOf(false)
        rule.setContent {
            ProvideRowFocus(key = "r-30-1") {
                Column(modifier = Modifier.fillMaxWidth().height(300.dp).verticalScroll(rememberScrollState())) {
                    if (loaded) {
                        repeat(GROUPS) { group ->
                            ItemGroup(
                                title = "Group $group",
                                rows = List(
                                    ROWS,
                                ) { row -> ListItem(icon = Icons.Filled.Settings, label = "Row $group.$row", key = "r-$group-$row") },
                            )
                        }
                    }
                }
            }
        }
        rule.mainClock.advanceTimeBy(200)
        loaded = true
        rule.mainClock.advanceTimeBy(500)
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        rule.onNodeWithText("Row 30.1").assertIsDisplayed()
    }

    private companion object {
        const val GROUPS = 40
        const val ROWS = 3
    }
}

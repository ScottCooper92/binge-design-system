package com.binge.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TITLE_TAG = "title"
private const val LIST_TAG = "list"

/**
 * The title collapsing under a real scroll (#394): [CollapsingTitleState.nestedScrollConnection] on an ancestor of a
 * list and [collapsingTitle] on the title, driven by touch swipes. The arithmetic is pinned in
 * [CollapsingTitleStateTest]; this checks the wiring that feeds it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class CollapsingTitleScrollTest {
    @get:Rule
    val rule = createComposeRule()

    private lateinit var state: CollapsingTitleState

    private fun show() {
        rule.setContent {
            state = remember { CollapsingTitleState() }
            Column(Modifier.size(width = 200.dp, height = 400.dp).nestedScroll(remember { state.nestedScrollConnection() })) {
                Box(Modifier.testTag(TITLE_TAG).collapsingTitle(state)) {
                    Box(Modifier.fillMaxWidth().height(TITLE_HEIGHT)) { Text("Title") }
                }
                LazyColumn(Modifier.testTag(LIST_TAG)) {
                    items(ROWS) { Box(Modifier.fillMaxWidth().height(ROW_HEIGHT)) { Text("Row $it") } }
                }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `scrolling the list down collapses the title away`() {
        show()
        rule.onNodeWithTag(TITLE_TAG).assertHeightIsEqualTo(TITLE_HEIGHT)

        rule.onNodeWithTag(LIST_TAG).performTouchInput { swipeUp() }
        rule.waitForIdle()

        assertEquals(state.heightPx.toFloat(), state.collapsedPx, 0f)
        rule.onNodeWithTag(TITLE_TAG).assertHeightIsEqualTo(0.dp)
    }

    @Test
    fun `scrolling back to the top opens the title again`() {
        show()
        rule.onNodeWithTag(LIST_TAG).performTouchInput { swipeUp() }
        rule.waitForIdle()
        assertTrue(state.collapsedPx > 0f)

        rule.onNodeWithTag(LIST_TAG).performTouchInput { swipeDown() }
        rule.onNodeWithTag(LIST_TAG).performTouchInput { swipeDown() }
        rule.waitForIdle()

        assertEquals(0f, state.collapsedPx, 0f)
        rule.onNodeWithTag(TITLE_TAG).assertHeightIsEqualTo(TITLE_HEIGHT)
    }

    private companion object {
        val TITLE_HEIGHT = 80.dp
        val ROW_HEIGHT = 60.dp
        const val ROWS = 60
    }
}

package com.binge.designsystem.template

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LIST = "list"
private const val ROWS = 60

/**
 * A collapsing bar over a list that starts scrolled arrives collapsed, and one over a list at the top arrives expanded. The
 * collapse is a one-off: a user who scrolls back up gets the bar back, and it is not collapsed again under them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeScreenScaffoldMidListTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private lateinit var behavior: TopAppBarScrollBehavior

    private fun show(firstVisibleItem: Int) {
        rule.setContent {
            TestTheme {
                behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                val listState = rememberLazyListState(initialFirstVisibleItemIndex = firstVisibleItem)
                Screen(listState)
            }
        }
        rule.waitForIdle()
    }

    @Composable
    private fun Screen(listState: androidx.compose.foundation.lazy.LazyListState) {
        BingeScreenScaffold(title = "Title", scrollBehavior = behavior, listState = listState) { padding ->
            LazyColumn(
                state = listState,
                contentPadding = padding,
                modifier = androidx.compose.ui.Modifier
                    .testTag(LIST),
            ) {
                items((0 until ROWS).toList()) { Text("Row $it") }
            }
        }
    }

    @Test
    fun `a list opened part way down starts under a collapsed bar`() {
        show(firstVisibleItem = 5)

        assertEquals(behavior.state.heightOffsetLimit, behavior.state.heightOffset, 0.5f)
        assertTrue("the bar has a height to collapse", behavior.state.heightOffsetLimit < 0f)
    }

    @Test
    fun `a list opened at the top starts under an expanded bar`() {
        show(firstVisibleItem = 0)

        assertEquals(0f, behavior.state.heightOffset, 0.5f)
    }

    @Test
    fun `scrolling back to the top expands the bar and it is not collapsed again`() {
        show(firstVisibleItem = 5)

        rule.onNodeWithTag(LIST).performTouchInput { repeat(6) { swipeDown() } }
        rule.waitForIdle()

        assertTrue("the user's scroll brought the bar back", behavior.state.heightOffset > behavior.state.heightOffsetLimit + 1f)
    }
}

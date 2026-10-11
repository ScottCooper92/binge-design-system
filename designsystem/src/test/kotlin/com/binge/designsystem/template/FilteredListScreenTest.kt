package com.binge.designsystem.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROWS = 60
private const val LIST_TAG = "rows"

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w400dp-h800dp")
class FilteredListScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a hoisted scroll behaviour is driven by the page's scroll`() {
        lateinit var behavior: TopAppBarScrollBehavior
        rule.setContent {
            behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            TestTheme {
                FilteredListScreen(
                    title = "Requests",
                    filters = listOf(FilterChipItem("All", 3), FilterChipItem("Pending", 1)),
                    selectedFilter = 0,
                    onFilterChange = {},
                    bar = ScreenBar.Collapsing,
                    scrollBehavior = behavior,
                    beyondViewportPageCount = 0,
                ) { _, padding ->
                    LazyColumn(modifier = Modifier.fillMaxSize().testTag(LIST_TAG), contentPadding = padding) {
                        items(ROWS) { Text("Row $it") }
                    }
                }
            }
        }
        rule.waitForIdle()
        assertTrue(behavior.state.collapsedFraction == 0f)

        rule.onNodeWithTag(LIST_TAG).performTouchInput { swipeUp() }
        rule.waitForIdle()

        assertTrue("expected the bar to collapse, was ${behavior.state.collapsedFraction}", behavior.state.collapsedFraction > 0f)
    }
}

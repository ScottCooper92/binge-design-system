package com.binge.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The `edgePadding` [BingeTabbedPager] is given reaches its [BingeTabRow]: the first tab moves by exactly the padding passed. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeTabbedPagerEdgePaddingTest {
    @get:Rule
    val composeTestRule = createTakeDownComposeRule()

    @Test
    fun `a passed edge padding moves the first tab by that much`() {
        var padding by mutableStateOf(0.dp)
        composeTestRule.setContent {
            BingeExpressiveTheme {
                BingeTabbedPager(
                    tabs = listOf("Cast", "Crew"),
                    selectedIndex = 0,
                    onSelectedIndexChange = {},
                    edgePadding = padding,
                ) { _, page -> Text("page $page") }
            }
        }
        composeTestRule.waitForIdle()
        val flush = firstTabLeft()
        composeTestRule.runOnIdle { padding = PADDING }
        composeTestRule.waitForIdle()
        assertEquals(PADDING.value, (firstTabLeft() - flush).value, 0.5f)
    }

    private fun firstTabLeft(): Dp = composeTestRule.onNodeWithText("Cast").getUnclippedBoundsInRoot().left

    private companion object {
        val PADDING = 48.dp
    }
}

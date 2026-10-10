package com.binge.designsystem.component

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** The overview's toggle, which frames show only seeded (#394): it flips, and reports the expand alone. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ExpandableOverviewTest {
    @get:Rule
    val rule = createComposeRule()

    private val more = RuntimeEnvironment.getApplication().getString(R.string.detail_show_more)
    private val less = RuntimeEnvironment.getApplication().getString(R.string.detail_show_less)

    @Test
    fun `the toggle flips between more and less, and reports only the expand`() {
        var expands = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ExpandableOverview(text = "An overview.", initiallyOverflowing = true, onExpand = { expands++ })
            }
        }

        rule.onNodeWithText(more).performClick()
        rule.onNodeWithText(less).performClick()

        rule.onNodeWithText(more).assertExists()
        assertEquals(1, expands)
    }

    @Test
    fun `text that fits offers no toggle`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) { ExpandableOverview(text = "Short.") }
        }

        rule.onNodeWithText(more).assertDoesNotExist()
    }
}

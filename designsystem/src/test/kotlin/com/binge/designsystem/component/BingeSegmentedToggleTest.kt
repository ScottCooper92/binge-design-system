package com.binge.designsystem.component

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isNotSelected
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSegmentedToggleTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(
        selected: Int = 0,
        enabled: Boolean = true,
        onSelect: (Int) -> Unit = {},
    ) = rule.setContent {
        TestTheme {
            BingeSegmentedToggle(
                listOf("OR", "AND"),
                selected,
                onSelect,
                contentDescription = "Match any or all",
                enabled = enabled,
            )
        }
    }

    @Test
    fun `the picked segment is selected and the other is not`() {
        show(selected = 1)
        rule.onNodeWithText("AND").assertIsSelected()
        rule.onNodeWithText("OR").assertIsNotSelectedForTest()
    }

    @Test
    fun `a tap on a segment reports its index`() {
        val picked = mutableListOf<Int>()
        show(onSelect = { picked += it })
        rule.onNodeWithText("AND").performClick()
        assertEquals(listOf(1), picked)
    }

    @Test
    fun `the group carries the caller's description`() {
        show()
        rule.onNode(hasContentDescription("Match any or all")).assertExists()
    }

    @Test
    fun `a disabled toggle ignores taps and says so to a screen reader`() {
        val picked = mutableListOf<Int>()
        show(enabled = false, onSelect = { picked += it })
        rule.onNodeWithText("AND").assertIsNotEnabled()
        rule.onNodeWithText("AND").performClick()
        assertEquals(emptyList<Int>(), picked)
    }

    @Test
    fun `an enabled toggle is enabled`() {
        show()
        rule.onNodeWithText("OR").assertIsEnabled()
    }

    @Test
    fun `labels are not clipped`() {
        show()
        rule.assertNoTextClipped()
    }
}

private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertIsNotSelectedForTest() = assert(isSelectable() and isNotSelected())

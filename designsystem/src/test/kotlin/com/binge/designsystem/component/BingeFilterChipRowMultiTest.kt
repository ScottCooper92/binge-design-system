package com.binge.designsystem.component

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The multi-select [BingeFilterChipRow] shows every chip in the set as selected and reports a tap as a toggle of that index. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeFilterChipRowMultiTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val items = listOf(FilterChipItem("Action"), FilterChipItem("Comedy"), FilterChipItem("Drama"))

    @Test
    fun `every chip in the set reads as selected and the others do not`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) { BingeFilterChipRow(items, selected = setOf(0, 2), onToggle = {}) }
        }

        rule.onNodeWithText("Action").assertIsSelected()
        rule.onNodeWithText("Drama").assertIsSelected()
        rule.onNodeWithText("Comedy").assertIsNotSelected()
    }

    @Test
    fun `a tap reports the index of the chip, selected or not`() {
        val toggled = mutableListOf<Int>()
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) { BingeFilterChipRow(items, selected = setOf(0), onToggle = { toggled += it }) }
        }

        rule.onNodeWithText("Action").performClick()
        rule.onNodeWithText("Drama").performClick()

        assertEquals(listOf(0, 2), toggled)
    }
}

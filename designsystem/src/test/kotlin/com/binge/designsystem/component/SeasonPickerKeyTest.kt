package com.binge.designsystem.component

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

/** A season can be identified by a text key as well as by its number: the key defaults to the number, and a tap hands both back. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SeasonPickerKeyTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `a season's key defaults to its number`() {
        assertEquals("3", SeasonRow(3, "Season 3", "x").key)
        assertEquals("id-9", SeasonRow(0, "Specials", "x", key = "id-9").key)
    }

    @Test
    fun `a tap hands back the number and the key`() {
        var number: Int? = null
        var key: String? = null
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                SeasonPickerContent(
                    rows = listOf(SeasonRow(0, "First", "a", key = "id-a"), SeasonRow(0, "Second", "b", key = "id-b")),
                    selected = 0,
                    onSelect = { number = it },
                    selectedKey = "id-a",
                    onSelectKey = { key = it },
                )
            }
        }
        rule.waitForIdle()

        rule.onNodeWithText("Second").performClick()

        assertEquals(0, number)
        assertEquals("id-b", key)
    }
}

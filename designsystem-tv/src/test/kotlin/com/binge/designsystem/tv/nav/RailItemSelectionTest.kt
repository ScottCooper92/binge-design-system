package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val FILMS = "Films"
private const val SHOWS = "Shows"

/**
 * A rail item's rule for when focus becomes a selection (#395): focus commits a change of selection and never a
 * restatement, so focus parked on the current item cannot drop the route a drill-down asked for. OK always selects.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class RailItemSelectionTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private val selections = mutableListOf<Any>()

    @Test
    fun `focus on the selected item selects nothing`() {
        setRail(selectedKey = FILMS)

        composeTestRule.onNodeWithText(FILMS).requestFocus()
        composeTestRule.waitForIdle()

        assertEquals(emptyList<Any>(), selections)
    }

    @Test
    fun `focus on another item selects it once`() {
        setRail(selectedKey = FILMS)

        composeTestRule.onNodeWithText(SHOWS).requestFocus()
        composeTestRule.waitForIdle()

        assertEquals(listOf<Any>(SHOWS), selections)
    }

    /** OK is an explicit ask for that destination's root, a way out of a drill-down, so it is not guarded. */
    @Test
    fun `ok on the selected item selects it`() {
        setRail(selectedKey = FILMS)
        composeTestRule.onNodeWithText(FILMS).requestFocus()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(FILMS).performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()

        assertEquals(listOf<Any>(FILMS), selections)
    }

    private fun setRail(selectedKey: Any) {
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    RailItem(TvNavRailItem(FILMS, FILMS, Icons.Filled.Movie), selectedKey, expanded = true, onSelect = { selections += it })
                    RailItem(TvNavRailItem(SHOWS, SHOWS, Icons.Filled.Tv), selectedKey, expanded = true, onSelect = { selections += it })
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}

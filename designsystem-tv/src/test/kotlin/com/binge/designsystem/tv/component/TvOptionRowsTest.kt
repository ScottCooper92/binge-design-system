package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TITLE = "Root folder"
private const val FIRST = "/data/movies"
private const val SECOND = "/data/movies-4k"
private const val AGREE = "Allow plain HTTP"
private const val GROUP = "group"

/**
 * The stacked option group commits on OK and never on focus, and never re-commits the option already chosen. The
 * checkbox row flips on OK, and a disabled one keeps the remote's place while ignoring OK.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvOptionRowsTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private val committed = mutableListOf<String>()
    private val checks = mutableListOf<Boolean>()

    @Test
    fun `moving down the group commits nothing`() {
        setGroup()
        option(FIRST).requestFocus()
        composeTestRule.waitForIdle()
        option(FIRST).assertIsFocused()

        press(Key.DirectionDown)

        option(SECOND).assertIsFocused()
        assertEquals(emptyList<String>(), committed)
    }

    @Test
    fun `OK on another option commits it`() {
        setGroup()
        option(SECOND).requestFocus()
        composeTestRule.waitForIdle()

        press(Key.DirectionCenter)

        assertEquals(listOf(SECOND), committed)
    }

    @Test
    fun `OK on the option already chosen commits nothing`() {
        setGroup()
        option(FIRST).requestFocus()
        composeTestRule.waitForIdle()

        press(Key.DirectionCenter)

        assertEquals(emptyList<String>(), committed)
    }

    @Test
    fun `a group with nothing to offer draws nothing`() {
        composeTestRule.setContent {
            BingeTvTheme {
                Box(Modifier.testTag(GROUP)) {
                    TvOptionGroup(title = TITLE, choices = emptyList<Pair<String, String>>(), selected = null, onSelect = {})
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(TITLE).assertDoesNotExist()
        composeTestRule.onAllNodes(isFocusable()).assertCountEquals(0)
        composeTestRule.onNodeWithTag(GROUP).assertWidthIsEqualTo(0.dp).assertHeightIsEqualTo(0.dp)
    }

    @Test
    fun `OK on the checkbox row flips it`() {
        setCheckbox(enabled = true)
        checkbox().assertIsFocused()

        press(Key.DirectionCenter)

        assertEquals(listOf(true), checks)
    }

    @Test
    fun `a disabled checkbox row keeps focus and ignores OK`() {
        setCheckbox(enabled = false)
        checkbox().assertIsFocused()

        press(Key.DirectionCenter)

        checkbox().assertIsFocused()
        assertEquals(emptyList<Boolean>(), checks)
    }

    private fun setGroup() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvOptionGroup(
                    title = TITLE,
                    choices = listOf(FIRST to FIRST, SECOND to SECOND),
                    selected = FIRST,
                    onSelect = { committed += it },
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun setCheckbox(enabled: Boolean) {
        composeTestRule.setContent {
            BingeTvTheme { TvCheckboxRow(label = AGREE, checked = false, onCheckedChange = { checks += it }, enabled = enabled) }
        }
        composeTestRule.waitForIdle()
        checkbox().requestFocus()
        composeTestRule.waitForIdle()
    }

    private fun option(label: String) = composeTestRule.onNode(hasText(label) and isFocusable())

    private fun checkbox() = composeTestRule.onNode(hasText(AGREE) and isFocusable())

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }
}

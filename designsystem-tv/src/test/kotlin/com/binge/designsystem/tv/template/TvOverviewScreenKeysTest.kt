package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The reader is a single focus stop, so its keys are the whole interaction: the start-edge key closes it, and
 * a reader over a list claims both horizontal keys so that BACK is the only way out.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvOverviewScreenKeysTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private var dismissed = 0
    private var previous = 0
    private var next = 0

    @Test
    fun `the start-edge key closes a single-item reader`() {
        setReader(crossing = false)

        press(Key.DirectionLeft)

        assertEquals(1, dismissed)
    }

    @Test
    fun `a reader over a list steps with both horizontal keys and does not close`() {
        setReader(crossing = true)

        press(Key.DirectionLeft)
        press(Key.DirectionRight)
        press(Key.DirectionRight)

        assertEquals(1, previous)
        assertEquals(2, next)
        assertEquals(0, dismissed)
    }

    @Test
    fun `the end-edge key does nothing on a single-item reader`() {
        setReader(crossing = false)

        press(Key.DirectionRight)

        assertEquals(0, dismissed + previous + next)
    }

    private fun setReader(crossing: Boolean) {
        composeTestRule.setContent {
            BingeTvTheme {
                TvOverviewScreen(
                    title = "Title",
                    overview = "Overview",
                    backdropUrl = null,
                    onDismiss = { dismissed++ },
                    onPrevious = if (crossing) ({ previous++ }) else null,
                    onNext = if (crossing) ({ next++ }) else null,
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocusable()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }
}

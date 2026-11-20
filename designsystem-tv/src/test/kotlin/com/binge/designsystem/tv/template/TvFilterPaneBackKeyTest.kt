package com.binge.designsystem.tv.template

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createTakeDownKeyboardAndroidComposeRule
import com.binge.designsystem.tv.catalog.FilterFixture
import com.binge.designsystem.tv.catalog.LABEL_CONTENT
import com.binge.designsystem.tv.catalog.LABEL_GENRES
import com.binge.designsystem.tv.catalog.LABEL_MINIMUM
import com.binge.designsystem.tv.catalog.LABEL_RATING
import com.binge.designsystem.tv.catalog.SampleFilterPane
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val CATEGORY = LABEL_GENRES
private const val FIRST_VALUE = "Action"
private const val RATING_CATEGORY = LABEL_RATING
private const val ANY_STEP = "Any"
private const val MINIMUM_TAB = LABEL_MINIMUM
private const val FIRST_CATEGORY = LABEL_CONTENT
private const val MAX_CATEGORIES = 12
private const val STEPPED_FRAMES = 6

/**
 * **A real Back key, sent through the activity as the remote sends it, leaves a filter's values in one press.** The other Back
 * tests post a navigation event straight to the dispatcher, which skips the key reaching the focused row first (the way a focusable
 * ancestor once consumed Back on the Shield), so this one goes the long way.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvFilterPaneBackKeyTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val leanback = LeanbackRule()

    @Test
    fun `one Back key from a value pops to the category list with focus on the category`() {
        setPane()
        openCategory(CATEGORY)
        text(FIRST_VALUE).assertIsFocused()

        pressBackKey()

        composeTestRule.onAllNodesWithText(FIRST_VALUE).assertCountEquals(0)
        text(CATEGORY).assertIsFocused()
        composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
    }

    @Test
    fun `one Back key from the Rating scale pops to the category list with focus on Rating`() {
        setPane()
        openCategory(RATING_CATEGORY)
        composeTestRule.onAllNodes(isFocused() and hasText(ANY_STEP)).assertCountEquals(1)

        pressBackKey()

        composeTestRule.onAllNodesWithText(MINIMUM_TAB).assertCountEquals(0)
        text(RATING_CATEGORY).assertIsFocused()
        composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
    }

    /**
     * The test clock lays the new view out a frame after the key, so the first frame is never focused here, and what the row
     * draws on that frame (it is seeded as focused while a swap is under way) is not in the semantics. These two pin the rest.
     */
    @Test
    fun `after a Back key the category holds focus from the second frame on`() {
        setPane()
        openCategory(CATEGORY)
        text(FIRST_VALUE).assertIsFocused()

        composeTestRule.mainClock.autoAdvance = false
        pressBackKeyWithoutIdling()
        val focusedByFrame = (1..STEPPED_FRAMES).map {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.onAllNodes(isFocused() and hasText(CATEGORY)).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        assertTrue("frames in which $CATEGORY held focus: $focusedByFrame", focusedByFrame.drop(1).all { it })
    }

    @Test
    fun `after OK on a category its first value holds focus from the second frame on`() {
        setPane()
        text(FIRST_CATEGORY).requestFocus()
        composeTestRule.waitForIdle()
        repeat(MAX_CATEGORIES) {
            if (composeTestRule.onAllNodes(isFocused() and hasText(CATEGORY)).fetchSemanticsNodes().isNotEmpty()) return@repeat
            press(Key.DirectionDown)
        }

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(Key.DirectionCenter) }
        val focusedByFrame = (1..STEPPED_FRAMES).map {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.onAllNodes(isFocused() and hasText(FIRST_VALUE)).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        assertTrue("frames in which $FIRST_VALUE held focus: $focusedByFrame", focusedByFrame.drop(1).all { it })
    }

    private fun openCategory(label: String) {
        text(FIRST_CATEGORY).requestFocus()
        composeTestRule.waitForIdle()
        repeat(MAX_CATEGORIES) {
            if (composeTestRule.onAllNodes(isFocused() and hasText(label)).fetchSemanticsNodes().isNotEmpty()) {
                press(Key.DirectionCenter)
                return
            }
            press(Key.DirectionDown)
        }
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun pressBackKeyWithoutIdling() {
        val decor = composeTestRule.activity.window.decorView
        composeTestRule.runOnUiThread {
            decor.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
            decor.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
        }
    }

    private fun pressBackKey() {
        pressBackKeyWithoutIdling()
        composeTestRule.waitForIdle()
    }

    private fun text(label: String): SemanticsNodeInteraction = composeTestRule.onNodeWithText(label)

    private fun setPane() {
        composeTestRule.setContent {
            BingeTvTheme {
                var state by remember { mutableStateOf(FilterFixture()) }
                SampleFilterPane(state = state, onState = { state = it }, entry = remember { FocusRequester() }, onOpenServices = {})
            }
        }
        composeTestRule.waitForIdle()
    }
}

package com.binge.designsystem.tv.nav

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.focus.LocalTvShellArrival
import com.binge.designsystem.tv.focus.TvShellArrival
import com.binge.designsystem.tv.focus.TvShellArrivalNone
import com.binge.designsystem.tv.focus.tvShellArrivalTarget
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TARGET = "target"
private val TARGET_SIZE = 40.dp

/** The shell's arrival: a destination's first focusable takes one request when it attaches after the shell armed it. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvShellArrivalTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private val misses = mutableListOf<String>()
    private val traces = mutableListOf<String>()
    private val arrival = TvShellArrival(onMiss = { misses += it }, onTrace = { traces += it })
    private var attached by mutableStateOf(false)

    private fun setContent() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalTvShellArrival provides arrival) {
                if (attached) {
                    Box(
                        Modifier
                            .size(TARGET_SIZE)
                            .testTag(TARGET)
                            .tvShellArrivalTarget(remember { FocusRequester() })
                            .focusable(),
                    )
                }
            }
        }
    }

    @Test
    fun `a target that attaches after the arrival is armed takes focus once`() {
        setContent()
        arrival.arm()
        attached = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TARGET).assertIsFocused()
        assertEquals(emptyList<String>(), misses)
    }

    @Test
    fun `a target that attaches with nothing armed takes no focus`() {
        setContent()
        attached = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TARGET).assertIsNotFocused()
    }

    @Test
    fun `a user choice clears the arrival before the target attaches`() {
        setContent()
        arrival.arm()
        arrival.clear()
        attached = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TARGET).assertIsNotFocused()
    }

    @Test
    fun `a second arm before anyone claimed the first reports a miss`() {
        arrival.arm()
        arrival.arm()
        assertEquals(1, misses.size)
    }

    @Test
    fun `the first key does not arm`() {
        setContent()
        arrival.armOnChange("movies")
        attached = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TARGET).assertIsNotFocused()
    }

    @Test
    fun `a cold start arms on the first key when asked, so the first destination's target is claimed`() {
        setContent()
        arrival.armOnChange("movies", armFirst = true)
        attached = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TARGET).assertIsFocused()
        assertEquals(emptyList<String>(), misses)
    }

    @Test
    fun `arming the first key does not make the next unchanged key arm again`() {
        setContent()
        arrival.armOnChange("movies", armFirst = true)
        arrival.armOnChange("movies", armFirst = true)
        assertEquals(emptyList<String>(), misses)
    }

    @Test
    fun `a changed key arms`() {
        setContent()
        arrival.armOnChange("movies")
        arrival.armOnChange("tv")
        attached = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TARGET).assertIsFocused()
    }

    @Test
    fun `a quiet switch arms nothing and only the one switch`() {
        setContent()
        arrival.armOnChange("tv")
        arrival.quietNextSwitch()
        arrival.armOnChange("movies")
        attached = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TARGET).assertIsNotFocused()
        arrival.armOnChange("tv")
        assertEquals(emptyList<String>(), misses)
    }

    /** Pins today's behaviour: a key that lands after Select but before the composition that arms is a clear with nothing pending, and the arm that follows still claims. */
    @Test
    fun `a clear before the arm does not stop the arrival, which is the race a key just after a Select can hit`() {
        setContent()
        arrival.armOnChange("movies")
        arrival.clear()
        arrival.armOnChange("tv")
        attached = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TARGET).assertIsFocused()
    }

    @Test
    fun `a destination with no target settles the arrival, so the next arm is not a miss`() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalTvShellArrival provides arrival) { if (attached) TvShellArrivalNone() }
        }
        arrival.arm()
        attached = true
        composeTestRule.waitForIdle()
        arrival.arm()
        assertEquals(emptyList<String>(), misses)
        assertEquals(listOf("arm", "settled: destination has no target", "arm"), traces)
    }

    @Test
    fun `settling with nothing pending says nothing and does not stop a later arrival`() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalTvShellArrival provides arrival) { TvShellArrivalNone() }
        }
        composeTestRule.waitForIdle()
        assertEquals(emptyList<String>(), traces)
        arrival.arm()
        assertEquals(true, arrival.isPending)
    }

    @Test
    fun `the trace names the arm, both kinds of clear and the claim`() {
        setContent()
        arrival.clear()
        arrival.arm()
        arrival.clear()
        arrival.arm()
        attached = true
        composeTestRule.waitForIdle()
        assertEquals(
            listOf("clear with nothing pending", "arm", "clear while an arrival is pending", "arm", "claimed by a target"),
            traces,
        )
    }

    @Test
    fun `a target reached with no arrival pending says so`() {
        setContent()
        attached = true
        composeTestRule.waitForIdle()
        assertEquals(listOf("target reached with no arrival pending"), traces)
    }
}

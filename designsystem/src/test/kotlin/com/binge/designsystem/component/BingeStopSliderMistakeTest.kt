package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A slider with fewer than two stops throws from composition. It has its own class, on the plain rule, because it leaves nothing
 * pending on the dispatcher and the take-down rule would move where the throw surfaces; the rest of the slider's tests are
 * [BingeStopSliderTest], on the take-down rule.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeStopSliderMistakeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `fewer than two stops is a mistake`() {
        assertThrows(IllegalArgumentException::class.java) {
            composeTestRule.setContent {
                TestTheme {
                    BingeStopSlider(
                        stops = listOf(BingeStop("Only", Icons.Filled.Star)),
                        selectedIndex = 0,
                        onCommit = {},
                        description = "Licences",
                    )
                }
            }
        }
    }
}

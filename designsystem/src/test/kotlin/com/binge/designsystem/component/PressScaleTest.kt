package com.binge.designsystem.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** [pressScale], the press feedback [MediaCard] and the category tiles share: smaller while pressed, still under reduced motion. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class PressScaleTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val source = MutableInteractionSource()

    private fun show(reduceMotion: Boolean) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false, reduceMotion = reduceMotion) {
                Box(Modifier.size(SIZE.dp).pressScale(source).testTag(TAG))
            }
        }
        rule.waitForIdle()
    }

    private fun width() =
        rule
            .onNodeWithTag(TAG)
            .fetchSemanticsNode()
            .boundsInRoot.width

    private fun press(): PressInteraction.Press {
        val press = PressInteraction.Press(Offset.Zero)
        rule.runOnIdle { source.tryEmit(press) }
        rule.waitForIdle()
        return press
    }

    @Test
    fun `a pressed tile scales down and springs back`() {
        show(reduceMotion = false)
        val rest = width()

        val press = press()
        assertTrue("pressed width ${width()} should be below $rest", width() < rest)

        rule.runOnIdle { source.tryEmit(PressInteraction.Release(press)) }
        rule.waitForIdle()
        assertEquals(rest, width(), 0.5f)
    }

    @Test
    fun `a pressed tile does not move under reduced motion`() {
        show(reduceMotion = true)
        val rest = width()

        press()

        assertEquals(rest, width(), 0.01f)
    }

    private companion object {
        const val TAG = "tile"
        const val SIZE = 100
    }
}

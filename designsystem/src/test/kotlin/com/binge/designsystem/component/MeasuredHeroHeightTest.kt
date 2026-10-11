package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val Fallback = 480.dp
private val Laid = 300.dp

/** What a screen that sets the overlay bar over its own hero reads back: the fallback first, then the laid-out height. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class MeasuredHeroHeightTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `it reads the fallback until the hero is laid out, then the laid-out height`() {
        val seen = mutableListOf<Dp>()
        rule.setContent {
            TestTheme {
                val measured = rememberMeasuredHeroHeight()
                seen += measured.height(Fallback)
                Hero(Modifier.measuredHeroHeight(measured))
            }
        }
        rule.waitForIdle()

        assertEquals("first composition has no measurement", Fallback, seen.first())
        assertEquals(Laid.value, seen.last().value, 0.5f)
    }

    @Composable
    private fun Hero(modifier: Modifier) {
        Box(modifier.height(Laid))
    }
}

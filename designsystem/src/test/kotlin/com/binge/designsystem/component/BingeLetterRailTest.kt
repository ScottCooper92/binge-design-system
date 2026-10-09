package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeLetterRailTest {
    @get:Rule
    val rule = createComposeRule()

    private val picked = mutableListOf<Char>()

    @Test
    fun `tapping the same letter twice jumps both times`() {
        rule.setContent {
            var current by remember { mutableStateOf<Char?>('A') }
            Box(Modifier.height(400.dp)) {
                BingeLetterRail(
                    letters = listOf('A', 'B', 'C', 'D'),
                    onLetter = {
                        picked += it
                        current = it
                    },
                    current = current,
                )
            }
        }

        // The top quarter of the rail is A, the second is B.
        rule.onRoot().performTouchInput { click(Offset(width / 2f, height * 0.375f)) }
        rule.waitForIdle()
        // The list is scrolled back by hand; the rail now says A again.
        rule.onRoot().performTouchInput { click(Offset(width / 2f, height * 0.375f)) }
        rule.waitForIdle()

        assertEquals(listOf('B', 'B'), picked)
    }
}

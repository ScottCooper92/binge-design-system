package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A parent sized from `IntrinsicSize.Min` has to see every line the row will draw, which `FlowRow` does not report. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class WrappingRowTest {
    @get:Rule
    val rule = createComposeRule()

    private fun heightOf(count: Int, childWidth: Int): Int {
        var size = IntSize.Zero
        rule.setContent {
            Row(Modifier.width(100.dp).height(IntrinsicSize.Min).onSizeChanged { size = it }) {
                WrappingRow(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalSpacing = 4.dp,
                    verticalSpacing = 4.dp,
                ) {
                    repeat(count) { Box(Modifier.size(width = childWidth.dp, height = 10.dp)) }
                }
            }
        }
        rule.waitForIdle()
        return size.height
    }

    @Test
    fun `an intrinsic-height parent sizes to every wrapped line`() {
        // Three 60dp children in 100dp wrap to one per line: three 10dp lines and two 4dp gaps.
        assertEquals(with(rule.density) { 38.dp.roundToPx() }, heightOf(count = 3, childWidth = 60))
    }

    @Test
    fun `children that fit share a line`() {
        assertEquals(with(rule.density) { 10.dp.roundToPx() }, heightOf(count = 2, childWidth = 30))
    }
}

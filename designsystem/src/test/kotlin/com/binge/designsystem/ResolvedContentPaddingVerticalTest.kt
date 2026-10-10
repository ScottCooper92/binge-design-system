package com.binge.designsystem

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ResolvedContentPaddingVerticalTest {
    @get:Rule
    val rule = createComposeRule()

    private fun padding(read: @androidx.compose.runtime.Composable () -> PaddingValues): PaddingValues {
        lateinit var result: PaddingValues
        rule.setContent { result = read() }
        rule.waitForIdle()
        return result
    }

    private fun PaddingValues.top(): Dp = calculateTopPadding()

    private fun PaddingValues.bottom(): Dp = calculateBottomPadding()

    @Test
    fun `vertical pads the top and the bottom by the same amount`() {
        val padding = padding { resolvedContentPadding(vertical = 12.dp) }

        assertEquals(12.dp, padding.top())
        assertEquals(12.dp, padding.bottom())
    }

    @Test
    fun `vertical leaves the sides as the plain call has them`() {
        lateinit var plain: PaddingValues
        lateinit var withVertical: PaddingValues
        rule.setContent {
            plain = resolvedContentPadding()
            withVertical = resolvedContentPadding(vertical = 12.dp)
        }
        rule.waitForIdle()

        assertEquals(plain.calculateLeftPadding(LayoutDirection.Ltr), withVertical.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(plain.calculateRightPadding(LayoutDirection.Ltr), withVertical.calculateRightPadding(LayoutDirection.Ltr))
    }

    @Test
    fun `vertical adds to top and bottom rather than replacing them`() {
        val padding = padding { resolvedContentPadding(top = 4.dp, bottom = 8.dp, vertical = 10.dp) }

        assertEquals(14.dp, padding.top())
        assertEquals(18.dp, padding.bottom())
    }

    @Test
    fun `the plain call has no vertical padding`() {
        val padding = padding { resolvedContentPadding() }

        assertEquals(0.dp, padding.top())
        assertEquals(0.dp, padding.bottom())
    }
}

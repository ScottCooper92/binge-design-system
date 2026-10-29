package com.binge.designsystem.component

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A loading filled button swallows taps unless it is told its tap is not the in-flight action. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeFilledButtonLoadingTest {
    @get:Rule
    val rule = createComposeRule()

    private var taps = 0

    @Test
    fun `a loading button swallows taps`() {
        setButton(loading = true, tapsWhileLoading = false)

        rule.onNode(hasClickAction()).performClick()

        assertEquals(0, taps)
    }

    @Test
    fun `a loading button that takes taps while loading passes them on`() {
        setButton(loading = true, tapsWhileLoading = true)

        rule.onNode(hasClickAction()).performClick()

        assertEquals(1, taps)
    }

    @Test
    fun `a button that is not loading takes taps`() {
        setButton(loading = false, tapsWhileLoading = false)

        rule.onNode(hasClickAction()).performClick()

        assertEquals(1, taps)
    }

    private fun setButton(loading: Boolean, tapsWhileLoading: Boolean) {
        rule.setContent {
            BingeFilledButton(label = "Go", onClick = { taps++ }, loading = loading, tapsWhileLoading = tapsWhileLoading)
        }
    }
}

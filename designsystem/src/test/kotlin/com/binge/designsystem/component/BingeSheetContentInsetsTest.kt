package com.binge.designsystem.component

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.testing.WithWindowInsets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val Cutout = 32.dp

/** A sheet's content insets carry the window's sides, in both forms, so landscape hardware stays clear of it (#459). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w891dp-h411dp-land")
class BingeSheetContentInsetsTest {
    @get:Rule
    val rule = createComposeRule()

    private fun leftInset(edgeToEdge: Boolean): Int {
        var left = -1
        var expected = -2
        rule.setContent {
            WithWindowInsets({
                WindowInsetsCompat
                    .Builder()
                    .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(Cutout.roundToPx(), 0, 0, 0))
                    .build()
            }) {
                val density = LocalDensity.current
                left = bingeSheetContentInsets(edgeToEdge).getLeft(density, LayoutDirection.Ltr)
                expected = with(density) { Cutout.roundToPx() }
            }
        }
        rule.waitForIdle()
        assertEquals(expected, left)
        return left
    }

    @Test
    fun `the default sheet insets a side cutout`() {
        leftInset(edgeToEdge = false)
    }

    @Test
    fun `the edge-to-edge sheet insets a side cutout`() {
        leftInset(edgeToEdge = true)
    }
}

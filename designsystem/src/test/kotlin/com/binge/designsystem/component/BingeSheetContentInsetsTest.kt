package com.binge.designsystem.component

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Dp
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

/**
 * A sheet's content insets carry the part of a side inset the sheet reaches, in both forms (#459). The sheet is at most
 * 640dp wide and centred, so a 660dp window leaves it 10dp from each side and a 32dp cutout reaches 22dp into it,
 * while an 891dp window leaves it clear of the cutout altogether.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSheetContentInsetsTest {
    @get:Rule
    val rule = createComposeRule()

    private fun assertLeftInset(edgeToEdge: Boolean, expected: Dp) {
        var left = -1
        var expectedPx = -2
        rule.setContent {
            WithWindowInsets({
                WindowInsetsCompat
                    .Builder()
                    .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(Cutout.roundToPx(), 0, 0, 0))
                    .build()
            }) {
                val density = LocalDensity.current
                left = bingeSheetContentInsets(edgeToEdge).getLeft(density, LayoutDirection.Ltr)
                expectedPx = with(density) { expected.roundToPx() }
            }
        }
        rule.waitForIdle()
        assertEquals(expectedPx.toFloat(), left.toFloat(), 1f)
    }

    @Test
    @Config(qualifiers = "w660dp-h360dp-land")
    fun `the default sheet insets the part of a side cutout it reaches`() {
        assertLeftInset(edgeToEdge = false, expected = 22.dp)
    }

    @Test
    @Config(qualifiers = "w660dp-h360dp-land")
    fun `the edge-to-edge sheet insets the part of a side cutout it reaches`() {
        assertLeftInset(edgeToEdge = true, expected = 22.dp)
    }

    @Test
    @Config(qualifiers = "w891dp-h411dp-land")
    fun `a sheet clear of a side cutout gets no side inset`() {
        assertLeftInset(edgeToEdge = false, expected = 0.dp)
    }

    @Test
    fun `the side margin is half the width the sheet leaves, and never negative`() {
        assertEquals(10, sheetSideMargin(windowWidth = 660, sheetMaxWidth = 640))
        assertEquals(0, sheetSideMargin(windowWidth = 400, sheetMaxWidth = 640))
    }
}

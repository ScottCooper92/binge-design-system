package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
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

/** A sheet is laid out inside the window's safe sides, and only narrows where it would otherwise reach them (#459). */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w891dp-h411dp-land")
class BingeSheetContentInsetsTest {
    @get:Rule
    val rule = createComposeRule()

    /** The width the sheet takes in a window [windowWidth] wide with a cutout on the left, and the expected width. */
    private fun sheetWidth(windowWidth: androidx.compose.ui.unit.Dp): Pair<Int, Int> {
        var actual = -1
        var expectedWide = -2
        rule.setContent {
            WithWindowInsets({
                WindowInsetsCompat
                    .Builder()
                    .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(Cutout.roundToPx(), 0, 0, 0))
                    .build()
            }) {
                val density = LocalDensity.current
                expectedWide = with(density) { minOf(BottomSheetDefaults.SheetMaxWidth.roundToPx(), (windowWidth - Cutout).roundToPx()) }
                Box(Modifier.width(windowWidth), contentAlignment = Alignment.TopCenter) {
                    Box(
                        Modifier
                            .bingeSheetSideInsets()
                            .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                            .fillMaxWidth()
                            .onSizeChanged { actual = it.width },
                    )
                }
            }
        }
        rule.waitForIdle()
        return actual to expectedWide
    }

    @Test
    fun `a narrow window narrows the sheet to the safe width`() {
        val (actual, expected) = sheetWidth(500.dp)
        assertEquals(expected, actual)
    }

    @Test
    fun `a window wider than the cap and the inset leaves the sheet at its full width`() {
        val (actual, expected) = sheetWidth(891.dp)
        assertEquals(expected, actual)
        assertEquals(true, actual > 0)
    }

    @Test
    fun `the content insets carry no sides`() {
        var sides = -1
        rule.setContent {
            WithWindowInsets({
                WindowInsetsCompat
                    .Builder()
                    .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(Cutout.roundToPx(), 0, Cutout.roundToPx(), 0))
                    .build()
            }) {
                val density = LocalDensity.current
                val layout = androidx.compose.ui.unit.LayoutDirection.Ltr
                sides = listOf(false, true).sumOf {
                    val insets = bingeSheetContentInsets(it)
                    insets.getLeft(density, layout) + insets.getRight(density, layout)
                }
            }
        }
        rule.waitForIdle()
        assertEquals(0, sides)
    }
}

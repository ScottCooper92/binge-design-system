package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.template.LocalHeroReadingMargin
import com.binge.designsystem.template.readingInset
import com.binge.designsystem.template.readingMargin
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [TitleStatsRows] puts all of a page's stats on one row, and two to a row from a font scale of 1.5; [readingMargin] and
 * [readingInset] start a section at the margin [LocalHeroReadingMargin] gives it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w411dp-h891dp-xhdpi")
class TitleStatsRowsTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val stats = listOf("a", "b", "c", "d").map { DetailStat(Icons.Filled.Star, "v$it", "label $it") }

    private fun show(fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) { Column { TitleStatsRows(stats) } }
            }
        }
        rule.waitForIdle()
    }

    private fun top(label: String) = rule.onNodeWithText(label, useUnmergedTree = true).getBoundsInRoot().top

    @Test
    fun `four stats share one row at the normal font`() {
        show(1.0f)

        assertEquals(top("label a"), top("label d"))
    }

    @Test
    fun `four stats go two to a row from a font scale of 1 point 5`() {
        show(1.5f)

        assertEquals(top("label a"), top("label b"))
        assertTrue("the third stat is on the next row", top("label c") > top("label b"))
        assertEquals(top("label c"), top("label d"))
    }

    @Test
    fun `just under 1 point 5 the stats stay on one row`() {
        show(1.49f)

        assertEquals(top("label a"), top("label d"))
    }

    @Test
    fun `a section takes the reading margin and a rail's inset adds it to the screen's`() {
        rule.setContent {
            CompositionLocalProvider(LocalHeroReadingMargin provides 24.dp) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("section", Modifier.readingMargin())
                        Text("rail", Modifier.padding(start = readingInset()))
                    }
                }
            }
        }
        rule.waitForIdle()

        assertEquals(
            24f,
            rule
                .onNodeWithText("section")
                .getBoundsInRoot()
                .left.value,
            0.5f,
        )
        assertEquals(
            24f + 16f,
            rule
                .onNodeWithText("rail")
                .getBoundsInRoot()
                .left.value,
            0.5f,
        )
    }
}

package com.binge.designsystem.component

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.LocalPaneInnerEdge
import com.binge.designsystem.PaneEdge
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TITLE = "Dune: Part Two"
private const val TOLERANCE = 1f

/** The hero's title lines up with the body under it, which pads the edge shared with another pane less (#359). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class DetailHeroPaneEdgeTest {
    @get:Rule
    val rule = createComposeRule()

    private fun titleLeft(innerEdge: PaneEdge?): Dp {
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalPaneInnerEdge provides innerEdge) {
                    DetailHero(title = TITLE, backdropUrl = null, tagline = null, metaText = "2024", onBack = {}, showChrome = false)
                }
            }
        }
        return rule.onNodeWithText(TITLE).getBoundsInRoot().left
    }

    @Test
    fun `alone in the window, the title sits the window inset from the edge`() {
        assertEquals(16f, titleLeft(innerEdge = null).value, TOLERANCE)
    }

    @Test
    fun `in the end pane, the title sits the inner inset from the shared edge`() {
        assertEquals(8.dp.value, titleLeft(innerEdge = PaneEdge.Start).value, TOLERANCE)
    }
}

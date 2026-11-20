package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private val TAGS = listOf("Heist", "Based on a novel", "Time loop", "Dystopia", "Found family")

/** A [TagRow] keeps its labels uncut at a large font, and its tags are buttons only when a click handler is given. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h800dp")
// Native graphics, because legacy graphics measures text at a pixel a character: nothing wraps and nothing is cut.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TagRowTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(
        compact: Boolean,
        fontScale: Float = 1f,
        onTagClick: ((Int) -> Unit)? = null,
    ) {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Box(Modifier.width(200.dp)) { TagRow(tags = TAGS, compact = compact, onTagClick = onTagClick) }
                }
            }
        }
        rule.waitForIdle()
    }

    @Test fun `no tag is clipped at 2_0, default`() {
        show(compact = false, fontScale = 2f)
        assertTrue("no text node was checked", rule.assertNoTextClipped() >= TAGS.size)
    }

    @Test fun `no tag is clipped at 2_0, compact`() {
        show(compact = true, fontScale = 2f)
        assertTrue("no text node was checked", rule.assertNoTextClipped() >= TAGS.size)
    }

    @Test fun `without a handler the tags take no taps`() {
        show(compact = false)
        rule.onAllNodes(hasClickAction()).assertCountEquals(0)
    }

    @Test fun `with a handler each tag is its own button named by its label`() {
        show(compact = true, onTagClick = {})
        rule.onAllNodes(hasClickAction()).assertCountEquals(TAGS.size)
        TAGS.forEach { label ->
            val node = rule.onNodeWithText(label).assertHasClickAction().fetchSemanticsNode()
            assertEquals(Role.Button, node.config[SemanticsProperties.Role])
        }
        rule.onAllNodesWithText("Time loop").assertCountEquals(1)
    }

    @Test fun `a tap reports the tag's index`() {
        val taps = mutableListOf<Int>()
        show(compact = false, onTagClick = { taps += it })
        rule.onNodeWithText("Time loop").performClick()
        rule.onNodeWithText("Heist").performClick()
        assertEquals(listOf(2, 0), taps)
    }
}

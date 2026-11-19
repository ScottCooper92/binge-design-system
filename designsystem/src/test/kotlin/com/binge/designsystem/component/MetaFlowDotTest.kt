package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A meta item that needs the whole line (a long genre list at a large font) still leaves room for the dot after it, so
 * the next item starts a new line behind a separator rather than with none.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h8000dp")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MetaFlowDotTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(entries: List<MetaEntry>) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                Box(Modifier.width(200.dp).testTag("flow")) { MetaFlow(entries) }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `a full-width content item leaves room for its dot`() {
        show(
            listOf(
                MetaEntry.Content { Text("Acción · Crimen · Drama · Suspense · Ciencia ficción · Aventura") },
                MetaEntry.Content { Text("Next") },
            ),
        )

        val flow = rule.onNodeWithTag("flow").getBoundsInRoot()
        val first = rule.onNodeWithText("Acción", substring = true).getBoundsInRoot()
        val reserved = flow.right - first.right
        assertTrue("the item leaves $reserved for its dot", reserved >= 13.dp)
    }

    @Test
    fun `a wrapped text item draws its dot on the line the text ends on`() {
        show(
            listOf(
                MetaEntry.WrappingText("Acción · Crimen · Drama · Suspense · Ciencia ficción · Aventura", Color.Unspecified),
                MetaEntry.Content { Text("Next") },
            ),
        )

        val text = rule.onNodeWithText("Acción", substring = true).getBoundsInRoot()
        val dot = rule.onNodeWithTag(META_DOT_TAG).getBoundsInRoot()
        assertTrue("the text wraps over several lines", text.height > 40.dp)
        assertTrue("the dot sits in the text's last line, not beside its middle", dot.top >= text.bottom - text.height / 2)
        assertTrue("the dot is inside the text's own width", dot.right <= text.right + 1.dp)
    }
}

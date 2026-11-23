package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.catalog.BingeSnackbarWorstCaseSample
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The rule for a snackbar is a result in one short sentence, readable in full. `assertNoTextClipped` accepts a deliberate
 * ellipsis, so this reads the layout itself: the result sentence is at most two lines at 1.3x font and nothing in it is
 * cut. A message that needs more room is a message dialog ([BingeMessageDialog]).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h8000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BingeSnackbarResultFitTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `a result sentence reads in full, in two lines or fewer, at 1_3x font`() {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1.3f)) {
                Box(Modifier.width(360.dp)) { BingeSnackbarWorstCaseSample() }
            }
        }
        rule.waitForIdle()

        val layouts = mutableListOf<TextLayoutResult>()
        val node = rule.onNode(hasText("Not changed. Couldn't reach your server.")).fetchSemanticsNode()
        node.config
            .getOrNull(SemanticsActions.GetTextLayoutResult)
            ?.action
            ?.invoke(layouts)
        val layout = layouts.single()

        assertTrue("took ${layout.lineCount} lines", layout.lineCount <= 2)
        assertFalse("the last line is cut", layout.isLineEllipsized(layout.lineCount - 1))
        assertEquals(false, layout.didOverflowHeight)
    }
}

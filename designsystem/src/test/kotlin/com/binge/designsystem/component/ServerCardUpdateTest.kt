package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val MIN_TOUCH_DP = 48

/** A [ServerCard]'s update row is one button, the whole width, that runs its click; without one there is no row. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ServerCardUpdateTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val isButton = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)

    private fun show(update: ServerCardUpdate?) {
        rule.setContent {
            TestTheme {
                ServerCard(title = "Home", logo = rememberVectorPainter(Icons.Filled.Tv), update = update)
            }
        }
    }

    @Test
    fun `the update row is one button that runs its click`() {
        var clicks = 0
        show(ServerCardUpdate("Update available: 2.6.0", "Release notes", onClick = { clicks++ }))

        rule
            .onNode(isButton and hasText("Update available: 2.6.0") and hasText("Release notes"), useUnmergedTree = false)
            .performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `the update row is at least the minimum touch target tall`() {
        show(ServerCardUpdate("Update available: 2.6.0", "Release notes", onClick = {}))

        val bounds = rule.onNode(isButton).fetchSemanticsNode().touchBoundsInRoot
        val minPx = with(rule.density) { MIN_TOUCH_DP.dp.toPx() }
        assertTrue("touch target height ${bounds.height} px is under $minPx", bounds.height >= minPx)
    }

    @Test
    fun `the row announces the mark's words after the label and the action`() {
        show(
            ServerCardUpdate(
                label = "Update available: 2.6.0",
                actionLabel = "Release notes",
                onClick = {},
                actionTrailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                actionTrailingContentDescription = "Opens outside the app",
            ),
        )

        val node = rule.onNode(isButton).fetchSemanticsNode()
        val texts = node.config[SemanticsProperties.Text].map { it.text }
        val described = node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
        assertEquals(listOf("Update available: 2.6.0", "Release notes"), texts)
        assertEquals(listOf("Opens outside the app"), described)
    }

    @Test
    fun `without a mark the row announces only its label and action`() {
        show(ServerCardUpdate("Update available: 2.6.0", "Release notes", onClick = {}))

        val node = rule.onNode(isButton).fetchSemanticsNode()
        assertEquals(emptyList<String>(), node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty())
    }

    @Test
    fun `no update shows no row`() {
        show(null)

        rule.onNodeWithText("Release notes").assertDoesNotExist()
        rule.onNode(isButton).assertDoesNotExist()
    }
}

/** The update row keeps its text whole at a large font, with and without the mark: no text clipped by its parent at 2x. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h800dp")
// Native graphics, because legacy graphics measures text at a pixel a character: nothing wraps and nothing is cut.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ServerCardUpdateClippingTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun assertNothingClipped(update: ServerCardUpdate) {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale = 2f)) {
                TestTheme { ServerCard(title = "Home", logo = rememberVectorPainter(Icons.Filled.Tv), update = update) }
            }
        }
        rule.waitForIdle()
        assertTrue("no text node was checked", rule.assertNoTextClipped() >= 2)
    }

    @Test
    fun `no text is clipped at 2_0 with the mark`() =
        assertNothingClipped(
            ServerCardUpdate(
                "Update available: 2.6.0",
                "Release notes",
                onClick = {},
                actionTrailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                actionTrailingContentDescription = "Opens outside the app",
            ),
        )

    @Test
    fun `no text is clipped at 2_0 without the mark`() =
        assertNothingClipped(
            ServerCardUpdate("Update available: 2.6.0", "Release notes", onClick = {
            }),
        )

    private fun show(fontScale: Float, update: ServerCardUpdate) {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                TestTheme { ServerCard(title = "Home", logo = rememberVectorPainter(Icons.Filled.Tv), update = update) }
            }
        }
        rule.waitForIdle()
    }

    private val update = ServerCardUpdate("Update available: 2.6.0", "Release notes", onClick = {})

    private fun labelLayout(): TextLayoutResult {
        val layouts = mutableListOf<TextLayoutResult>()
        rule
            .onNodeWithText("Update available: 2.6.0", useUnmergedTree = true)
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(layouts)
        return layouts.single()
    }

    @Test
    fun `at 2_0 the label breaks only between words and the action wraps under it`() {
        show(fontScale = 2f, update)

        val label = rule.onNodeWithText("Update available: 2.6.0", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val action = rule.onNodeWithText("Release notes", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val layout = labelLayout()
        val text = layout.layoutInput.text.text
        for (line in 1 until layout.lineCount) {
            assertEquals("the label breaks inside a word at line $line", ' ', text[layout.getLineStart(line) - 1])
        }
        assertTrue("the action is not under the label: ${action.top} vs ${label.bottom}", action.top >= label.bottom)
    }

    @Test
    fun `at the normal font the action stays on the label's line`() {
        show(fontScale = 1f, update)

        val label = rule.onNodeWithText("Update available: 2.6.0", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val action = rule.onNodeWithText("Release notes", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("the action dropped under the label", action.top < label.bottom)
        assertTrue("the action is not after the label", action.left >= label.right)
    }
}

package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
    fun `no update shows no row`() {
        show(null)

        rule.onNodeWithText("Release notes").assertDoesNotExist()
        rule.onNode(isButton).assertDoesNotExist()
    }
}

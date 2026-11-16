package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeSentiment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MIN_TOUCH_DP = 48

/** A [ServerCardChip] with an `onClick` is a button; one without is drawn and read as it always was. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ServerCardChipClickTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val isButton = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
    private val hasClick = SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick)

    private fun show(chip: ServerCardChip, statusChip: ServerCardChip? = null) {
        rule.setContent {
            TestTheme {
                ServerCard(
                    title = "Home",
                    logo = rememberVectorPainter(Icons.Filled.Tv),
                    chips = listOf(chip),
                    statusChip = statusChip,
                )
            }
        }
    }

    @Test
    fun `a chip with an onClick is a button that runs it`() {
        var clicks = 0
        show(ServerCardChip("Update available", BingeSentiment.Caution, onClick = { clicks++ }))

        rule.onNodeWithText("Update available").assert(isButton).performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `a content description replaces the spoken label of a tappable chip`() {
        show(ServerCardChip("Update available", BingeSentiment.Caution, onClick = {}, contentDescription = "Release notes"))

        rule.onNodeWithContentDescription("Release notes").assert(isButton)
        rule.onNodeWithText("Update available").assertDoesNotExist()
    }

    @Test
    fun `a tappable chip with no content description reads its label`() {
        show(ServerCardChip("Update available", BingeSentiment.Caution, onClick = {}))

        rule.onNodeWithText("Update available").assert(isButton)
    }

    @Test
    fun `a chip with no onClick is not a button and has no click action`() {
        show(ServerCardChip("Update available", BingeSentiment.Caution, contentDescription = "Release notes"))

        rule.onNodeWithText("Update available").assert(!isButton).assert(!hasClick)
    }

    @Test
    fun `the status chip stays a label even when it carries an onClick`() {
        show(
            ServerCardChip("Demo", BingeSentiment.Caution),
            statusChip = ServerCardChip("Connected", BingeSentiment.Positive, onClick = {}),
        )

        rule.onNodeWithText("Connected").assert(!isButton).assert(!hasClick)
    }

    @Test
    fun `a tappable chip's touch target is at least the minimum`() {
        show(ServerCardChip("Demo", BingeSentiment.Caution, onClick = {}))

        // The node is the pill at its own size; the touch bounds are what a finger can hit, and they are never smaller than 48dp.
        val bounds = rule.onNodeWithText("Demo").fetchSemanticsNode().touchBoundsInRoot
        val minPx = with(rule.density) { MIN_TOUCH_DP.dp.toPx() }
        assertTrue("touch target ${bounds.height} x ${bounds.width} px is under $minPx", bounds.height >= minPx && bounds.width >= minPx)
    }
}

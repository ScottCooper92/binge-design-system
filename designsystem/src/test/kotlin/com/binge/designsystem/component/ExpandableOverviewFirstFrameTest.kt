package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.R
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private const val NEXT = "next"

/**
 * The overview decides whether it overflows in the pass that places it, so on the first composed frame the toggle is already there or not, and
 * whatever sits below it is already where it will stay. The frame clock is held, so nothing after the first frame has run. Lines are separated by
 * newlines, so the count does not depend on the font.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ExpandableOverviewFirstFrameTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val more = RuntimeEnvironment.getApplication().getString(R.string.detail_show_more)

    private fun show(text: String, fontScale: Float) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                    Column(Modifier.width(300.dp)) {
                        ExpandableOverview(text = text)
                        Box(Modifier.size(24.dp).testTag(NEXT))
                    }
                }
            }
        }
    }

    private fun hasToggle() = rule.onAllNodesWithTextCount(more) > 0

    private fun nextTop() = rule.onNodeWithTag(NEXT).getUnclippedBoundsInRoot().top

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTextCount(text: String) =
        onAllNodes(
            androidx.compose.ui.test
                .hasText(text),
        ).fetchSemanticsNodes().size

    private fun assertFirstFrameIsSettled(
        text: String,
        fontScale: Float,
        toggle: Boolean,
    ) {
        show(text, fontScale)
        val firstToggle = hasToggle()
        val firstTop = nextTop()

        rule.mainClock.autoAdvance = true
        rule.waitForIdle()

        assertEquals("the toggle on the first frame", toggle, firstToggle)
        assertEquals("the toggle once settled", toggle, hasToggle())
        assertEquals("what sits below, first frame against settled", nextTop().value, firstTop.value, 0.01f)
    }

    @Test
    fun `a two line overview has no toggle and nothing moves, at 1x`() = assertFirstFrameIsSettled(TWO_LINES, 1f, toggle = false)

    @Test
    fun `a two line overview has no toggle and nothing moves, at 2x`() = assertFirstFrameIsSettled(TWO_LINES, 2f, toggle = false)

    @Test
    fun `an overview of exactly three lines has no toggle and nothing moves, at 1x`() =
        assertFirstFrameIsSettled(THREE_LINES, 1f, toggle = false)

    @Test
    fun `an overview of exactly three lines has no toggle and nothing moves, at 2x`() =
        assertFirstFrameIsSettled(THREE_LINES, 2f, toggle = false)

    @Test
    fun `a long overview has its toggle on the first frame and nothing moves, at 1x`() = assertFirstFrameIsSettled(LONG, 1f, toggle = true)

    @Test
    fun `a long overview has its toggle on the first frame and nothing moves, at 2x`() = assertFirstFrameIsSettled(LONG, 2f, toggle = true)

    private companion object {
        const val TWO_LINES = "First line\nSecond line"
        const val THREE_LINES = "First line\nSecond line\nThird line"
        const val LONG = "First line\nSecond line\nThird line\nFourth line\nFifth line"
    }
}

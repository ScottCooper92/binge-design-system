package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.R
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
 * [DetailActions] stacks its round actions under the primary from two up on a phone, keeps them beside it with one, and
 * [DetailActionsSkeleton] holds the height the loaded row has.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w411dp-h891dp-xhdpi")
class DetailActionsTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun actions(count: Int) = List(count) { DetailRoundAction(Icons.Filled.PlayArrow, "Action $it", onClick = {}) }

    private fun show(fontScale: Float = 1f, content: @androidx.compose.runtime.Composable () -> Unit) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) { Column { content() } }
            }
        }
        rule.waitForIdle()
    }

    private fun row(count: Int) =
        show {
            DetailActions(
                primary = { Text("Primary", Modifier.then(it).testTag("primary")) },
                roundActions = actions(count),
            )
        }

    private fun primaryBottom() = rule.onNodeWithTag("primary").getBoundsInRoot().bottom

    private fun actionTop(index: Int) = rule.onNodeWithText("Action $index", useUnmergedTree = true).getBoundsInRoot().top

    @Test
    fun `one round action sits beside the primary`() {
        row(1)

        val primary = rule.onNodeWithTag("primary").getBoundsInRoot()
        val action = rule.onNodeWithText("Action 0", useUnmergedTree = true).getBoundsInRoot()
        assertTrue("the action is beside the primary", action.left >= primary.right)
    }

    @Test
    fun `two round actions are stacked under the primary on a phone`() {
        row(2)

        assertTrue("the actions are below the primary", actionTop(0) >= primaryBottom())
        assertEquals(actionTop(0), actionTop(1))
    }

    @Test
    fun `the primary takes the whole row when stacked`() {
        row(3)

        val bounds = rule.onNodeWithTag("primary").getBoundsInRoot()
        val width = bounds.right - bounds.left
        assertTrue("the primary spans the row", width > actionWidthFloor())
    }

    private fun actionWidthFloor() =
        rule.onNodeWithText("Action 0", useUnmergedTree = true).getBoundsInRoot().let { (it.right - it.left) * 3 }

    private fun skeletonMatchesLoaded(fontScale: Float, count: Int) {
        show(fontScale) {
            Column(Modifier.testTag("loaded")) {
                DetailActions(
                    primary = { Text("P", Modifier.then(it).height(dimensionResource(R.dimen.button_filled_height))) },
                    roundActions = actions(count),
                )
            }
            Column(Modifier.testTag("skeleton")) { DetailActionsSkeleton(buttons = count) }
        }

        fun heightOf(tag: String) = rule.onNodeWithTag(tag).getBoundsInRoot().let { (it.bottom - it.top).value }

        assertEquals("buttons=$count at ${fontScale}x", heightOf("loaded"), heightOf("skeleton"), 1f)
    }

    @Test
    fun `the skeleton is the loaded row's height with no round actions`() = skeletonMatchesLoaded(1f, 0)

    @Test
    fun `the skeleton is the loaded row's height beside one round action at the largest font`() = skeletonMatchesLoaded(2f, 1)

    @Test
    fun `the skeleton is the loaded row's height stacked over three round actions`() = skeletonMatchesLoaded(1f, 3)

    @Test
    fun `the skeleton is the loaded row's height stacked over three round actions at the largest font`() = skeletonMatchesLoaded(2f, 3)

    @Test
    fun `a round action's trailing mark is announced after its name`() {
        show {
            DetailActions(
                primary = { Text("Primary", it) },
                roundActions = listOf(
                    DetailRoundAction(
                        Icons.Filled.PlayArrow,
                        "Trailer",
                        onClick = {},
                        trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                        trailingContentDescription = "Opens outside the app",
                    ),
                ),
            )
        }

        rule.onNode(hasContentDescription("Opens outside the app", substring = true)).assertExists()
    }

    private fun cells(parent: String) =
        rule
            .onAllNodes(hasTestTag(DETAIL_ACTION_CELL_TAG) and hasAnyAncestor(hasTestTag(parent)), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .map { it.boundsInRoot }

    private fun spreadRows(
        fontScale: Float,
        count: Int,
        withPrimary: Boolean,
    ) {
        show(fontScale) {
            Column(Modifier.testTag("loaded")) {
                DetailActions(
                    primary = if (withPrimary) {
                        { Text("P", Modifier.then(it).height(dimensionResource(R.dimen.button_filled_height))) }
                    } else {
                        null
                    },
                    roundActions = actions(count),
                )
            }
            Column(Modifier.testTag("skeleton")) { DetailActionsSkeleton(buttons = count, primary = withPrimary) }
        }
    }

    private fun assertSpreadEvenly(
        fontScale: Float,
        count: Int,
        withPrimary: Boolean,
    ) {
        spreadRows(fontScale, count, withPrimary)
        val loaded = cells("loaded")
        assertEquals("cells at ${fontScale}x, $count buttons", count, loaded.size)
        val widths = loaded.map { it.width }
        widths.forEach { assertEquals("every cell is the same width", widths.first(), it, 1f) }
        val spacingPx = rule.density.run { 10.dp.toPx() }
        loaded.zipWithNext { a, b -> b.left - a.right }.forEach {
            assertEquals("the cells are spaced by detail_action_row_spacing", spacingPx, it, 1f)
        }
        // The cells fill the row between the content insets, so the last ends as far from the right edge as the first is from the left.
        val rootRight = rule
            .onRoot()
            .fetchSemanticsNode()
            .boundsInRoot.right
        assertEquals("the row is not inset evenly", loaded.first().left, rootRight - loaded.last().right, 1f)
    }

    private fun assertSkeletonCellsMatch(
        fontScale: Float,
        count: Int,
        withPrimary: Boolean,
    ) {
        spreadRows(fontScale, count, withPrimary)
        val loaded = cells("loaded")
        val skeleton = cells("skeleton")
        assertEquals(loaded.size, skeleton.size)
        loaded.zip(skeleton).forEachIndexed { index, (l, s) ->
            assertEquals("cell $index left at ${fontScale}x", l.left, s.left, 1f)
            assertEquals("cell $index width at ${fontScale}x", l.width, s.width, 1f)
        }
    }

    @Test fun `two round actions spread evenly under the primary`() = assertSpreadEvenly(1f, 2, withPrimary = true)

    @Test fun `three round actions spread evenly under the primary`() = assertSpreadEvenly(1f, 3, withPrimary = true)

    @Test fun `four round actions spread evenly under the primary`() = assertSpreadEvenly(1f, 4, withPrimary = true)

    @Test fun `round actions with no primary spread evenly across the row`() = assertSpreadEvenly(1f, 3, withPrimary = false)

    @Test fun `three round actions spread evenly at the largest font`() = assertSpreadEvenly(2f, 3, withPrimary = true)

    @Test fun `the skeleton's plates sit in the loaded row's cells for two`() = assertSkeletonCellsMatch(1f, 2, withPrimary = true)

    @Test fun `the skeleton's plates sit in the loaded row's cells for three`() = assertSkeletonCellsMatch(1f, 3, withPrimary = true)

    @Test fun `the skeleton's plates sit in the loaded row's cells for four`() = assertSkeletonCellsMatch(1f, 4, withPrimary = true)

    @Test fun `the skeleton's plates sit in the loaded row's cells with no primary`() = assertSkeletonCellsMatch(1f, 3, withPrimary = false)

    @Test fun `the skeleton's plates sit in the loaded row's cells at the largest font`() =
        assertSkeletonCellsMatch(
            2f,
            3,
            withPrimary = true,
        )
}

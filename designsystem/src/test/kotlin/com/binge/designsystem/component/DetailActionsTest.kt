package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
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

    private fun actions(count: Int) =
        List(count) { DetailRoundAction(Icons.Filled.PlayArrow, "Action $it", onClick = {}) }

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
}

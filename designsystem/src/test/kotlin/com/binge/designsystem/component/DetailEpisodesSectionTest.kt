package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** [DetailEpisodesSection] stacks what its state slot draws, one view under the next, and shows no more rows than it is told to. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class DetailEpisodesSectionTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun bottom(tag: String) =
        rule
            .onNodeWithTag(tag)
            .getBoundsInRoot()
            .bottom.value

    private fun top(tag: String) =
        rule
            .onNodeWithTag(tag)
            .getBoundsInRoot()
            .top.value

    @Test
    fun `the views of the state slot are stacked and not laid over each other`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                DetailEpisodesSection(
                    title = "Episodes",
                    episodes = emptyList(),
                    stateContent = {
                        Box(Modifier.testTag("first").fillMaxWidth().height(40.dp))
                        Box(Modifier.testTag("second").fillMaxWidth().height(40.dp))
                    },
                )
            }
        }
        rule.waitForIdle()

        assertTrue("the second view is below the first", top("second") >= bottom("first"))
    }

    @Test
    fun `a negative count of shown episodes shows none and does not throw`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                DetailEpisodesSection(title = "Episodes", episodes = listOf(EpisodeRow(1, "Pilot", null)), shown = -1)
            }
        }
        rule.waitForIdle()

        rule.onAllNodesWithText("Pilot", useUnmergedTree = true).assertCountEquals(0)
    }
}

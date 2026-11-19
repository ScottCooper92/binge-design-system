package com.binge.designsystem.component

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * An [EpisodeBrowseRow]'s still is decorative beside the episode's name, so a screen reader says the name once: from the text, not again
 * from the image.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class EpisodeBrowseRowStillSemanticsTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val name = "The North Remembers"

    @Test
    fun `the still carries no content description and the name is read once`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                EpisodeBrowseRow(episode = EpisodeBrowseUi(number = 3, name = name), onClick = {})
            }
        }

        rule.onAllNodesWithContentDescription(name, useUnmergedTree = true).assertCountEquals(0)
        rule.onAllNodesWithText(name, useUnmergedTree = true).assertCountEquals(1)
    }
}

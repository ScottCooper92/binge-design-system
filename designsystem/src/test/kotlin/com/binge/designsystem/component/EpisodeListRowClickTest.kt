package com.binge.designsystem.component

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * An [EpisodeListRow] with an `onClick` is a button; with a null one it is only displayed, with the same text and no
 * click semantics, button role or focus target. The ripple is the click's own indication, so it goes with them.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class EpisodeListRowClickTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(onClick: (() -> Unit)?) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                EpisodeListRow(
                    episodeNumber = 3,
                    name = "The North Remembers",
                    stillUrl = null,
                    onClick = onClick,
                    airDate = "Mar 5, 2023",
                )
            }
        }
    }

    @Test
    fun `a row with an onClick is a focusable button that reports its click`() {
        var clicks = 0
        show(onClick = { clicks++ })

        rule
            .onNodeWithText("The North Remembers")
            .assertHasClickAction()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Focused))
            .performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `a row with a null onClick is not a button, has no click action and is not focusable`() {
        show(onClick = null)

        // The same text is on screen, so the row still reads as the episode.
        rule
            .onNodeWithText("The North Remembers")
            .assertHasNoClickAction()
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Focused))
        rule.onNodeWithText("EP 03").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
    }
}

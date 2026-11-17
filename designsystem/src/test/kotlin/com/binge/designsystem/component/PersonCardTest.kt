package com.binge.designsystem.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** [PersonCard]: one click target and one accessibility item, the initials when there is no photo, and a skeleton the card's own height. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h800dp")
// Native graphics, because the skeleton and the card must agree on the height of their text, which legacy graphics measures at a pixel a character.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PersonCardTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `the card is one click target announcing the name and subtitle`() {
        var clicks = 0
        rule.setContent {
            TestTheme { PersonCard(name = "Amara Okafor", subtitle = "Lead", photoUrl = null, onClick = { clicks++ }) }
        }

        rule.onNode(hasClickAction()).assertContentDescriptionEquals("Amara Okafor, Lead").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `a card with no subtitle announces the name alone`() {
        rule.setContent { TestTheme { PersonCard(name = "Amara Okafor", subtitle = null, photoUrl = null, onClick = {}) } }

        rule.onNode(hasClickAction()).assertContentDescriptionEquals("Amara Okafor")
    }

    @Test
    fun `a description replaces the spoken label`() {
        rule.setContent {
            TestTheme {
                PersonCard(
                    name = "Amara Okafor",
                    subtitle = "Lead",
                    photoUrl = null,
                    onClick = {},
                    description = "Amara Okafor, plays the lead",
                )
            }
        }

        rule.onNode(hasClickAction()).assertContentDescriptionEquals("Amara Okafor, plays the lead")
    }

    @Test
    fun `with no photo the initials stand in`() {
        rule.setContent { TestTheme { PersonCard(name = "Amara Okafor", subtitle = "Lead", photoUrl = null, onClick = {}) } }

        rule.onAllNodesWithText("AO", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun `the skeleton takes exactly the room the card does`() {
        rule.setContent {
            TestTheme {
                PersonCard(name = "Amara Okafor", subtitle = "Lead", photoUrl = null, onClick = {}, modifier = Modifier.testTag(CARD))
                PersonCardSkeleton(modifier = Modifier.testTag(SKELETON))
            }
        }

        val card = rule.onNodeWithTag(CARD).fetchSemanticsNode().size
        val skeleton = rule.onNodeWithTag(SKELETON).fetchSemanticsNode().size
        assertEquals(card, skeleton)
    }

    private companion object {
        const val CARD = "card"
        const val SKELETON = "skeleton"
    }
}

package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * [PersonGridCard]: one click target announcing the name and role, a 2:3 photo that fills its cell, and a skeleton the card's own height at
 * every font scale, with or without a role.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h800dp")
// Native graphics, because the skeleton and the card must agree on the height of their text, which legacy graphics measures at a pixel a character.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PersonGridCardTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `the card is one click target announcing the name and role`() {
        var clicks = 0
        rule.setContent {
            TestTheme { PersonGridCard(name = "Amara Okafor", role = "Lead", photoUrl = null, onClick = { clicks++ }) }
        }

        rule.onNode(hasClickAction()).assertContentDescriptionEquals("Amara Okafor, Lead").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `a card with a blank role announces the name alone`() {
        rule.setContent { TestTheme { PersonGridCard(name = "Amara Okafor", role = " ", photoUrl = null, onClick = {}) } }

        rule.onNode(hasClickAction()).assertContentDescriptionEquals("Amara Okafor")
    }

    @Test
    fun `a description replaces the spoken label`() {
        rule.setContent {
            TestTheme {
                PersonGridCard(
                    name = "Amara Okafor",
                    role = "Lead",
                    photoUrl = null,
                    onClick = {},
                    description = "Amara Okafor, plays the lead",
                )
            }
        }

        rule.onNode(hasClickAction()).assertContentDescriptionEquals("Amara Okafor, plays the lead")
    }

    @Test
    fun `the card fills its cell and keeps the poster's aspect`() {
        rule.setContent {
            TestTheme {
                Box(Modifier.width(CELL_WIDTH.dp)) {
                    PersonGridCard(name = "Amara Okafor", role = "Lead", photoUrl = null, onClick = {}, modifier = Modifier.testTag(CARD))
                }
            }
        }

        val bounds = rule.onNodeWithTag(CARD).getBoundsInRoot()
        assertEquals(CELL_WIDTH, bounds.wide(), 0.5f)
        assert(
            bounds.tall() > CELL_WIDTH / CARD_ASPECT_RATIO,
        ) { "the caption sits under a photo ${CELL_WIDTH / CARD_ASPECT_RATIO}dp tall" }
    }

    @Test
    fun `the skeleton takes exactly the room the card does at every font scale`() {
        var scale by mutableFloatStateOf(1f)
        var role by mutableStateOf<String?>("Lead")
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = scale)) {
                TestTheme {
                    Column {
                        Box(Modifier.width(CELL_WIDTH.dp)) {
                            PersonGridCard(
                                name = "Amara Okafor",
                                role = role,
                                photoUrl = null,
                                onClick = {},
                                modifier = Modifier.testTag(CARD),
                            )
                        }
                        Box(Modifier.width(CELL_WIDTH.dp)) { PersonGridCardSkeleton(Modifier.testTag(SKELETON)) }
                    }
                }
            }
        }
        for (fontScale in listOf(1f, 1.3f, 2f)) {
            for (text in listOf("Lead", null)) {
                scale = fontScale
                role = text
                rule.waitForIdle()
                assertEquals(
                    "font scale $fontScale, role $text",
                    rule.onNodeWithTag(CARD).getBoundsInRoot().tall(),
                    rule.onNodeWithTag(SKELETON).getBoundsInRoot().tall(),
                    0.5f,
                )
            }
        }
    }

    private fun DpRect.wide() = right.value - left.value

    private fun DpRect.tall() = bottom.value - top.value

    private companion object {
        const val CELL_WIDTH = 112f
        const val CARD = "card"
        const val SKELETON = "skeleton"
    }
}

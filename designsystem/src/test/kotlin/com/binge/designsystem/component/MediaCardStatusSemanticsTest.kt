package com.binge.designsystem.component

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasClickAction
import com.binge.designsystem.R
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeSentiment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A [MediaCard]'s status is a coloured dot with no words, so its label has to be in the card's semantics or a screen reader never says it:
 * "Arrival, Available".
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h800dp")
class MediaCardStatusSemanticsTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var status by mutableStateOf<MediaCardStatus?>(null)
    private var favourite by mutableStateOf(false)
    private var watched by mutableStateOf(false)
    private var unwatchedCount by mutableStateOf<Int?>(null)

    private fun show(initial: MediaCardStatus?) {
        status = initial
        rule.setContent {
            TestTheme {
                MediaCard(
                    posterUrl = null,
                    title = "Arrival",
                    rating = null,
                    onClick = {},
                    status = status,
                    favourite = favourite,
                    watched = watched,
                    unwatchedCount = unwatchedCount,
                    modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
                )
            }
        }
    }

    private fun stateDescriptionOfCard(): String? =
        rule
            .onNode(hasClickAction())
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.StateDescription)

    @Test
    fun `every sentiment's label is announced with the card`() {
        show(null)
        BingeSentiment.entries.forEach { sentiment ->
            status = MediaCardStatus(label = "Label ${sentiment.name}", sentiment = sentiment)
            rule.waitForIdle()

            assertEquals("Label ${sentiment.name}", stateDescriptionOfCard())
        }
    }

    @Test
    fun `a card with no status announces none`() {
        show(null)
        rule.waitForIdle()

        assertNull(stateDescriptionOfCard())
    }

    @Test
    fun `the card with a status is still one button`() {
        show(MediaCardStatus(label = "Available", sentiment = BingeSentiment.Positive))
        rule.waitForIdle()

        rule.onNode(hasClickAction()).assertHasClickAction()
        rule.onNode(hasClickAction()).assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.StateDescription))
    }

    @Test
    fun `a favourite is announced with the card, and is not a separate target`() {
        show(null)
        favourite = true
        rule.waitForIdle()

        assertEquals("Favourite", stateDescriptionOfCard())
        // One click target, the card; the heart is drawn only and adds no node that takes a click or a focus.
        rule.onAllNodes(hasClickAction()).assertCountEquals(1)
    }

    @Test
    fun `a favourite with a status reads the favourite first`() {
        show(MediaCardStatus(label = "Available", sentiment = BingeSentiment.Positive))
        favourite = true
        rule.waitForIdle()

        assertEquals("Favourite, Available", stateDescriptionOfCard())
        rule.onAllNodes(hasClickAction()).assertCountEquals(1)
    }

    @Test
    fun `a card that is not a favourite does not say so`() {
        show(MediaCardStatus(label = "Available", sentiment = BingeSentiment.Positive))
        favourite = false
        rule.waitForIdle()

        assertEquals("Available", stateDescriptionOfCard())
    }

    private fun textOfCard(): List<String> =
        rule
            .onNode(hasClickAction())
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
            .orEmpty()
            .map { it.text }

    @Test
    fun `a watched card announces it first, and is still one target`() {
        show(MediaCardStatus(label = "Available", sentiment = BingeSentiment.Positive))
        watched = true
        favourite = true
        rule.waitForIdle()

        assertEquals("Watched, Favourite, Available", stateDescriptionOfCard())
        rule.onAllNodes(hasClickAction()).assertCountEquals(1)
    }

    @Test
    fun `an unwatched count is announced once, in words, and its digits are not read again`() {
        show(null)
        unwatchedCount = 3
        rule.waitForIdle()

        assertEquals("3 unwatched", stateDescriptionOfCard())
        assertEquals(
            listOf("Arrival"),
            textOfCard()
                .filter { it.isNotBlank() }
                .filter { it != "" }
                .distinct()
                .filter { it == "Arrival" },
        )
        assertTrue("the badge's digits leaked into the card's text: ${textOfCard()}", textOfCard().none { it == "3" })
        rule.onAllNodes(hasClickAction()).assertCountEquals(1)
    }

    @Test
    fun `a count above 99 does not read 99 plus, and a count of zero says nothing`() {
        show(null)
        unwatchedCount = 120
        rule.waitForIdle()
        assertEquals("120 unwatched", stateDescriptionOfCard())
        assertTrue(textOfCard().none { it.contains("99") })

        unwatchedCount = 0
        rule.waitForIdle()
        assertNull(stateDescriptionOfCard())
    }

    @Test
    fun `a watched card draws the tick alone, so an unwatched count is not announced beside it`() {
        show(null)
        watched = true
        unwatchedCount = 3
        rule.waitForIdle()

        assertEquals("Watched", stateDescriptionOfCard())
    }
}

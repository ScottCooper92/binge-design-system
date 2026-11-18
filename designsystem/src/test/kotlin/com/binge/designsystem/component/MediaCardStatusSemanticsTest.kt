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
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasClickAction
import com.binge.designsystem.R
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeSentiment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}

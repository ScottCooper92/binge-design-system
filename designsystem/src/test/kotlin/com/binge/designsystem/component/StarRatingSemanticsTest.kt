package com.binge.designsystem.component

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val STARS = 5

private val isFocusable = SemanticsMatcher.keyIsDefined(SemanticsProperties.Focused)
private val isButton = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
private val takesLongClick = SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick)

/**
 * Guards that a rating can be set without the gesture. The interactive control took its taps through
 * `detectTapGestures`, which publishes nothing to the accessibility tree — five stars contributing no
 * name, no role and no action, under a row whose only semantics was a static description of the
 * current value. Rating a title is a primary action of the app, and it was impossible with TalkBack or
 * Switch Access and unreachable by focus (#2195; WCAG 4.1.2 and 2.1.1).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class StarRatingSemanticsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setRating(rating: Float, onRatingChange: (Float) -> Unit = {}) {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                StarRating(rating = rating, interactive = true, onRatingChange = onRatingChange)
            }
        }
    }

    @Test
    fun `every star is a focusable button that takes a long press`() {
        setRating(6f)

        composeTestRule
            .onAllNodes(hasClickAction())
            .assertCountEquals(STARS)
            .assertAll(isButton and isFocusable and takesLongClick)
    }

    /** The name is the value the tap sets, so a screen reader picks a rating rather than a star. */
    @Test
    fun `a star names the value its tap would set`() {
        var rated: Float? = null
        setRating(0f) { rated = it }

        composeTestRule.onNodeWithContentDescription("Rate 8.0 out of 10").performClick()

        assertEquals(8f, rated)
    }

    /** Tapping a star that is already full drops it to its half, and the name has to say so. */
    @Test
    fun `a full star names the half its tap would drop it to`() {
        var rated: Float? = null
        setRating(8f) { rated = it }

        composeTestRule.onNodeWithContentDescription("Rate 7.0 out of 10").performClick()

        assertEquals(7f, rated)
    }

    /** The odd values live behind the long press, which is now an action a screen reader can invoke. */
    @Test
    fun `the long press action sets the half value`() {
        var rated: Float? = null
        setRating(0f) { rated = it }

        composeTestRule
            .onNodeWithContentDescription("Rate 6.0 out of 10")
            .performSemanticsAction(SemanticsActions.OnLongClick) { it() }

        assertEquals(5f, rated)
    }

    /** The row still announces the value it is showing — the half the old control did have. */
    @Test
    fun `the row still announces the current rating`() {
        setRating(6f)

        composeTestRule.onNodeWithContentDescription("Rating: 6.0 out of 10").assertExists()
    }

    /**
     * An interactive star is 28dp with 3dp to the next, the app's smallest target and well under the
     * 48dp guidance. A gesture detector gets no expansion; `combinedClickable` does.
     */
    @Test
    fun `an interactive star clears the minimum touch target`() {
        var density = Density(1f)
        composeTestRule.setContent {
            density = LocalDensity.current
            BingeExpressiveTheme(dynamicColor = false) {
                StarRating(rating = 6f, interactive = true, onRatingChange = {})
            }
        }

        val touch = composeTestRule.onAllNodes(hasClickAction())[0].fetchSemanticsNode().touchBoundsInRoot
        val minimum = with(density) { 48.dp.toPx() }
        assertTrue(
            "touch bounds are ${touch.width} x ${touch.height}px; the 48dp minimum is ${minimum}px",
            touch.width >= minimum && touch.height >= minimum,
        )
    }
}

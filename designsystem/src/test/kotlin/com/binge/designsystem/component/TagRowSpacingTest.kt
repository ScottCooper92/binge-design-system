package com.binge.designsystem.component

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TOLERANCE = 0.5f
private const val GAP = 8f
private const val MIN_TOUCH = 48f
private val Tags = listOf("alaska", "dog", "special forces", "military", "american wilderness", "survival", "rescue")

/**
 * A tappable compact tag takes only its visual size in the layout, so the gaps between tags are the even 8dp and a row is as tall as its
 * tags; its touch target is still at least 48dp, reaching into the empty space around it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TagRowSpacingTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val tapped = mutableListOf<Int>()

    private fun show(width: Int = 360) {
        rule.setContent {
            TestTheme {
                TagRow(tags = Tags, compact = true, onTagClick = { tapped += it }, modifier = Modifier.width(width.dp))
            }
        }
    }

    private fun bounds(label: String) = rule.onNodeWithText(label).getBoundsInRoot()

    @Test
    fun `tags on one line are 8dp apart however short or long they are`() {
        show()
        val alaska = bounds("alaska")
        val dog = bounds("dog")
        val special = bounds("special forces")

        assertEquals(GAP, dog.left.value - alaska.right.value, TOLERANCE)
        assertEquals(GAP, special.left.value - dog.right.value, TOLERANCE)
    }

    @Test
    fun `rows are 8dp apart and as tall as their tags`() {
        show(width = 200)
        val first = bounds("alaska")
        val wrapped = rule
            .onAllNodes(hasClickAction())
            .fetchSemanticsNodes()
            .map { it.boundsInRoot.top }
            .distinct()
            .sorted()

        assertTrue("The tags wrap onto more than one line", wrapped.size > 1)
        val rowStep = (wrapped[1] - wrapped[0]) / rule.density.density
        assertEquals(first.height.value + GAP, rowStep, TOLERANCE)
        assertTrue("A tag is not stretched to the touch target", first.height.value < MIN_TOUCH)
    }

    @Test
    fun `a tap just outside a tag still reaches it, up to a 48dp target`() {
        show()
        val tag = bounds("alaska")
        val outside = (MIN_TOUCH - tag.height.value) / 2f - 1f

        rule.onNodeWithText("alaska").performTouchInput { click(Offset(centerX, -outside * density)) }

        assertEquals(listOf(0), tapped)
    }
}

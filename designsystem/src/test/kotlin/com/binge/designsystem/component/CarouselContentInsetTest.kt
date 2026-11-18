package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val FIRST_ITEM = "first-item"
private const val TRAILING = "trailing"
private const val TOLERANCE = 0.5f

/**
 * A [MediaCarousel]'s first item and a [SectionHeader]'s title start at the screen's content inset, which widens with the window
 * (16, 24, then 32dp), so a row lines up with the headers and chip rows around it on a tablet or a foldable.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class CarouselContentInsetTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun assertFirstItemStartsAt(expectedDp: Float) {
        rule.setContent {
            TestTheme {
                MediaCarousel(title = "Trending", items = listOf(1, 2, 3), itemKey = { it }, onEndTileClick = null) { index, _ ->
                    Box(Modifier.size(40.dp).then(if (index == 0) Modifier.testTag(FIRST_ITEM) else Modifier))
                }
            }
        }
        rule.waitForIdle()

        assertEquals(
            expectedDp,
            rule
                .onNodeWithTag(FIRST_ITEM)
                .getBoundsInRoot()
                .left.value,
            TOLERANCE,
        )
    }

    private fun assertHeaderTrailingEndsAt(expectedGapDp: Float) {
        rule.setContent {
            TestTheme {
                SectionHeader(title = "Trending", trailingContent = { Box(Modifier.size(8.dp).testTag(TRAILING)) })
            }
        }
        rule.waitForIdle()

        val root = rule.onNodeWithTag(TRAILING).getBoundsInRoot()
        val width = rule
            .onRoot()
            .getBoundsInRoot()
            .right.value
        assertEquals(expectedGapDp, width - root.right.value, TOLERANCE)
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi")
    fun `at 411dp the first card starts at 16dp`() = assertFirstItemStartsAt(16f)

    @Test
    @Config(qualifiers = "w700dp-h900dp-xhdpi")
    fun `at 700dp the first card starts at 24dp`() = assertFirstItemStartsAt(24f)

    @Test
    @Config(qualifiers = "w900dp-h900dp-xhdpi")
    fun `at 900dp the first card starts at 32dp`() = assertFirstItemStartsAt(32f)

    @Test
    @Config(qualifiers = "w700dp-h900dp-xhdpi")
    fun `at 700dp a header's trailing content is 24dp from the end`() = assertHeaderTrailingEndsAt(24f)

    @Test
    @Config(qualifiers = "w900dp-h900dp-xhdpi")
    fun `at 900dp a header's trailing content is 32dp from the end`() = assertHeaderTrailingEndsAt(32f)
}

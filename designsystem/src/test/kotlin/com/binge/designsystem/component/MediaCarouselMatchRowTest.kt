package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * [MediaCarousel] with `endTileHeight = Dp.Unspecified`: the trailing tile is as tall as the tallest item the row has composed, at any font
 * scale, and follows the items down as well as up.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h800dp")
// Native graphics, because a person card's height is its text's, which legacy graphics measures at a pixel a character.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MediaCarouselMatchRowTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun endTileHeightPx(): Int {
        // "See all" is on the header link too; the end tile is the second.
        val endTile = rule.onAllNodesWithText("See all")[1]
        return endTile.fetchSemanticsNode().size.height
    }

    private var fontScale by mutableFloatStateOf(1f)

    private fun showPersonRow() {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme {
                    MediaCarousel(
                        title = "Cast",
                        items = listOf("Amara Okafor", "Jonas Lindqvist"),
                        itemKey = { it },
                        onMoreClick = {},
                        endTileHeight = Dp.Unspecified,
                    ) { _, name ->
                        PersonCard(name = name, subtitle = "Lead", photoUrl = null, onClick = {}, modifier = Modifier.testTag(name))
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun cardHeightPx() =
        rule
            .onNodeWithTag("Amara Okafor")
            .fetchSemanticsNode()
            .size.height

    @Test
    fun `the end tile is as tall as a person card, and follows it as the font scale grows`() {
        fontScale = 1f
        showPersonRow()
        val atDefault = cardHeightPx()
        assertEquals(atDefault, endTileHeightPx())

        fontScale = 2f
        rule.waitForIdle()

        val atTwice = cardHeightPx()
        assertTrue("the card should be taller at 2x ($atTwice) than at the default ($atDefault)", atTwice > atDefault)
        assertEquals(atTwice, endTileHeightPx())
    }

    @Test
    fun `the end tile follows the items down when they shrink`() {
        var tall by mutableStateOf(true)
        rule.setContent {
            TestTheme {
                MediaCarousel(
                    title = "Row",
                    items = listOf(1, 2),
                    itemKey = { it },
                    onMoreClick = {},
                    endTileHeight = Dp.Unspecified,
                ) { _, _ -> Box(Modifier.width(80.dp).height(if (tall) 120.dp else 60.dp).testTag("item")) }
            }
        }
        rule.waitForIdle()
        val density = rule.density.density
        assertEquals(120f, endTileHeightPx() / density, 1f)

        tall = false
        rule.waitForIdle()

        assertEquals(60f, endTileHeightPx() / density, 1f)
    }

    @Test
    fun `an explicit height still wins`() {
        rule.setContent {
            TestTheme {
                MediaCarousel(title = "Row", items = listOf(1), itemKey = { it }, onMoreClick = {}, endTileHeight = 100.dp) { _, _ ->
                    Box(Modifier.width(80.dp).height(40.dp))
                }
            }
        }
        rule.waitForIdle()

        assertEquals(100f, endTileHeightPx() / rule.density.density, 1f)
    }
}

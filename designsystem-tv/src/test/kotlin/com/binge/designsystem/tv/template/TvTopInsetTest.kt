package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.template.ScreenAction
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.nav.LocalTvHostedAsOverlay
import com.binge.designsystem.tv.nav.LocalTvTabHosted
import com.binge.designsystem.tv.nav.LocalTvTopInset
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val TAB = "tab"
private const val BODY = "Nothing here yet."
private const val ACTION = "Refresh"
private val Bar = 56.dp
private val CardWidth = 120.dp
private const val TOLERANCE = 0.5f

private val Rows = listOf(TvHubRow(key = "trending", title = "Trending", items = List(4) { it }))

/**
 * A host's top bar pushes every page down by its height, and the hub's copy band, its rows and the grid move together,
 * so the copy neither clips under the bar nor overlaps the rows. A tab's page claims no focus when it arrives: the tab keeps it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
// The row anchors hold a TvSectionTitle, and legacy graphics measures text at one pixel per character.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvTopInsetTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private var inset by mutableStateOf(0.dp)

    @Test
    fun `the hub's copy band and first row move down by the inset together`() {
        show { Hub() }
        val copyBefore = top(TvLayoutAnchors.HUB_COPY)
        val rowBefore = top(TvLayoutAnchors.hubRow(0))

        inset = Bar
        rule.waitForIdle()

        assertMovedByBar(copyBefore, top(TvLayoutAnchors.HUB_COPY))
        assertMovedByBar(rowBefore, top(TvLayoutAnchors.hubRow(0)))
    }

    @Test
    fun `the grid's first cell moves down by the inset`() {
        show { Grid() }
        val before = top(TvLayoutAnchors.GRID_FIRST_CELL)

        inset = Bar
        rule.waitForIdle()

        assertMovedByBar(before, top(TvLayoutAnchors.GRID_FIRST_CELL))
    }

    @Test
    fun `a page's padding takes the inset`() {
        show { TvMessagePage(body = BODY, hosting = TvPageHosting.Overlay) }
        val before = rule.onNodeWithText(BODY).getUnclippedBoundsInRoot().top

        inset = Bar
        rule.waitForIdle()

        // The plate centres in what is left, so it moves by half the bar.
        val after = rule.onNodeWithText(BODY).getUnclippedBoundsInRoot().top
        assertEquals((Bar / 2).value, (after - before).value, TOLERANCE)
    }

    @Test
    fun `a tab's page leaves focus on the tab`() {
        arrive(tabHosted = true)

        rule.onNodeWithTag(TAB).assertIsFocused()
        rule.onNodeWithText(ACTION).assertIsNotFocused()
    }

    @Test
    fun `an overlay that is not a tab still takes focus when it arrives`() {
        arrive(tabHosted = false)

        rule.onNodeWithText(ACTION).assertIsFocused()
    }

    private fun arrive(tabHosted: Boolean) {
        var shown by mutableStateOf(false)
        rule.setContent {
            BingeTvTheme {
                CompositionLocalProvider(LocalTvHostedAsOverlay provides true, LocalTvTabHosted provides tabHosted) {
                    Column {
                        Box(Modifier.size(48.dp).testTag(TAB).focusable())
                        if (shown) {
                            TvMessagePage(body = BODY, hosting = TvPageHosting.Overlay, primary = ScreenAction(ACTION, onClick = {}))
                        }
                    }
                }
            }
        }
        rule.onNodeWithTag(TAB).requestFocus()
        rule.waitForIdle()
        shown = true
        rule.waitForIdle()
    }

    private fun show(content: @Composable () -> Unit) {
        rule.setContent {
            BingeTvTheme {
                CompositionLocalProvider(LocalTvTopInset provides inset, LocalTvHostedAsOverlay provides true) { content() }
            }
        }
        rule.waitForIdle()
    }

    private fun top(anchor: String): Dp = rule.onNodeWithTag(anchor, useUnmergedTree = true).getUnclippedBoundsInRoot().top

    private fun assertMovedByBar(before: Dp, after: Dp) = assertEquals(Bar.value, (after - before).value, TOLERANCE)
}

@Composable
private fun Hub() {
    TvImmersiveHub(
        rows = Rows,
        itemId = { it },
        cardWidth = CardWidth,
        onItemClick = {},
        artwork = {},
        copy = {},
        hosting = TvPageHosting.Overlay,
    ) { _, _, onFocusChanged, onClick, cellModifier ->
        Box(cellModifier.width(CardWidth).aspectRatio(2f / 3f).tvClickable(onFocusChanged = onFocusChanged, onClick = onClick))
    }
}

@Composable
private fun Grid() {
    TvImmersiveGrid(
        heading = "Trending",
        count = 8,
        itemAt = { it },
        itemKey = { it },
        artwork = {},
        copy = {},
        hosting = TvPageHosting.Overlay,
    ) { _, _, onFocusChanged, cellModifier ->
        Box(cellModifier.aspectRatio(2f / 3f).tvClickable(onFocusChanged = onFocusChanged, onClick = {}))
    }
}

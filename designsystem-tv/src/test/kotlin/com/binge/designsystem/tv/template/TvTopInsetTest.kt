package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.template.ScreenAction
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.nav.LocalTvHostedAsOverlay
import com.binge.designsystem.tv.nav.LocalTvTabHosted
import com.binge.designsystem.tv.nav.LocalTvTopInset
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val TAB = "tab"
private const val BODY = "Nothing here yet."
private const val ACTION = "Refresh"

/** A real top bar's height: the top overscan, an icon button, and the bar's bottom gap. */
private val Bar = 95.dp

/** A bar shorter than the overlap the immersive content does not add. */
private val ShortBar = 56.dp
private const val SYNOPSIS = "synopsis"
private const val META = "meta"
private const val SYNOPSIS_LINES = 2
private const val LONG_SYNOPSIS =
    "A synopsis long enough to need more than two lines at the band's width, so the copy has to fit its lines into " +
        "what the band leaves it rather than running past the band's end and up under the bar above it."
private val CardWidth = 120.dp
private const val TOLERANCE = 0.5f

private val Rows = listOf(TvHubRow(key = "trending", title = "Trending", items = List(4) { it }))

/**
 * A host's top bar moves the hub's copy band, its rows and the grid together, by the bar's height less the overlap the bar
 * already holds, so the copy clears the bar without opening a gap under it; with no bar the geometry is as it always was.
 * A page's padding takes the whole inset. A tab's page claims no focus when it arrives: the tab keeps it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
// The row anchors hold a TvSectionTitle, and legacy graphics measures text at one pixel per character.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvTopInsetTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private var inset by mutableStateOf(0.dp)

    /** Binge's hub has no top bar, so its geometry must not move at all. */
    @Test
    fun `with no top bar the hub's copy band and first row sit where they always have`() {
        show { Hub() }

        assertAt(OVERSCAN, top(TvLayoutAnchors.HUB_COPY))
        assertAt(CONTENT_TOP - BAND_BOTTOM, bottom(TvLayoutAnchors.HUB_COPY))
        assertAt(CONTENT_TOP + ROW_GAP, top(TvLayoutAnchors.hubRow(0)))
    }

    @Test
    fun `with no top bar the grid's first cell sits where it always has`() {
        show { Grid() }

        assertAt(CONTENT_TOP, top(TvLayoutAnchors.GRID_FIRST_CELL))
    }

    @Test
    fun `under a top bar the hub's rows move by the bar less its overlap, and the copy starts just inside the bar's foot`() {
        show { Hub() }

        inset = Bar
        rule.waitForIdle()

        assertAt(CONTENT_TOP + ROW_GAP + Bar - BAR_OVERLAP, top(TvLayoutAnchors.hubRow(0)))
        assertAt(Bar - BAR_COPY_OVERLAP, top(TvLayoutAnchors.HUB_COPY))
        assertAt(CONTENT_TOP + Bar - BAR_OVERLAP - BAND_BOTTOM_UNDER_BAR, bottom(TvLayoutAnchors.HUB_COPY))
    }

    @Test
    fun `under a top bar the grid's first cell stays in step with the hub's rows`() {
        show { Grid() }

        inset = Bar
        rule.waitForIdle()

        assertAt(CONTENT_TOP + Bar - BAR_OVERLAP, top(TvLayoutAnchors.GRID_FIRST_CELL))
    }

    /** A bar shorter than the overlap moves nothing up: the rows sit no higher than they do with no bar. */
    @Test
    fun `under a short top bar the hub's rows sit no higher than with no bar`() {
        inset = ShortBar
        show { Hub() }

        assertAt(CONTENT_TOP + ROW_GAP, top(TvLayoutAnchors.hubRow(0)))
    }

    @Test
    fun `under a short top bar the grid's first cell sits no higher than with no bar`() {
        inset = ShortBar
        show { Grid() }

        assertAt(CONTENT_TOP, top(TvLayoutAnchors.GRID_FIRST_CELL))
    }

    /** The copy a hub draws, a meta line, a title, a status line and a synopsis, clears the bar with two synopsis lines. */
    @Test
    fun `under a top bar the copy clears the bar and two synopsis lines fit at the default font`() {
        inset = Bar
        show { Hub(copy = { BackdropCopy() }) }

        val copyTop = rule.onNodeWithTag(META, useUnmergedTree = true).getUnclippedBoundsInRoot().top
        assertTrue("the copy starts at $copyTop, under the bar's $Bar", copyTop >= Bar)
        assertEquals(SYNOPSIS_LINES, synopsisLines())
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

    private fun bottom(anchor: String): Dp = rule.onNodeWithTag(anchor, useUnmergedTree = true).getUnclippedBoundsInRoot().bottom

    private fun assertAt(expected: Dp, actual: Dp) = assertEquals(expected.value, actual.value, TOLERANCE)

    private fun synopsisLines(): Int {
        val layouts = mutableListOf<TextLayoutResult>()
        rule
            .onNodeWithTag(SYNOPSIS, useUnmergedTree = true)
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(layouts)
        return layouts.single().lineCount
    }
}

/** The band's dimens, as values, so a test fails when one moves rather than following it. */
private val OVERSCAN = 27.dp
private val CONTENT_TOP = 240.dp

/** The hub's first row starts one row gap below the content top: the hero slot ends there. */
private val ROW_GAP = 24.dp
private val BAND_BOTTOM = 24.dp
private val BAND_BOTTOM_UNDER_BAR = 12.dp
private val BAR_OVERLAP = 92.dp
private val BAR_COPY_OVERLAP = 8.dp

/** A hub's copy as the companion draws it: the synopsis takes the lines the band leaves it, up to two. */
@Composable
private fun ColumnScope.BackdropCopy() {
    Text("Movie · 2024 · PG-13", style = MaterialTheme.typography.titleMedium, maxLines = 1, modifier = Modifier.testTag(META))
    Text("A title", style = MaterialTheme.typography.displaySmall, maxLines = 1)
    Text("Pending · Scott · 2 days ago", style = MaterialTheme.typography.titleMedium, maxLines = 1)
    val style = MaterialTheme.typography.bodyMedium
    BoxWithConstraints(modifier = Modifier.weight(1f, fill = false)) {
        val lineHeight = with(LocalDensity.current) { style.lineHeight.toDp() }
        val lines = (maxHeight / lineHeight).toInt().coerceIn(0, SYNOPSIS_LINES)
        if (lines > 0) {
            Text(LONG_SYNOPSIS, style = style, maxLines = lines, overflow = TextOverflow.Ellipsis, modifier = Modifier.testTag(SYNOPSIS))
        }
    }
}

@Composable
private fun Hub(copy: @Composable ColumnScope.(Int) -> Unit = {}) {
    TvImmersiveHub(
        rows = Rows,
        itemId = { it },
        cardWidth = CardWidth,
        onItemClick = {},
        artwork = {},
        copy = copy,
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

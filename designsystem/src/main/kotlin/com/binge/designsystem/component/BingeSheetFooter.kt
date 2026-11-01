package com.binge.designsystem.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics

/**
 * Where a page's primary action goes. In the side sheet the footer is pinned to the foot of the sheet and the page scrolls
 * above it, so the action is in the same place on every page and in every state. Anywhere else, the bottom sheet included,
 * it is drawn in place, where the page put it.
 *
 * Call it from the page's content with the action, `BingeActionFooter` usually. At most one footer is pinned per page; the
 * last one composed wins.
 */
@Composable
fun BingeSheetFooter(content: @Composable () -> Unit) {
    val host = LocalSheetFooterHost.current
    if (host == null) {
        content()
        return
    }
    val current by rememberUpdatedState(content)
    val pinned = remember(host) { @Composable { current() } }
    // Handed over while composing, so the panel, which composes the footer after the page, has it on the first frame.
    // The field is state, so a footer registered later invalidates the panel.
    host.content = pinned
    DisposableEffect(host) {
        onDispose {
            if (host.content === pinned) {
                host.content = null
            }
        }
    }
}

/**
 * A vertical scroll for a page's content that the side sheet can see, so it knows when content is under the footer and draws
 * the edge. Outside the side sheet it is an ordinary `verticalScroll` with its own state.
 *
 * Use it on one node per page: inside the side sheet every call shares the one scroll state, so two scrolling nodes would
 * move together.
 */
@Composable
fun Modifier.bingeSheetScroll(): Modifier = verticalScroll(LocalSheetScroll.current ?: rememberScrollState())

/**
 * The footer a page handed to [BingeSheetFooter], held by the side sheet's panel; absent where nothing pins it. [content] is a
 * snapshot state, written as the page composes and read as the panel composes after it, so a footer that appears or goes
 * after the first frame recomposes the panel too.
 */
@Stable
internal class SheetFooterHost {
    var content by mutableStateOf<(@Composable () -> Unit)?>(null)
}

internal val LocalSheetFooterHost = compositionLocalOf<SheetFooterHost?> { null }

internal val LocalSheetScroll = compositionLocalOf<ScrollState?> { null }

/** Whether content is under the footer. A [ScrollState] reports `Int.MAX_VALUE` as its bound until its first layout. */
private fun ScrollState.hasContentUnderFooter(): Boolean = maxValue != Int.MAX_VALUE && canScrollForward

/** Read by tests: true while the edge above the pinned footer is showing. */
internal val SheetFooterEdgeShown = SemanticsPropertyKey<Boolean>("SheetFooterEdgeShown")
private var SemanticsPropertyReceiver.sheetFooterEdgeShown by SheetFooterEdgeShown

/**
 * The edge keeps its 1dp whether or not it shows, so the page does not shift as content scrolls under it; whether it
 * shows is read in the draw phase, so a scroll redraws it rather than recomposing the panel.
 */
private fun Modifier.sheetFooterEdge(scroll: ScrollState): Modifier =
    graphicsLayer { alpha = if (scroll.hasContentUnderFooter()) 1f else 0f }
        .semantics { sheetFooterEdgeShown = scroll.hasContentUnderFooter() }

/**
 * A side sheet page: [content] above, and the footer a page pinned below it. The content takes what the footer leaves and
 * scrolls in it; with no footer it takes the whole panel, as it always did. The edge above the footer shows only while the
 * content scrolls under it.
 */
@Composable
internal fun ColumnScope.SheetPageWithFooter(content: @Composable ColumnScope.() -> Unit) {
    val scroll = rememberScrollState()
    val host = remember { SheetFooterHost() }
    CompositionLocalProvider(
        LocalSheetFooterHost provides host,
        LocalSheetScroll provides scroll,
    ) {
        Column(modifier = Modifier.weight(1f)) { content() }
        host.content?.let { footer ->
            HorizontalDivider(
                modifier = Modifier.sheetFooterEdge(scroll),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Column(modifier = Modifier.fillMaxWidth()) { footer() }
        }
    }
}

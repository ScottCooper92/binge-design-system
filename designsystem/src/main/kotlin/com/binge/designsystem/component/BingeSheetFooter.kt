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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

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
    host.content = pinned
    DisposableEffect(host) {
        onDispose {
            if (host.content === pinned) {
                host.content = null
                host.version++
            }
        }
    }
}

/**
 * A vertical scroll for a page's content that the side sheet can see, so it knows when content is under the footer and draws
 * the edge. Outside the side sheet it is an ordinary `verticalScroll` with its own state.
 */
@Composable
fun Modifier.bingeSheetScroll(): Modifier = verticalScroll(LocalSheetScroll.current ?: rememberScrollState())

/**
 * The footer a page handed to [BingeSheetFooter], held by the side sheet's panel; absent where nothing pins it. [content] is a
 * plain field, written as the page composes and read as the panel composes after it; [version] is what tells the panel it was
 * taken away later.
 */
@Stable
internal class SheetFooterHost {
    var content: (@Composable () -> Unit)? = null
    var version by mutableIntStateOf(0)
}

internal val LocalSheetFooterHost = compositionLocalOf<SheetFooterHost?> { null }

internal val LocalSheetScroll = compositionLocalOf<ScrollState?> { null }

/** Tags the edge drawn above a pinned footer while content is under it, for a test to find. */
internal const val SHEET_FOOTER_EDGE_TAG = "sheet-footer-edge"

/**
 * A side sheet page: [content] above, and the footer a page pinned below it. The content takes what the footer leaves and
 * scrolls in it; with no footer it takes the whole panel, as it always did. The edge above the footer shows only while the
 * content scrolls under it.
 */
@Composable
internal fun ColumnScope.SheetPageWithFooter(content: @Composable ColumnScope.() -> Unit) {
    val scroll = rememberScrollState()
    val host = remember { SheetFooterHost() }
    // Read so a footer taken away after the first frame is noticed.
    host.version
    CompositionLocalProvider(
        LocalSheetFooterHost provides host,
        LocalSheetScroll provides scroll,
    ) {
        Column(modifier = Modifier.weight(1f)) { content() }
        host.content?.let { footer ->
            if (scroll.canScrollForward) {
                HorizontalDivider(modifier = Modifier.testTag(SHEET_FOOTER_EDGE_TAG), color = MaterialTheme.colorScheme.outlineVariant)
            }
            Column(modifier = Modifier.fillMaxWidth()) { footer() }
        }
    }
}

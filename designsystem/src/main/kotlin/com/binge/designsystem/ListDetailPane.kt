package com.binge.designsystem

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Provides [LocalPaneWidth] as the width [content] is actually measured at, rather than one derived
 * from a scene strategy's own preferred-width arithmetic — the list pane and the detail pane get
 * different real widths from the same directive, so only measuring what the host's `NavDisplay`
 * placed this entry's content at tells one pane's own width from another's.
 */
@Composable
fun PaneContent(content: @Composable () -> Unit) {
    BoxWithConstraints {
        CompositionLocalProvider(LocalPaneWidth provides maxWidth) {
            content()
        }
    }
}

/**
 * The back-navigation behavior a list-detail `SceneStrategy` should use, in place of the library's
 * own default (`PopUntilScaffoldValueChange`). That default pops repeatedly until the pane
 * configuration itself changes — but clearing a detail pane's selection back to its placeholder does
 * not change which pane roles are expanded, so it is not a "scaffold value change" and Back skips
 * straight past it. With the list pane at the root of a host's own back stack, that means Back walks
 * every screen the detail pane has ever shown and leaves the host's own section entirely on a single
 * press. [BackNavigationBehavior.PopLatest] pops exactly the one entry Back was pressed on, every time.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
val PaneBackNavigationBehavior: BackNavigationBehavior = BackNavigationBehavior.PopLatest

/**
 * Whether the current section is showing one pane at a time — phone width, or a list-detail scene's
 * single-pane fallback below its breakpoint — rather than the list and its selected detail side by
 * side. Defaults to `true` — a screen with no provider (a `@Preview`, or one that never sits in a
 * list-detail scene) behaves as it always did, with its own Back shown. Read through [paneBackOrNull]
 * rather than directly, in every case that applies to.
 */
val LocalIsSinglePaneNav = staticCompositionLocalOf { true }

/**
 * How many entries a host has stacked above its list pane's own root, for whatever is currently
 * showing in the detail pane beside it — 1 for a screen pushed directly from the list, more for one
 * nested further above that. The host provides its own live count per entry; a screen with no
 * provider (a `@Preview`, or one outside a list-detail pane) defaults to 1. Read through
 * [paneBackOrNull] rather than directly.
 */
val LocalPaneDepth = staticCompositionLocalOf { 1 }

/**
 * The one back-arrow rule for a list-detail pane, wherever it applies: hidden only when the list pane
 * is showing beside the detail pane ([hubBeside]) and this is the sole entry stacked above the list
 * root ([paneDepth] <= 1) — popping there would not return the viewer anywhere they came from, since
 * the list pane never left the screen. A screen stacked deeper always keeps its own Back: the pane
 * beside it is a parent screen, not a way back to itself, so depth alone tells a nested screen it
 * need not opt in — replacing the old marker-interface opt-out.
 */
fun paneShowsBack(hubBeside: Boolean, paneDepth: Int): Boolean = !hubBeside || paneDepth > 1

/**
 * [onBack] where [paneShowsBack] says this entry should carry its own Back, `null` where the list
 * pane beside it already offers one. Reads [LocalIsSinglePaneNav] and [LocalPaneDepth], both provided
 * per entry by the host.
 */
@Composable
fun paneBackOrNull(onBack: () -> Unit): (() -> Unit)? =
    onBack.takeIf { paneShowsBack(hubBeside = !LocalIsSinglePaneNav.current, paneDepth = LocalPaneDepth.current) }

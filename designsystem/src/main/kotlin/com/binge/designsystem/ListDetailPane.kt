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
 * [onBack] on a single pane, where a detail screen opened directly from a host's own list was pushed
 * full-screen and needs its own way back; `null` on two-pane, where it renders beside that list and a
 * second Back control would duplicate the one the list already offers.
 *
 * **Not for a screen nested a level deeper than that** — one pushed from within another detail screen
 * rather than from the host's own list directly (a specific list's own contents, say, pushed from a
 * list-of-lists screen rather than from the host's list pane). The pane beside a screen like that is
 * its *parent* screen, not a way back to itself, so it keeps its own Back unconditionally regardless
 * of pane mode.
 */
@Composable
fun paneBackOrNull(onBack: () -> Unit): (() -> Unit)? = onBack.takeIf { LocalIsSinglePaneNav.current }

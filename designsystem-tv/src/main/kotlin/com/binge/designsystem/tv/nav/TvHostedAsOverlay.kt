package com.binge.designsystem.tv.nav

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

/**
 * Whether the current TV content is hosted as a **full-screen overlay above the shell** (a detail page, a
 * sub-page) rather than inside [BingeTvNavRail]'s content pane. The outer overlay host provides
 * `true`; everywhere else it defaults to `false`.
 *
 * It exists because a single route/entry can be hosted **both** ways depending on how it was reached — e.g.
 * a page is in-pane when selected from the rail, but an overlay when pushed from another screen — so only the host can say which, not the content. Boards read it to choose their
 * overscan: in-pane the rail already offsets the start edge, but an overlay is panel-edge-to-panel-edge and
 * must carry overscan itself.
 *
 * A screen that is *always* overlay-hosted (a picker that is only ever pushed) passes its board `hostedAsOverlay = true`
 * explicitly, so its screenshot renders the real state without the host present; a dual-hosted board omits
 * it and inherits this local.
 */
val LocalTvHostedAsOverlay = staticCompositionLocalOf { false }

/**
 * PROTOTYPE: a page shown as a tab under a persistent bar. It is laid out as an overlay, full-screen with its own overscan, but
 * arrives like a destination: it never claims focus, so selecting the tab leaves focus on the tab.
 */
val LocalTvTabHosted = staticCompositionLocalOf { false }

/** PROTOTYPE: the height of a bar the host lays over the top of its pages, which a page adds to its top overscan. */
val LocalTvTopInset = compositionLocalOf { Dp(0f) }

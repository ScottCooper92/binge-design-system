package com.binge.designsystem.tv.nav

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

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
 * Whether the page is one tab of a host that keeps its tabs in a bar above the pages. Such a page is laid out as an
 * overlay, full-screen with its own overscan, but it never claims focus when it arrives: selecting a tab moves to its
 * page and leaves focus on the tab, and the user steps down into the page. Default `false`.
 */
val LocalTvTabHosted = staticCompositionLocalOf { false }

/**
 * The height of a bar the host lays over the top of its pages, such as a row of tabs. Every page template adds it to
 * its top overscan, so its first line clears the bar. The host provides the bar's measured height; it is 0 without one.
 */
val LocalTvTopInset = compositionLocalOf { 0.dp }

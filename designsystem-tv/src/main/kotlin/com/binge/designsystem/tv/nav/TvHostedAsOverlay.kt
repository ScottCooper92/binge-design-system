package com.binge.designsystem.tv.nav

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether the current TV content is hosted as a **full-screen overlay above the shell** (a media detail, a
 * Settings sub-page) rather than inside [BingeTvNavRail]'s content pane. The outer overlay host provides
 * `true`; everywhere else it defaults to `false`.
 *
 * It exists because a single route/entry can be hosted **both** ways depending on how it was reached — e.g.
 * `TvSettingsRoute` is in-pane when selected from the rail's Settings item, but an overlay when pushed from
 * the Account screen — so only the host can say which, not the content. Boards read it to choose their
 * overscan: in-pane the rail already offsets the start edge, but an overlay is panel-edge-to-panel-edge and
 * must carry overscan itself — a regression the overlay host introduced.
 *
 * A screen that is *always* overlay-hosted (Watch providers) passes its board `hostedAsOverlay = true`
 * explicitly, so its screenshot renders the real state without the host present; a dual-hosted board omits
 * it and inherits this local.
 */
val LocalTvHostedAsOverlay = staticCompositionLocalOf { false }

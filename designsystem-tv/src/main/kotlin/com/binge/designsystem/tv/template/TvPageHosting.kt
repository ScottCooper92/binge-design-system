package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.tv.focus.TvArrivalFocus
import com.binge.designsystem.tv.focus.TvArrivalFocusEffect
import com.binge.designsystem.tv.focus.TvOverlayArrivalFocusEffect
import com.binge.designsystem.tv.focus.tvArrivalObserver
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.tv.nav.LocalTvHostedAsOverlay
import com.binge.designsystem.tv.nav.LocalTvTabHosted
import com.binge.designsystem.tv.nav.LocalTvTopInset
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.R as TvR

/**
 * How a TV page is shown, which decides two things a template owns: how far its start edge clears the panel,
 * and who places first focus on it. The arrival rules are `docs/tv-foundation.md`'s: a destination never
 * requests focus, an overlay must.
 */
enum class TvPageHosting {
    /**
     * In a shell's content pane, a destination of its navigation. The shell offers focus; the page only routes it to
     * its entry target.
     */
    Destination,

    /** Full-screen above the shell. The page claims focus itself, with one request once its target has attached. */
    Overlay,

    /** Before the shell exists — onboarding, a gate. The page places first focus once it has laid out. */
    PreShell,
}

/** What [LocalTvHostedAsOverlay] says the host is: an overlay, or a shell destination. */
@Composable
@ReadOnlyComposable
fun currentTvPageHosting(): TvPageHosting = if (LocalTvHostedAsOverlay.current) TvPageHosting.Overlay else TvPageHosting.Destination

/**
 * The overscan-safe padding for a page hosted as [hosting]. In-pane the start edge clears the host's start inset plus
 * the content gutter; anywhere else nothing sits beside the page, so every edge takes the overscan margin.
 */
@Composable
fun tvPagePadding(hosting: TvPageHosting): PaddingValues {
    val horizontal = dimensionResource(TvR.dimen.tv_overscan_horizontal)
    val vertical = dimensionResource(TvR.dimen.tv_overscan_vertical)
    return PaddingValues(
        start = if (hosting == TvPageHosting.Destination) tvContentGutterStart() else horizontal,
        top = vertical + LocalTvTopInset.current,
        end = horizontal,
        bottom = vertical,
    )
}

/**
 * The page root's focus wiring for [hosting]: entry routed to [entry] in every mode, plus the arrival effect
 * the mode calls for. [enabled] holds the claim back until the target exists; [key] re-places an overlay's or
 * pre-shell page's focus when what it shows changes (the next step).
 */
@Composable
internal fun tvPageArrival(
    hosting: TvPageHosting,
    entry: FocusRequester?,
    enabled: Boolean,
    key: Any?,
): Modifier {
    if (entry == null) return Modifier
    return when (hosting) {
        TvPageHosting.Destination -> Modifier.tvEntryFocusGroup(entry)
        TvPageHosting.Overlay -> {
            // A tab's page is entered from its tab, so it claims no focus of its own: that would pull focus off the tab.
            if (LocalTvTabHosted.current) return Modifier.tvEntryFocusGroup(entry)
            // The page group is already composed when the key changes, so its observer does not report again for the new holder.
            val arrival = remember(entry, key) { TvArrivalFocus(entry).also { it.attached = true } }
            TvArrivalFocusEffect(arrival, enabled)
            Modifier.tvArrivalObserver(arrival).tvEntryFocusGroup(entry)
        }
        TvPageHosting.PreShell -> {
            if (enabled) TvOverlayArrivalFocusEffect(entry, key)
            Modifier.tvEntryFocusGroup(entry)
        }
    }
}

package com.binge.designsystem.tv.nav

import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester

/*
 * These are retry *counts*, not durations. `withFrameNanos` resumes on the next frame the app produces, and a
 * TV app waiting on a query produces very few (measured: a drill-down spent three retries on a two-second
 * provider list), so a count buys an unknown amount of wall-clock time. That's mostly in our favour — a frame
 * arrives precisely when content changes — but it also means these numbers can't be tuned into a fix: an
 * earlier attempt widened the window five-fold to no effect, because the window was never the problem.
 */

/**
 * Startup budget: enough frames for a first screen to arrive and claim focus from `TvFocusSink` (#2523). No
 * longer a retry-until-we-must-bail window — the sink means the loop's `taken()` check is satisfied within a
 * frame or two of any destination, real or still-loading, so this is a cold-start sanity bound rather than
 * something a destination is expected to run out.
 */
internal const val FOCUS_HANDOFF_FRAMES = 120

/**
 * Drill-down budget, deliberately generous: the loop exits the moment focus is taken, and there is no fallback
 * on this path, so giving up early is the only visible failure.
 */
internal const val CONTENT_HANDOFF_FRAMES = 600

/**
 * The drill-down handoff's outer bound, measured in time rather than frames because it is the only budget that
 * can expire on a destination that never redraws. A destination with nothing focusable can never satisfy the
 * handoff, and a static "coming soon" pane draws almost no frames, so a frame count would barely advance while
 * suppressing the rail's expansion (see `isExpanded`) for the user's whole visit. Sized to clear the slowest
 * real handoff measured — just under two seconds, a cold watch-provider list.
 */
internal const val CONTENT_HANDOFF_TIMEOUT_MS = 3_000L

/**
 * Offer focus to the content group each frame until something takes it, then stop.
 *
 * Retried, not fired once: a still-loading destination has only a placeholder and no focus target, and a
 * request on a group with no focusable child fails *silently*. This is every cold start, not an edge case.
 *
 * [yieldToRail] separates the two callers: at startup a user who reaches the rail first must keep focus, so
 * the loop gives up; after a drill-down it must not, since Compose has already parked focus in the rail — the
 * state being corrected.
 */
internal suspend fun offerFocusToContent(
    contentFocus: FocusRequester,
    yieldToRail: () -> Boolean,
    taken: () -> Boolean,
    frames: Int = FOCUS_HANDOFF_FRAMES,
) {
    repeat(frames) {
        if (taken() || yieldToRail()) return
        runCatching { contentFocus.requestFocus() }
        withFrameNanos { }
    }
}

/**
 * Wait for the content to actually *lose* focus, returning whether it did within the window.
 *
 * A drill-down's focus loss hasn't happened yet when the navigation does: the outgoing screen stays composed
 * and focused until its exit transition finishes (measured: 890ms default, 436ms after `tvContentSwap`'s
 * fade). So at the frame the back stack changes, `contentHasFocus` still reads the *old* screen and wrongly
 * concludes focus was handed over — the bug that made two earlier attempts no-ops. Hence sequenced, not raced:
 * a `false` return means the content held focus throughout, so nothing was stranded.
 */
internal suspend fun awaitContentFocusLost(contentHasFocus: () -> Boolean): Boolean {
    repeat(FOCUS_HANDOFF_FRAMES) {
        if (!contentHasFocus()) return true
        withFrameNanos { }
    }
    return false
}

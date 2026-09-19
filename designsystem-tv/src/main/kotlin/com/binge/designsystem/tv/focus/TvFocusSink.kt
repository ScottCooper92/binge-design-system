package com.binge.designsystem.tv.focus

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import com.binge.designsystem.tv.R as TvR

/** Test tag baked into every [TvFocusSink] — there is no `Modifier` parameter for a caller to tag it with. */
const val TV_FOCUS_SINK_TAG = "tv-focus-sink"

/**
 * A passive, inert focus target for a pane with nothing else focusable yet (#2518). A caller composes it
 * conditionally — `if (!paneHasFocus) TvFocusSink(...)` — as a sibling of the pane's real content, never inside
 * a scrollable, so the pane's own entry requester always has *something* to land on from its first frame
 * instead of racing the destination's first focusable child. **Gate presence on the pane's own `hasFocus`, not
 * on a flag scoped to the sink alone**: the sink holding focus is itself "the pane has focus", so the next
 * recomposition removes it — and removing an already-focused node runs Compose's own recovery search, which is
 * exactly how the loop that placed the sink gets a fresh, unsatisfied entry to retry into once the destination
 * grows something real. Gating out that removal (tried, then reverted — see git history) leaves the sink an
 * inert focus the destination's own content can never displace, since re-requesting the pane's entry while a
 * descendant already holds it is a no-op, not a re-evaluation.
 *
 * **Never calls `requestFocus` itself.** It only receives what a group's own entry redirect, an explicit
 * request, or Compose's own recovery search hands it — a puller here would reproduce the rail collapsing under
 * a user still walking it (#1335). [leftEntry] is the one sanctioned way out by D-pad: the content pane passes
 * its rail's selected-item requester so ← still opens the rail; the overlay host leaves it `null`, since Back
 * is the only way out of an overlay. Every other direction is cancelled, so a stray press during a skeleton
 * does nothing rather than landing on a geometric pick behind the pane.
 *
 * 1dp and fully transparent rather than zero-size: some Compose versions drop zero-size nodes from directional
 * search, so this stays a real laid-out node. No `Modifier` parameter — the geometry, alpha and semantics here
 * are the whole contract, not something a call site should be able to override.
 */
@Composable
fun TvFocusSink(leftEntry: FocusRequester? = null) {
    Box(
        Modifier
            .testTag(TV_FOCUS_SINK_TAG)
            .size(dimensionResource(TvR.dimen.tv_focus_sink_size))
            .alpha(0f)
            .focusProperties {
                left = leftEntry ?: FocusRequester.Cancel
                up = FocusRequester.Cancel
                down = FocusRequester.Cancel
                right = FocusRequester.Cancel
            }.focusable()
            .clearAndSetSemantics { hideFromAccessibility() },
    )
}

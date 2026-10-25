package com.binge.designsystem.tv.focus

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.unit.LayoutDirection
import com.binge.designsystem.tv.R as TvR

/** Test tag baked into every [TvFocusSink] — there is no `Modifier` parameter for a caller to tag it with. */
const val TV_FOCUS_SINK_TAG = "tv-focus-sink"

/**
 * A passive, inert focus target for a pane with nothing else focusable yet. A caller composes it as a sibling of the
 * pane's real content, never inside a scrollable, so the pane's own entry requester always has *something* to land on
 * from its first frame instead of racing the destination's first focusable child.
 *
 * **Keep it mounted while it holds focus, and hand focus on explicitly.** Gate its presence on the pane's *real*
 * content having focus, not on the pane's own `hasFocus`, which the sink itself satisfies. Removing a sink that holds
 * focus leaves Compose's recovery search to place it, and with nothing else in the pane that is whatever sits beside
 * it: in a TV shell, its navigation, every other frame for as long as the pane stays empty. Instead, while the sink
 * holds focus, offer it to a group around the real content: a request into a group with nothing focusable fails
 * quietly and leaves focus where it is, so the offer can repeat until the content grows a target.
 *
 * **Never calls `requestFocus` itself.** It only receives what a group's own entry redirect, an explicit
 * request, or Compose's own recovery search hands it — a puller here would take focus from a user still walking
 * the navigation. [startEntry] is the one sanctioned way out by D-pad: a host with navigation at the layout's
 * start edge passes that navigation's requester, so the key toward the start edge reaches it — ← under LTR,
 * → under RTL. Leave it `null` where there is no such navigation: in an overlay, Back is the only way out.
 * Every other direction is cancelled, so a stray press during a skeleton does nothing rather than landing on a
 * geometric pick behind the pane.
 *
 * 1dp and fully transparent rather than zero-size: some Compose versions drop zero-size nodes from directional
 * search, so this stays a real laid-out node. No `Modifier` parameter — the geometry, alpha and semantics here
 * are the whole contract, not something a call site should be able to override.
 */
@Composable
fun TvFocusSink(startEntry: FocusRequester? = null) {
    // Focus directions are physical, so the start edge's key is picked from the layout direction here.
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val exit = startEntry ?: FocusRequester.Cancel
    Box(
        Modifier
            .testTag(TV_FOCUS_SINK_TAG)
            .size(dimensionResource(TvR.dimen.tv_focus_sink_size))
            .alpha(0f)
            .focusProperties {
                left = if (rtl) FocusRequester.Cancel else exit
                up = FocusRequester.Cancel
                down = FocusRequester.Cancel
                right = if (rtl) exit else FocusRequester.Cancel
            }.focusable()
            .clearAndSetSemantics { hideFromAccessibility() },
    )
}

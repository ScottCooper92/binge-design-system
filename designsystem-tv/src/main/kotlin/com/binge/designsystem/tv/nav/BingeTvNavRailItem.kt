package com.binge.designsystem.tv.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.component.NavSuiteBadge
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.BingeTvInitialsAvatar
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusContentColor
import com.binge.designsystem.tv.focus.tvFocusFill
import com.binge.designsystem.tv.R as TvR

/*
 * One entry in the nav rail — its visuals, and the rule for when focus becomes a selection. Split from the
 * rail because the two answer different questions: the rail owns *where focus is*, an item owns *what focus
 * means* (an amber treatment, and at most one commit).
 */

/**
 * A focus-owning rail item; the stateless [RailItemSurface] is what a screenshot renders.
 *
 * Selecting on focus rather than on click is the rail's whole interaction model — see [BingeTvNavRail].
 */
@Composable
internal fun RailItem(
    item: TvNavRailItem,
    selectedKey: Any?,
    expanded: Boolean,
    onSelect: (Any) -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = expanded,
) {
    var focused by remember { mutableStateOf(false) }
    val selected = item.key == selectedKey
    RailItemSurface(
        item = item,
        selected = selected,
        expanded = expanded,
        isFocused = focused,
        showLabel = showLabel,
        modifier = modifier
            .tvClickable(
                // Focus commits a *change* of selection, never a restatement — the guard that keeps a drill-down alive. A forward
                // navigation disposes the control that caused it and Compose parks focus here via the `onEnter` redirect; committing
                // that would pick a rail destination and drop the route asked for (measured: "see all" opened the *unfiltered* grid).
                onFocusChanged = { gained ->
                    focused = gained
                    if (gained && !selected) onSelect(item.key)
                },
                // Unguarded, unlike focus: pressing OK on the highlighted item is an explicit ask for that
                // destination's root, which is a legitimate way out of a drill-down.
                onClick = { onSelect(item.key) },
            ).semantics {
                role = Role.Tab
                this.selected = selected
            },
    )
}

/**
 * The stateless item visuals. [selected] and [isFocused] coincide here (focus selects), so both drive one
 * treatment: an amber fill with `onPrimary` content while [expanded], and the amber accent on the bare icon
 * while collapsed. A collapsed strip gets no fill — a filled pill behind a lone icon reads as a button. The
 * label is shown per [showLabel], separate from [expanded]: while the rail is animating open it is held back
 * until the panel is wide enough to hold the label, so the label pops in rather than un-clipping left-to-right.
 *
 * **The rail deliberately does not adopt `TvRowEmphasis`:** its two tiers already express a
 * current-but-unfocused treatment as fill-versus-tint (expanded → full amber fill; collapsed → amber icon, no
 * fill). A dim-amber fill on top would give the collapsed strip the filled pill ruled out above and be a third
 * mark for a state already marked. There is no `Resting`/`Current` ambiguity to resolve: because the rail
 * selects on focus, "current" and "focused" never disagree while the rail has focus, so [current] as
 * one flag is honest rather than overloaded. `docs/tv-foundation.md` records this.
 */
@Composable
internal fun RailItemSurface(
    item: TvNavRailItem,
    selected: Boolean,
    expanded: Boolean,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    showLabel: Boolean = expanded,
) {
    // Current-or-focused (they coincide) reads as amber; expanded it becomes a fill, so the content
    // flips to onPrimary to stay legible on it.
    val current = selected || isFocused
    val filled = current && expanded
    // onSurface, not onSurfaceVariant: the resting glass fill (RAIL_SCRIM_ALPHA/RAIL_SCRIM_HOLD_ALPHA in
    // BingeTvNavRail) sits low enough over bright artwork that onSurfaceVariant fails the 3:1 icon bar. The
    // cost is the resting row's own hierarchy — carried afterwards by the current row's primary and the
    // focused row's tvFocusFill.
    val contentColor = tvFocusContentColor(
        isFocused = filled,
        resting = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    )
    // [BingeShapes.TvListItem], shared with the filter panel's rows on purpose: the rail and those rows are the
    // app's two vertical 10-foot lists, so a corner difference would read as inconsistency. The token makes
    // "same shape" checkable, since tv-material's `ListItem` doesn't expose its default as a `Shape` to copy.
    Row(
        modifier = modifier
            .fillMaxWidth()
            // Fixed height so the label appearing can't grow the row and jog the rows below during the slide.
            .height(dimensionResource(TvR.dimen.tv_nav_rail_item_height))
            .clip(BingeShapes.TvListItem)
            .tvFocusFill(isFocused = filled, shape = BingeShapes.TvListItem)
            .padding(horizontal = dimensionResource(TvR.dimen.tv_nav_rail_item_padding_h)),
        verticalAlignment = Alignment.CenterVertically,
        // Start-aligned in both states, read as centred because `tv_nav_rail_collapsed_width` leaves the same 24dp after
        // the glyph that the padding puts before it. Centring instead looks right at rest but wrong in motion: against an
        // animating width the icon slides left as the rail closes. Pinned to the start, only the label animates.
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_nav_rail_icon_label_gap)),
    ) {
        Box {
            val avatarName = item.displayName
            if (avatarName != null) {
                BingeTvInitialsAvatar(
                    name = avatarName,
                    avatarUrl = item.avatarUrl,
                    size = dimensionResource(TvR.dimen.tv_nav_rail_avatar_size),
                )
            } else {
                Icon(imageVector = item.icon, contentDescription = null, tint = contentColor)
            }
            val badge = item.badge
            if (badge is NavSuiteBadge.Label) {
                RailItemBadge(
                    text = badge.text,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(
                            x = dimensionResource(TvR.dimen.tv_nav_rail_badge_offset),
                            y = -dimensionResource(TvR.dimen.tv_nav_rail_badge_offset),
                        ),
                )
            }
        }
        if (showLabel) {
            // Ellipsised rather than wrapped or measured: the rail's width is fixed, so a long label
            // truncates instead of deciding how much of the screen the sidebar takes.
            Text(text = item.label, color = contentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/**
 * The rail's [NavSuiteBadge.Label] treatment: a fixed-colour pill (never the item's amber accent, which a
 * badge must stay legible against in either state) overlaid on the icon or avatar's corner. Sized by content
 * rather than pinned to a circle, so a two-digit count doesn't clip the way [tv_nav_rail_badge_size] alone
 * would.
 */
@Composable
private fun RailItemBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .defaultMinSize(
                minWidth = dimensionResource(TvR.dimen.tv_nav_rail_badge_size),
                minHeight = dimensionResource(TvR.dimen.tv_nav_rail_badge_size),
            ).clip(BingeShapes.Pill)
            .background(MaterialTheme.colorScheme.error)
            .padding(horizontal = dimensionResource(TvR.dimen.tv_nav_rail_badge_padding_h)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = MaterialTheme.colorScheme.onError, style = MaterialTheme.typography.labelSmall)
    }
}

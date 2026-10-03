package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

/** Where a [ListItem] takes the user, and so which trailing glyph promises the right thing. */
enum class ListItemDestination {
    /** Pushes a screen inside the app, or opens a sheet; Back returns here. The chevron. */
    InApp,

    /** Leaves the app — a browser, another app. The row says so before it's tapped, not after. */
    External,
}

/**
 * How a [ListItem] joins the row above it as a child of that row: a line down the parent icon's
 * axis that curves into this row, drawn on the row itself so it spans the row's full height.
 */
enum class ListItemConnector {
    /** The line curves into this row and carries on down to the next sibling. */
    Continue,

    /** The last sibling: the line stops at the curve, an L rather than a T. */
    End,
}

/** One row of a [ItemGroup]: an icon in a tinted box, a label with an optional detail line, and a trailing slot. */
@Immutable
data class ListItem(
    val icon: ImageVector,
    /**
     * Rendered in place of [icon] when set, for a glyph the caller resolves itself — one loaded from
     * another package's resources, say — so the row shows it without this module knowing its source.
     */
    val iconPainter: (@Composable () -> Painter)? = null,
    /** Tint for the icon box wash and glyph; defaults to the accent on a neutral box. */
    val iconTint: Color? = null,
    val label: String,
    val detail: String? = null,
    /** Tint for [detail]; defaults to the muted caption colour. Use the error colour for problems. */
    val detailColor: Color? = null,
    /** A count badge shown before the trailing chevron; omitted when null or not positive. */
    val badgeCount: Int? = null,
    /** With [badgeCount], renders the count as a tonal pill in this colour instead of the default badge. */
    val badgeTint: Color? = null,
    /**
     * An uncounted "needs you" badge ("!") in the badge slot, for something that wants attention but has
     * nothing to count — a lapsed sign-in, say. Takes the slot over [badgeCount] when both are set.
     */
    val badgeAlert: Boolean = false,
    /**
     * Marks the row as the one currently open, for a group used as a navigation list beside a
     * detail pane. It is a wash and a semantics flag, not a substitute for the chevron: the row
     * still opens the same thing when tapped.
     */
    val selected: Boolean = false,
    val clickable: Boolean = true,
    /** Only read when [trailingContent] is null — a caller supplying its own trailing slot owns this too. */
    val destination: ListItemDestination = ListItemDestination.InApp,
    val trailingContent: (@Composable () -> Unit)? = null,
    val onClick: () -> Unit = {},
    val onLongClick: (() -> Unit)? = null,
    /** Rendered in place of the icon box when set, for a leading visual that is not a glyph — an avatar, say. */
    val leadingContent: (@Composable () -> Unit)? = null,
    /**
     * A call this row started is in flight: a small spinner takes the trailing slot, a screen reader hears
     * that it is in progress, and the row stops taking taps. It is not dimmed — it is the row doing the work.
     */
    val loading: Boolean = false,
    /** Joins the row to a parent row above it; the icon box then follows a connector-width inset. */
    val connector: ListItemConnector? = null,
    /** Dimmed and inert, for a row that cannot be used because something else is in flight. */
    val disabled: Boolean = false,
    /** What a long-press does, announced by TalkBack ("double tap and hold to …"). Only read with [onLongClick]. */
    val onLongClickLabel: String? = null,
    /**
     * Makes this a switch row: non-null is the switch's on/off state, null (the default) a plain button row.
     * The whole row is then one `Role.Switch` node carrying a [androidx.compose.ui.state.ToggleableState], so a
     * screen reader announces "Label, Switch, On" rather than "Label, Button". A tap calls [onClick], which
     * should flip the state the caller owns. Unless [trailingContent] is set, the row draws the switch itself
     * and no chevron. A switch row has no long-press; [onLongClick] is ignored.
     */
    val toggled: Boolean? = null,
)

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeInitialsAvatar
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.ItemRows
import com.binge.designsystem.component.ListItem
import com.binge.designsystem.component.ListItemConnector
import com.binge.designsystem.component.ListItemDestination
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeSentiment
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.accent
import com.binge.designsystem.theme.tonalContainer

/**
 * Public samples for [ItemGroup] — a clipped card of list items with dividers: a titled group
 * of plain rows, and an untitled group with tinted icons and count badges. See the convention KDoc
 * on [MediaCardRatedSample].
 */
@Composable
fun ItemGroupTitledSample() {
    ScreenshotTheme {
        ItemGroup(
            title = "My library",
            rows = listOf(
                ListItem(icon = Icons.Filled.Bookmark, label = "Watchlist", detail = "42 titles"),
                ListItem(icon = Icons.Filled.Bookmark, label = "Watched", detail = "186 titles"),
                ListItem(icon = Icons.Filled.Settings, label = "Preferences"),
            ),
        )
    }
}

/** An untitled group whose rows carry sentiment-tinted icons and trailing count badges. */
@Composable
fun ItemGroupTintedSample() {
    ScreenshotTheme {
        ItemGroup(
            title = null,
            rows = listOf(
                ListItem(
                    icon = Icons.Filled.Inbox,
                    iconTint = BingeSentiment.Caution.accent(),
                    label = "Requests",
                    detail = "Approve, decline & track",
                    badgeCount = 3,
                    badgeTint = BingeSentiment.Caution.accent(),
                ),
                ListItem(
                    icon = Icons.Filled.People,
                    iconTint = BingeSentiment.Info.accent(),
                    label = "Users",
                    detail = "Roles, quotas & permissions",
                    badgeCount = 8,
                    badgeTint = BingeSentiment.Neutral.accent(),
                ),
            ),
        )
    }
}

/**
 * The two badge forms on the default badge: a count, and the uncounted alert for something that
 * wants the user with nothing to count. Each sits before the row's own trailing control.
 */
@Composable
fun ItemGroupBadgesSample() {
    ScreenshotTheme {
        ItemGroup(
            title = "Companion apps",
            rows = listOf(
                ListItem(
                    icon = Icons.Filled.Inbox,
                    label = "Requests",
                    detail = "2 waiting for you",
                    badgeCount = 2,
                ),
                ListItem(
                    icon = Icons.Filled.People,
                    label = "Accounts",
                    detail = "Sign in again to keep syncing",
                    badgeAlert = true,
                ),
            ),
        )
    }
}

/**
 * A group standing in as a navigation list beside a detail pane: the open row carries the selected
 * wash, the rest do not. The wash is the only difference — the row keeps its chevron and its badge.
 */
@Composable
fun ItemGroupSelectedSample() {
    // Seeded on Requests; tapping a row opens it, as a navigation list beside a pane would.
    var open by remember { mutableStateOf("Requests") }
    ScreenshotTheme {
        ItemGroup(
            title = "Manage",
            rows = listOf(
                ListItem(
                    icon = Icons.Filled.Inbox,
                    iconTint = BingeSentiment.Caution.accent(),
                    label = "Requests",
                    detail = "Approve, decline & track",
                    badgeCount = 3,
                    badgeTint = BingeSentiment.Caution.accent(),
                    selected = open == "Requests",
                    onClick = { open = "Requests" },
                ),
                ListItem(
                    icon = Icons.Filled.People,
                    iconTint = BingeSentiment.Info.accent(),
                    label = "Users",
                    detail = "Roles, quotas & permissions",
                    selected = open == "Users",
                    onClick = { open = "Users" },
                ),
                ListItem(
                    icon = Icons.Filled.Settings,
                    label = "Settings",
                    detail = "Server & connection",
                    selected = open == "Settings",
                    onClick = { open = "Settings" },
                ),
            ),
        )
    }
}

/**
 * A row that leaves the app carries ↗ instead of the chevron (#51) — shown beside an ordinary
 * in-app row so the two read as distinct promises at a glance.
 */
@Composable
fun ItemGroupExternalSample() {
    ScreenshotTheme {
        ItemGroup(
            title = "About",
            rows = listOf(
                ListItem(icon = Icons.Filled.Settings, label = "Preferences"),
                ListItem(
                    icon = Icons.Filled.Policy,
                    label = "Privacy policy",
                    destination = ListItemDestination.External,
                ),
            ),
        )
    }
}

/**
 * Rows that act where they are carry no chevron and read as buttons: an ordinary action in the primary colour, one
 * running with its spinner, and a destructive one in its icon's red, beside an in-app row for contrast.
 */
@Composable
fun ItemGroupActionSample() {
    ScreenshotTheme {
        ItemGroup(
            title = "Libraries",
            rows = listOf(
                ListItem(icon = Icons.Filled.Folder, label = "Library settings"),
                ListItem(icon = Icons.Filled.Sync, label = "Sync libraries", destination = ListItemDestination.Action),
                ListItem(
                    icon = Icons.Filled.Refresh,
                    label = "Start a full scan",
                    detail = "Scanning 3 of 12",
                    loading = true,
                    destination = ListItemDestination.Action,
                ),
                ListItem(
                    icon = Icons.Filled.Delete,
                    iconTint = MaterialTheme.colorScheme.error,
                    label = "Delete the library",
                    destination = ListItemDestination.Action,
                ),
            ),
        )
    }
}

/**
 * The same rows as [ItemGroupTitledSample] with [ItemGroup]'s `rowVerticalPadding` raised
 * one rung on the padding ramp, for a caller whose rows are its main content (#133).
 */
@Composable
fun ItemGroupTallRowsSample() {
    ScreenshotTheme {
        ItemGroup(
            title = "My library",
            rows = listOf(
                ListItem(icon = Icons.Filled.Bookmark, label = "Watchlist", detail = "42 titles"),
                ListItem(icon = Icons.Filled.Bookmark, label = "Watched", detail = "186 titles"),
                ListItem(icon = Icons.Filled.Settings, label = "Preferences"),
            ),
            rowVerticalPadding = dimensionResource(R.dimen.padding_m),
        )
    }
}

/**
 * The same rows as [ItemGroupTitledSample] with [ItemGroup]'s `titleSpacing` dropped one
 * rung on the padding ramp, for a sheet whose titled groups sit close together.
 */
@Composable
fun ItemGroupTightTitleSample() {
    ScreenshotTheme {
        ItemGroup(
            title = "My library",
            rows = listOf(
                ListItem(icon = Icons.Filled.Bookmark, label = "Watchlist", detail = "42 titles"),
                ListItem(icon = Icons.Filled.Bookmark, label = "Watched", detail = "186 titles"),
                ListItem(icon = Icons.Filled.Settings, label = "Preferences"),
            ),
            titleSpacing = dimensionResource(R.dimen.padding_xs),
        )
    }
}

/**
 * [ListItem.leadingContent] standing in for the icon box: a user list, each row led by an avatar
 * rather than a glyph. `icon` is still supplied (the parameter has no default) but never drawn,
 * since [leadingContent] wins when set.
 */
@Composable
fun ItemGroupLeadingContentSample() {
    ScreenshotTheme {
        ItemGroup(
            title = "Users",
            rows = listOf(
                ListItem(
                    icon = Icons.Filled.People,
                    leadingContent = {
                        BingeInitialsAvatar(
                            name = "Ada Lovelace",
                            size = dimensionResource(R.dimen.item_group_icon_size),
                        )
                    },
                    label = "Ada Lovelace",
                    detail = "Owner",
                ),
                ListItem(
                    icon = Icons.Filled.People,
                    leadingContent = {
                        BingeInitialsAvatar(
                            name = "Grace Hopper",
                            size = dimensionResource(R.dimen.item_group_icon_size),
                        )
                    },
                    label = "Grace Hopper",
                    detail = "Can request",
                ),
            ),
        )
    }
}

/**
 * A call in flight from one row: it swaps its chevron for a spinner, and the rows that would start
 * another call are dimmed and inert. A row that does not touch the same resource, here the link out,
 * stays live.
 */
@Composable
fun ItemGroupBusySample() {
    ScreenshotTheme {
        ItemGroup(
            title = "Actions",
            rows = listOf(
                ListItem(
                    icon = Icons.Filled.Policy,
                    label = "Open on server",
                    destination = ListItemDestination.External,
                ),
                ListItem(icon = Icons.Filled.Flag, label = "Report an issue", disabled = true),
                ListItem(
                    icon = Icons.Filled.Block,
                    iconTint = BingeSentiment.Negative.accent(),
                    label = "Block",
                    loading = true,
                ),
            ),
        )
    }
}

/**
 * [ItemRows] with no surface of its own, drawn inside a card the caller supplies — the request
 * quota readout living on the account card, rather than [ItemGroup]'s own clipped surface.
 */
@Composable
fun ItemRowsNoSurfaceSample() {
    ScreenshotTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BingeShapes.Large)
                .background(BingeSentiment.Info.accent().tonalContainer())
                .padding(vertical = dimensionResource(R.dimen.padding_s)),
        ) {
            ItemRows(
                rows = listOf(
                    ListItem(icon = Icons.Filled.Inbox, label = "Request quota", detail = "8 of 10 used this month"),
                    ListItem(icon = Icons.Filled.Inbox, label = "Resets", detail = "In 12 days", clickable = false),
                ),
            )
        }
    }
}

/**
 * A parent row with its children joined to it by [ListItem.connector]: the first children
 * continue the line, the last one stops at the curve. Each child keeps its own icon box.
 */
@Composable
fun ItemGroupConnectorSample() {
    ScreenshotTheme {
        ItemGroup(
            title = null,
            rows = listOf(
                ListItem(icon = Icons.Filled.Tune, label = "Advanced options"),
                ListItem(
                    icon = Icons.Filled.Dns,
                    label = "Server",
                    detail = "Home",
                    connector = ListItemConnector.Continue,
                ),
                ListItem(
                    icon = Icons.Filled.HighQuality,
                    label = "Quality profile",
                    detail = "HD-1080p",
                    connector = ListItemConnector.Continue,
                ),
                ListItem(
                    icon = Icons.Filled.Folder,
                    label = "Root folder",
                    detail = "/media/movies",
                    connector = ListItemConnector.End,
                ),
            ),
        )
    }
}

/** Switch rows ([ListItem.toggled]): on, off and disabled, each drawing its own switch and no chevron. */
@Composable
fun ItemGroupSwitchSample() {
    ScreenshotTheme {
        ItemGroup(
            title = "Network",
            rows = listOf(
                ListItem(
                    icon = Icons.Filled.Dns,
                    label = "Trust proxy",
                    detail = "Read the client address from headers",
                    toggled = true,
                ),
                ListItem(icon = Icons.Filled.Policy, label = "Force IPv4", toggled = false),
                ListItem(icon = Icons.Filled.Block, label = "CSRF protection", toggled = true, disabled = true),
            ),
        )
    }
}

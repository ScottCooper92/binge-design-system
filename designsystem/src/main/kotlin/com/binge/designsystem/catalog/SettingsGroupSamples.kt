package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeInitialsAvatar
import com.binge.designsystem.component.SettingsGroup
import com.binge.designsystem.component.SettingsRow
import com.binge.designsystem.component.SettingsRowDestination
import com.binge.designsystem.component.SettingsRows
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeSentiment
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.accent
import com.binge.designsystem.theme.tonalContainer

/**
 * Public samples for [SettingsGroup] — a clipped card of settings rows with dividers: a titled group
 * of plain rows, and an untitled group with tinted icons and count badges. See the convention KDoc
 * on [MediaCardRatedSample].
 */
@Composable
fun SettingsGroupTitledSample() {
    ScreenshotTheme {
        SettingsGroup(
            title = "My library",
            rows = listOf(
                SettingsRow(icon = Icons.Filled.Bookmark, label = "Watchlist", detail = "42 titles"),
                SettingsRow(icon = Icons.Filled.Bookmark, label = "Watched", detail = "186 titles"),
                SettingsRow(icon = Icons.Filled.Settings, label = "Preferences"),
            ),
        )
    }
}

/** An untitled group whose rows carry sentiment-tinted icons and trailing count badges. */
@Composable
fun SettingsGroupTintedSample() {
    ScreenshotTheme {
        SettingsGroup(
            title = null,
            rows = listOf(
                SettingsRow(
                    icon = Icons.Filled.Inbox,
                    iconTint = BingeSentiment.Caution.accent(),
                    label = "Requests",
                    detail = "Approve, decline & track",
                    badgeCount = 3,
                    badgeTint = BingeSentiment.Caution.accent(),
                ),
                SettingsRow(
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
fun SettingsGroupBadgesSample() {
    ScreenshotTheme {
        SettingsGroup(
            title = "Companion apps",
            rows = listOf(
                SettingsRow(
                    icon = Icons.Filled.Inbox,
                    label = "Requests",
                    detail = "2 waiting for you",
                    badgeCount = 2,
                ),
                SettingsRow(
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
fun SettingsGroupSelectedSample() {
    ScreenshotTheme {
        SettingsGroup(
            title = "Manage",
            rows = listOf(
                SettingsRow(
                    icon = Icons.Filled.Inbox,
                    iconTint = BingeSentiment.Caution.accent(),
                    label = "Requests",
                    detail = "Approve, decline & track",
                    badgeCount = 3,
                    badgeTint = BingeSentiment.Caution.accent(),
                    selected = true,
                ),
                SettingsRow(
                    icon = Icons.Filled.People,
                    iconTint = BingeSentiment.Info.accent(),
                    label = "Users",
                    detail = "Roles, quotas & permissions",
                ),
                SettingsRow(icon = Icons.Filled.Settings, label = "Settings", detail = "Server & connection"),
            ),
        )
    }
}

/**
 * A row that leaves the app carries ↗ instead of the chevron (#51) — shown beside an ordinary
 * in-app row so the two read as distinct promises at a glance.
 */
@Composable
fun SettingsGroupExternalSample() {
    ScreenshotTheme {
        SettingsGroup(
            title = "About",
            rows = listOf(
                SettingsRow(icon = Icons.Filled.Settings, label = "Preferences"),
                SettingsRow(
                    icon = Icons.Filled.Policy,
                    label = "Privacy policy",
                    destination = SettingsRowDestination.External,
                ),
            ),
        )
    }
}

/**
 * The same rows as [SettingsGroupTitledSample] with [SettingsGroup]'s `rowVerticalPadding` raised
 * one rung on the padding ramp, for a caller whose rows are its main content (#133).
 */
@Composable
fun SettingsGroupTallRowsSample() {
    ScreenshotTheme {
        SettingsGroup(
            title = "My library",
            rows = listOf(
                SettingsRow(icon = Icons.Filled.Bookmark, label = "Watchlist", detail = "42 titles"),
                SettingsRow(icon = Icons.Filled.Bookmark, label = "Watched", detail = "186 titles"),
                SettingsRow(icon = Icons.Filled.Settings, label = "Preferences"),
            ),
            rowVerticalPadding = dimensionResource(R.dimen.padding_m),
        )
    }
}

/**
 * The same rows as [SettingsGroupTitledSample] with [SettingsGroup]'s `titleSpacing` dropped one
 * rung on the padding ramp, for a sheet whose titled groups sit close together.
 */
@Composable
fun SettingsGroupTightTitleSample() {
    ScreenshotTheme {
        SettingsGroup(
            title = "My library",
            rows = listOf(
                SettingsRow(icon = Icons.Filled.Bookmark, label = "Watchlist", detail = "42 titles"),
                SettingsRow(icon = Icons.Filled.Bookmark, label = "Watched", detail = "186 titles"),
                SettingsRow(icon = Icons.Filled.Settings, label = "Preferences"),
            ),
            titleSpacing = dimensionResource(R.dimen.padding_xs),
        )
    }
}

/**
 * [SettingsRow.leadingContent] standing in for the icon box: a user list, each row led by an avatar
 * rather than a glyph. `icon` is still supplied (the parameter has no default) but never drawn,
 * since [leadingContent] wins when set.
 */
@Composable
fun SettingsGroupLeadingContentSample() {
    ScreenshotTheme {
        SettingsGroup(
            title = "Users",
            rows = listOf(
                SettingsRow(
                    icon = Icons.Filled.People,
                    leadingContent = {
                        BingeInitialsAvatar(
                            name = "Ada Lovelace",
                            size = dimensionResource(R.dimen.settings_group_icon_size),
                        )
                    },
                    label = "Ada Lovelace",
                    detail = "Owner",
                ),
                SettingsRow(
                    icon = Icons.Filled.People,
                    leadingContent = {
                        BingeInitialsAvatar(
                            name = "Grace Hopper",
                            size = dimensionResource(R.dimen.settings_group_icon_size),
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
 * [SettingsRows] with no surface of its own, drawn inside a card the caller supplies — the request
 * quota readout living on the account card, rather than [SettingsGroup]'s own clipped surface.
 */
@Composable
fun SettingsRowsNoSurfaceSample() {
    ScreenshotTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BingeShapes.Large)
                .background(BingeSentiment.Info.accent().tonalContainer())
                .padding(vertical = dimensionResource(R.dimen.padding_s)),
        ) {
            SettingsRows(
                rows = listOf(
                    SettingsRow(icon = Icons.Filled.Inbox, label = "Request quota", detail = "8 of 10 used this month"),
                    SettingsRow(icon = Icons.Filled.Inbox, label = "Resets", detail = "In 12 days", clickable = false),
                ),
            )
        }
    }
}

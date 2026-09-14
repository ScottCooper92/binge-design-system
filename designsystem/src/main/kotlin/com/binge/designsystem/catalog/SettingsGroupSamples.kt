package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.SettingsGroup
import com.binge.designsystem.component.SettingsRow
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeSentiment
import com.binge.designsystem.theme.accent

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

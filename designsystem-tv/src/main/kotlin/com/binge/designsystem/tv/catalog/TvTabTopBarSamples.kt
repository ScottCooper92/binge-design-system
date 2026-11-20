@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.tv.R
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.nav.LocalTvTopInset
import com.binge.designsystem.tv.nav.TvAccountMenuRow
import com.binge.designsystem.tv.nav.TvNavItem
import com.binge.designsystem.tv.nav.TvTopBarAvatarSurface
import com.binge.designsystem.tv.nav.TvTopBarScaffold
import com.binge.designsystem.tv.nav.TvTopBarSlots
import com.binge.designsystem.tv.nav.tvContentGutterStart

private const val SEARCH = "search"
private const val MOVIES = "movies"

private val PendingSlots = TvTopBarSlots(
    header = TvNavItem(key = "account", label = "Account", icon = Icons.Filled.Person, displayName = "Scott Cooper"),
    items = listOf(
        TvNavItem(key = SEARCH, label = "Search", icon = Icons.Filled.Search),
        TvNavItem(key = MOVIES, label = "Movies", icon = Icons.Filled.Movie),
        TvNavItem(key = "tv", label = "TV shows", icon = Icons.Filled.Tv),
        TvNavItem(key = "requests", label = "Requests", icon = Icons.Filled.Bookmark),
        TvNavItem(key = "issues", label = "Issues", icon = Icons.Filled.Report),
    ),
    footer = TvNavItem(key = "settings", label = "Settings", icon = Icons.Filled.Settings),
)

private val ScaffoldSlots = TvTopBarSlots(
    header = TvNavItem(key = "account", label = "Account", icon = Icons.Filled.Person, displayName = "Scott Cooper"),
    items = listOf(
        TvNavItem(key = SEARCH, label = "Search", icon = Icons.Filled.Search),
        TvNavItem(key = MOVIES, label = "Movies", icon = Icons.Filled.Movie),
        TvNavItem(key = "tv", label = "TV shows", icon = Icons.Filled.Tv),
    ),
    footer = TvNavItem(key = "settings", label = "Settings", icon = Icons.Filled.Settings),
)

/**
 * The tab scaffold: the account avatar, three tabs with Search as an icon, the gear, and a page below that takes focus first. Back
 * climbs from the page to the tab, then home. The page here is only a stand-in.
 */
@Composable
fun TvTopBarScaffoldSample() {
    var selected by remember { mutableStateOf<Any>(MOVIES) }
    val content = remember { FocusRequester() }
    TvTopBarScaffold(
        slots = ScaffoldSlots,
        selectedKey = selected,
        homeKey = MOVIES,
        onSelect = { selected = it },
        contentDepth = 0,
        onGoHome = { selected = MOVIES },
        overlayOpen = false,
        contentFocusRequester = content,
        iconOnlyKeys = setOf(SEARCH),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            TvButton(label = "Page content", onClick = {}, modifier = Modifier.focusRequester(content))
        }
    }
}

/** The scaffold with the avatar's menu open, showing its detail line, a "Demo" tag beside the name and an extra line under the header. */
@Composable
fun TvTopBarScaffoldAccountMenuSample() {
    val content = remember { FocusRequester() }
    TvTopBarScaffold(
        slots = ScaffoldSlots,
        selectedKey = MOVIES,
        homeKey = MOVIES,
        onSelect = {},
        contentDepth = 0,
        onGoHome = {},
        overlayOpen = false,
        contentFocusRequester = content,
        accountRows = listOf(TvAccountMenuRow(key = "account", label = "Account", icon = Icons.Filled.Person, onClick = {})),
        accountDetail = "Server: home.example",
        accountTag = "Demo",
        accountExtraContent = { Text(text = "12 of 20 requests used", style = MaterialTheme.typography.bodyMedium) },
        accountInitiallyOpen = true,
        iconOnlyKeys = setOf(SEARCH),
    ) {
        Box(Modifier.fillMaxSize())
    }
}

/** Tabs the server has not yet resolved are hidden: Requests and Issues are pending, the selected Movies tab is kept. */
@Composable
fun TvTopBarScaffoldPendingTabsSample() {
    val content = remember { FocusRequester() }
    TvTopBarScaffold(
        slots = PendingSlots,
        selectedKey = MOVIES,
        homeKey = MOVIES,
        onSelect = {},
        contentDepth = 0,
        onGoHome = {},
        overlayOpen = false,
        contentFocusRequester = content,
        iconOnlyKeys = setOf(SEARCH),
        pendingKeys = setOf("requests", "issues"),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            TvButton(label = "Page content", onClick = {}, modifier = Modifier.focusRequester(content))
        }
    }
}

/** The account avatar signed in, with a picture-less profile, then as the current destination, then focused. */
@Composable
fun TvTopBarAvatarSample() {
    val entry = TvNavItem(key = "account", label = "Account", icon = Icons.Filled.Person, displayName = "Scott Cooper")
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.tv_content_gutter_start))) {
            TvTopBarAvatarSurface(entry, selected = false, isFocused = false)
            TvTopBarAvatarSurface(entry, selected = true, isFocused = false)
            TvTopBarAvatarSurface(entry, selected = false, isFocused = true)
        }
    }
}

/** The account avatar signed out: the person glyph, in the same three states. */
@Composable
fun TvTopBarAvatarSignedOutSample() {
    val entry = TvNavItem(key = "account", label = "Account", icon = Icons.Filled.Person)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.tv_content_gutter_start))) {
            TvTopBarAvatarSurface(entry, selected = false, isFocused = false)
            TvTopBarAvatarSurface(entry, selected = true, isFocused = false)
            TvTopBarAvatarSurface(entry, selected = false, isFocused = true)
        }
    }
}

/**
 * The scaffold with `contentBehindBar`: a full-bleed backdrop runs behind the bar, which draws over it on its scrim, and the page
 * pads its own content by the bar's height, read from `LocalTvTopInset`. Focus is as in the default scaffold.
 */
@Composable
fun TvTopBarScaffoldBehindBarSample() {
    val content = remember { FocusRequester() }
    TvTopBarScaffold(
        slots = ScaffoldSlots,
        selectedKey = MOVIES,
        homeKey = MOVIES,
        onSelect = {},
        contentDepth = 0,
        onGoHome = {},
        overlayOpen = false,
        contentFocusRequester = content,
        iconOnlyKeys = setOf(SEARCH),
        contentBehindBar = true,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.surfaceVariant))),
        ) {
            Column(
                modifier = Modifier
                    .padding(top = LocalTvTopInset.current)
                    .padding(start = tvContentGutterStart()),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.tv_media_row_card_gap)),
            ) {
                Text(text = "Requests", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                TvButton(label = "Page content", onClick = {}, modifier = Modifier.focusRequester(content))
            }
        }
    }
}

@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.tv.nav.BingeTvTopBar
import com.binge.designsystem.tv.nav.TvNavItem
import com.binge.designsystem.tv.nav.TvTopBarSurface

private const val TOP_BAR_ART_FRACTION = 0.3f

private val TopBarHeader = TvNavItem(
    key = "account",
    label = "Account",
    icon = Icons.Filled.Person,
    displayName = "Alexander Hamilton",
)
private val TopBarFooter = TvNavItem(key = "settings", label = "Settings", icon = Icons.Filled.Settings)
internal val TopBarSampleItems = listOf(
    TvNavItem(key = "home", label = "Home", icon = Icons.Filled.Home),
    TvNavItem(key = "movies", label = "Movies", icon = Icons.Filled.Movie),
    TvNavItem(key = "tv", label = "TV shows", icon = Icons.Filled.Tv),
    TvNavItem(key = "requests", label = "Requests", icon = Icons.Filled.Bookmark),
    TvNavItem(key = "search", label = "Search", icon = Icons.Filled.Search),
)
private val TopBarLongItems = listOf(
    TvNavItem(key = "home", label = "Recently added", icon = Icons.Filled.Home),
    TvNavItem(key = "movies", label = "Popular movies", icon = Icons.Filled.Movie),
    TvNavItem(key = "tv", label = "Trending TV shows", icon = Icons.Filled.Tv),
    TvNavItem(key = "requests", label = "Pending requests", icon = Icons.Filled.Bookmark),
    TvNavItem(key = "search", label = "Search the library", icon = Icons.Filled.Search),
)

/** The top bar over page art: an avatar at the start, five tabs centred, a gear at the end. Click a tab to select it. */
@Composable
fun TvTopBarSample() {
    TvTopBarSample(items = TopBarSampleItems)
}

/**
 * The top bar with [items], over a band of page art so the scrim reads. [focusedKey], when set, holds the bar focused on that
 * item (its key, or the header's or footer's) without a focus request; null leaves the bar live.
 */
@Composable
internal fun TvTopBarSample(
    items: List<TvNavItem>,
    focusedKey: Any? = null,
    selectedKey: Any = "movies",
    iconOnlyFooter: Boolean = true,
) {
    var selected by rememberSaveable { mutableStateOf(selectedKey) }
    val requesters = remember { (items + TopBarHeader + TopBarFooter).associate { it.key to FocusRequester() } }
    val down = remember { FocusRequester() }
    val iconOnly = if (iconOnlyFooter) setOf<Any>(TopBarFooter.key) else emptySet()
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(TOP_BAR_ART_FRACTION)
                .background(MaterialTheme.colorScheme.primary),
        )
        Text(
            text = "Page content under the bar",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Center),
        )
        if (focusedKey == null) {
            BingeTvTopBar(
                header = TopBarHeader,
                items = items,
                footer = TopBarFooter,
                selectedKey = selected,
                onSelect = { selected = it },
                itemFocusRequesters = requesters,
                down = down,
                onFocusedChange = {},
                iconOnlyKeys = iconOnly,
            )
        } else {
            TvTopBarSurface(
                header = TopBarHeader,
                items = items,
                footer = TopBarFooter,
                selectedKey = selected,
                barFocused = true,
                focusedKey = focusedKey,
                onItemFocusChanged = { _, _ -> },
                onSelect = {},
                itemFocusRequesters = requesters,
                down = down,
                iconOnlyKeys = iconOnly,
                visible = true,
                scrim = true,
            )
        }
    }
}

/** The top bar with labels long enough to crowd it: for the 1.3x and 2x font frames, where each stays on one line. */
@Composable
fun TvTopBarLongLabelsSample() {
    TvTopBarSample(items = TopBarLongItems)
}

/** The top bar with its footer left as a label pill, the default, and long tabs: the tabs keep clear of the wider end. */
@Composable
fun TvTopBarLabelFooterSample() {
    TvTopBarSample(items = TopBarLongItems, iconOnlyFooter = false)
}

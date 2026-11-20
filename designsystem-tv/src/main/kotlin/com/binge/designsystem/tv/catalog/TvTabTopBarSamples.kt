@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.tv.R
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.nav.TvNavItem
import com.binge.designsystem.tv.nav.TvTopBarAvatarSurface
import com.binge.designsystem.tv.nav.TvTopBarScaffold
import com.binge.designsystem.tv.nav.TvTopBarSlots

private const val SEARCH = "search"
private const val MOVIES = "movies"

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

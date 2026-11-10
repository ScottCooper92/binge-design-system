@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.tv.component.TvIconButton
import com.binge.designsystem.tv.nav.BingeTvTopBar
import com.binge.designsystem.tv.nav.TvAccountMenu
import com.binge.designsystem.tv.nav.TvAccountMenuConfirm
import com.binge.designsystem.tv.nav.TvAccountMenuRow

private const val ART_FRACTION = 0.3f

/** The account menu in the top bar's trailing slot, closed: only the avatar and the gear show, and the bar keeps its size. */
@Composable
fun TvAccountMenuSample() {
    AccountMenuBar(initiallyOpen = false)
}

/** The same bar with the menu open over the avatar: the header, a quota line, then the rows, with Sign out last. */
@Composable
fun TvAccountMenuOpenSample() {
    AccountMenuBar(initiallyOpen = true)
}

/**
 * The real menu on a live bar. Focus the avatar to open it, Down to enter its rows, Up to return. BACK closes it onto the
 * avatar and it stays closed until focus leaves and comes back. Sign out asks first, then closes the menu.
 */
@Composable
fun TvAccountMenuDemo() {
    AccountMenuBar(initiallyOpen = false)
}

@Composable
private fun AccountMenuBar(initiallyOpen: Boolean) {
    val down = remember { FocusRequester() }
    var lastAction by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(ART_FRACTION)
                .background(MaterialTheme.colorScheme.primary),
        )
        Text(
            text = lastAction.ifEmpty { "Page content under the bar" },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Center),
        )
        BingeTvTopBar(
            header = null,
            items = emptyList(),
            footer = null,
            selectedKey = "none",
            onSelect = {},
            itemFocusRequesters = emptyMap(),
            down = down,
            onFocusedChange = {},
            trailing = {
                TvAccountMenu(
                    avatarLabel = "Account",
                    name = "Alexander Hamilton",
                    detail = "Administrator",
                    down = down,
                    initiallyOpen = initiallyOpen,
                    extraContent = {
                        Text(
                            text = "12 of 20 requests used",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    rows = listOf(
                        TvAccountMenuRow("switch", "Switch profile", Icons.Filled.SwapHoriz, { lastAction = "Switch profile" }),
                        TvAccountMenuRow(
                            key = "signout",
                            label = "Sign out",
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            onClick = { lastAction = "Signed out" },
                            confirm = TvAccountMenuConfirm("Sign out?", "You will need to sign in again.", "Sign out"),
                        ),
                        TvAccountMenuRow("settings", "Settings", Icons.Filled.Settings, { lastAction = "Settings" }),
                    ),
                )
                TvIconButton(icon = Icons.Filled.Settings, label = "Settings", onClick = {})
            },
        )
    }
}

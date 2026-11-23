@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.binge.designsystem.component.BingeNavPresentation
import com.binge.designsystem.component.BingeNavSuiteItem
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.shell.TabShell
import com.binge.designsystem.shell.rememberTabShellState

private enum class ShellTab(
    val label: String,
) {
    Movies("Movies"),
    Search("Search"),
    Settings("Settings"),
}

private data class ShellRoute(
    val name: String,
) : NavKey

/**
 * Three tabs that each keep their own back stack, in the bottom bar. Tap another tab to show it as it was left; tap the
 * tab that is showing to send it back to its root. Back from another tab returns to Movies.
 */
@Composable
fun TabShellSample() {
    ScreenshotTheme {
        val stacks = remember { ShellTab.entries.associateWith { NavBackStack<NavKey>(ShellRoute(it.label)) } }
        val state = rememberTabShellState(ShellTab.entries, initial = ShellTab.Movies, stacks = stacks)
        val items =
            listOf(
                BingeNavSuiteItem(key = ShellTab.Movies, label = ShellTab.Movies.label, icon = Icons.Default.Movie),
                BingeNavSuiteItem(key = ShellTab.Search, label = ShellTab.Search.label, icon = Icons.Default.Search),
                BingeNavSuiteItem(key = ShellTab.Settings, label = ShellTab.Settings.label, icon = Icons.Default.Settings),
            )
        TabShell(state = state, items = items, presentation = BingeNavPresentation.BottomBar) { tab ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("${tab.label} content", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

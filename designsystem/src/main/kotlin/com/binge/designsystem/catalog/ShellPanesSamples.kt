@file:OnePerScreen(fullScreen = true)

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.binge.designsystem.pane.LocalShellDetail
import com.binge.designsystem.pane.ShellDetail
import com.binge.designsystem.pane.ShellPanes
import com.binge.designsystem.preview.ScreenshotTheme

private data class SampleKey(
    val name: String,
) : NavKey

private fun paneEntry(name: String) = NavEntry<NavKey>(SampleKey(name)) { SamplePane(name) }

@Composable
private fun SamplePane(name: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = name, style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * [ShellPanes] on a window with room for three: the home, a page opened from it and that page's item, each a third of the width with the
 * design system's gap between them. The words are the sample's own.
 */
@Composable
fun ShellPanesSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalShellDetail provides ShellDetail(paneEntry("Page"), paneEntry("Item"), opensItems = true, threePane = true),
        ) {
            ShellPanes(
                home = { SamplePane("Home") },
                defaultPage = { SamplePane("Default page") },
                itemPlaceholder = { SamplePane("Pick an item") },
            )
        }
    }
}

/** [ShellPanes] with a page that opens items and none open yet, on a window with room for three: the third pane shows its placeholder. */
@Composable
fun ShellPanesPlaceholderSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalShellDetail provides ShellDetail(paneEntry("Page"), null, opensItems = true, threePane = true),
        ) {
            ShellPanes(
                home = { SamplePane("Home") },
                defaultPage = { SamplePane("Default page") },
                itemPlaceholder = { SamplePane("Pick an item") },
            )
        }
    }
}

/** [ShellPanes] on a window with room for two, with no page open: the home beside the default page. */
@Composable
fun ShellPanesDefaultPageSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalShellDetail provides ShellDetail(null, null, opensItems = false, threePane = false),
        ) {
            ShellPanes(
                home = { SamplePane("Home") },
                defaultPage = { SamplePane("Default page") },
                itemPlaceholder = { SamplePane("Pick an item") },
            )
        }
    }
}

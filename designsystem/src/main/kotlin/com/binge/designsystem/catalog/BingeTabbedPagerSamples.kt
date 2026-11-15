@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeTabbedPager
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [BingeTabbedPager]: [com.binge.designsystem.component.BingeTabRow] wired to a swipeable page per
 * tab, the tabs overlaid on the paged content. Each page is its own view, which is what tabs are for: a different
 * colour and a line saying which tab it belongs to. Swipe, or tap a tab; the two stay in step.
 *
 * Bounded to [R.dimen.catalog_tabbed_pager_height], because the pager is otherwise `fillMaxSize`.
 */
@Composable
fun BingeTabbedPagerSample() {
    TabbedPagerBox(tabs = listOf("Requests", "Library", "Activity"))
}

/**
 * The same pager with five tabs as long as a real screen's, scrolling past the edge: the row scrolls rather than
 * shrinking a tab, so every label stays whole at any font size.
 */
@Composable
fun BingeTabbedPagerManyTabsSample() {
    TabbedPagerBox(tabs = listOf("All (42)", "Pending (4)", "Downloading (5)", "Available (28)", "Declined (2)"))
}

@Composable
private fun TabbedPagerBox(tabs: List<String>) {
    var selected by remember { mutableIntStateOf(0) }
    ScreenshotTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.catalog_tabbed_pager_height)),
        ) {
            BingeTabbedPager(
                tabs = tabs,
                selectedIndex = selected,
                onSelectedIndexChange = { selected = it },
                modifier = Modifier.fillMaxSize(),
            ) { contentPadding, page ->
                val tone = if (page % 2 == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(tone)
                        .padding(contentPadding)
                        .padding(horizontal = dimensionResource(R.dimen.padding_m)),
                ) {
                    Text(text = "Page ${page + 1}: ${tabs[page]}", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

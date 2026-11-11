@file:OnePerScreen

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
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
import com.binge.designsystem.component.BingeTabRow
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.resolvedContentInset

/** Scrollable tab strip with the second tab active — the caller owns the selected index. */
@Composable
fun BingeTabRowSample() {
    var selected by remember { mutableIntStateOf(1) }
    ScreenshotTheme {
        BingeTabRow(
            tabs = listOf("All (42)", "Pending (4)", "Downloading (5)", "Available (28)", "Declined (2)"),
            selectedIndex = selected,
            onSelect = { selected = it },
        )
    }
}

/**
 * The strip inside a column that already carries the screen inset, with no edge padding: the first tab's cell
 * starts at the text's left edge, and the label sits inside it by the tab's own padding. With the default padding the
 * row would indent by the inset a second time.
 */
@Composable
fun BingeTabRowFlushSample() {
    var selected by remember { mutableIntStateOf(0) }
    ScreenshotTheme {
        Column(modifier = Modifier.padding(horizontal = resolvedContentInset())) {
            Text(text = "Content in a padded column", style = MaterialTheme.typography.titleMedium)
            BingeTabRow(
                tabs = listOf("All (42)", "Pending (4)", "Downloading (5)", "Available (28)", "Declined (2)"),
                selectedIndex = selected,
                onSelect = { selected = it },
                edgePadding = dimensionResource(R.dimen.catalog_tab_row_flush_padding),
            )
        }
    }
}

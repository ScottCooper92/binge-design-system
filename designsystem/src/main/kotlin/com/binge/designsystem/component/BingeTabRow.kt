package com.binge.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * The tab strip on its own: a scrollable row of text tabs with the active indicator.
 * Stateless — the caller owns [selectedIndex] and reacts to [onSelect]. Kept separate from
 * [BingeTabbedPager] so it can be previewed and screenshot-tested without a pager.
 */
@Composable
fun BingeTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryScrollableTabRow(
        selectedTabIndex = selectedIndex.coerceIn(0, (tabs.size - 1).coerceAtLeast(0)),
        modifier = modifier,
        edgePadding = dimensionResource(R.dimen.screen_content_inset),
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeTabRowLight() {
    BingeExpressiveTheme {
        BingeTabRow(
            tabs = listOf("All (42)", "Pending (4)", "Downloading (5)", "Available (28)", "Declined (2)"),
            selectedIndex = 1,
            onSelect = {},
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewBingeTabRowDark() {
    BingeExpressiveTheme {
        BingeTabRow(
            tabs = listOf("Open", "Resolved", "All"),
            selectedIndex = 0,
            onSelect = {},
        )
    }
}

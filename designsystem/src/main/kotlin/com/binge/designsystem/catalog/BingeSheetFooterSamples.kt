@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeActionFooter
import com.binge.designsystem.component.BingeSheetFooter
import com.binge.designsystem.component.bingeSheetScroll
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.TaskSheetPreviewFrame

private const val LONG_PAGE_ROWS = 24

/**
 * A short page with its primary in the sheet's footer, called from inside the page's own scrolling column as a page does. At the expanded width the side sheet pins the primary to the foot of
 * the sheet, so it is in the same place as on a long page; below it the bottom sheet draws it in place, under the last row.
 */
@Composable
fun BingeSheetFooterSample() {
    ScreenshotTheme {
        FooterBackdrop {
            TaskSheetPreviewFrame {
                FooterPage(rows = 3)
            }
        }
    }
}

/**
 * A page longer than the sheet: the content scrolls above the footer, and the edge between them shows because content is
 * still under it. Scrolled to the end, or on a page that fits, there is no edge.
 */
@Composable
fun BingeSheetFooterScrollingSample() {
    ScreenshotTheme {
        FooterBackdrop {
            TaskSheetPreviewFrame {
                FooterPage(rows = LONG_PAGE_ROWS)
            }
        }
    }
}

@Composable
private fun FooterBackdrop(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { content() }
}

@Composable
private fun ColumnScope.FooterPage(rows: Int) {
    Column(modifier = Modifier.bingeSheetScroll()) {
        Text(
            text = "Request",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        )
        repeat(rows) { index ->
            Text(
                text = "Season ${index + 1}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().padding(
                    horizontal = dimensionResource(R.dimen.padding_m),
                    vertical = dimensionResource(R.dimen.padding_s),
                ),
            )
        }
        BingeSheetFooter { BingeActionFooter(label = "Request", onClick = {}) }
    }
}

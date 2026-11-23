@file:OnePerScreen
@file:CatalogGroup("Sheets")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeActionFooter
import com.binge.designsystem.component.BingeAdaptiveSheet
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.BingeSheetFooter
import com.binge.designsystem.component.BingeSheetKind
import com.binge.designsystem.component.bingeSheetScroll
import com.binge.designsystem.preview.ScreenshotTheme

private const val DEMO_ROWS = 30

/**
 * A task sheet with a long page and a pinned primary, opened for real: widen the window to 840dp or more and the side
 * sheet keeps the button at its foot while the rows scroll above it, with an edge between them until the last row.
 */
@Composable
fun BingeSheetFooterDemo() {
    ScreenshotTheme {
        var open by remember { mutableStateOf(false) }
        Column(modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_m))) {
            BingeFilledButton(label = "Open a long page", onClick = { open = true })
        }
        if (open) {
            BingeAdaptiveSheet(kind = BingeSheetKind.Task, onDismissRequest = { open = false }) {
                Column(modifier = Modifier.weight(1f, fill = false).bingeSheetScroll()) {
                    Text(
                        text = "Request",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
                    )
                    repeat(DEMO_ROWS) { index ->
                        Text(
                            text = "Season ${index + 1}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth().padding(
                                horizontal = dimensionResource(R.dimen.padding_m),
                                vertical = dimensionResource(R.dimen.padding_s),
                            ),
                        )
                    }
                }
                BingeSheetFooter { BingeActionFooter(label = "Request", onClick = { open = false }) }
            }
        }
    }
}

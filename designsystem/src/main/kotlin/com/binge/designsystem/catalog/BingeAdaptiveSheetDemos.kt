@file:OnePerScreen
@file:CatalogGroup("Sheets")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
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
import com.binge.designsystem.component.BingeAdaptiveSheet
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.BingeOutlinedButton
import com.binge.designsystem.component.BingeSheetKind
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.preview.ScreenshotTheme

private const val DETAIL_PAGE = "detail"

/**
 * An adaptive sheet of each kind, opened for real: widen the window to 840dp or more (unfold a foldable, rotate a tablet)
 * and the task sheet becomes a side sheet whose "Open a page" row stacks a second page inside it under a Back, where a
 * narrow window keeps today's bottom sheet and opens the page as a second one. The menu is a bottom sheet at any width.
 */
@Composable
fun BingeAdaptiveSheetDemo() {
    ScreenshotTheme {
        var kind by remember { mutableStateOf<BingeSheetKind?>(null) }
        var secondSheet by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            BingeFilledButton(label = "Open a task sheet", onClick = { kind = BingeSheetKind.Task })
            BingeOutlinedButton(label = "Open a menu sheet", onClick = { kind = BingeSheetKind.Menu })
        }
        kind?.let { open ->
            BingeAdaptiveSheet(kind = open, onDismissRequest = { kind = null }) { nav ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_m)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                ) {
                    Text(
                        text = if (nav.page == DETAIL_PAGE) "A page inside the sheet" else "A ${open.name.lowercase()} sheet",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (open == BingeSheetKind.Task && nav.page != DETAIL_PAGE) {
                        BingeTextButton(
                            label = "Open a page",
                            onClick = { if (nav.stacksPages) nav.push(DETAIL_PAGE) else secondSheet = true },
                        )
                    }
                    BingeTextButton(label = "Close", onClick = { kind = null })
                }
            }
        }
        if (secondSheet) {
            BingeAdaptiveSheet(kind = BingeSheetKind.Task, onDismissRequest = { secondSheet = false }) {
                Text(
                    text = "The second sheet a narrow window opens",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
                )
            }
        }
    }
}

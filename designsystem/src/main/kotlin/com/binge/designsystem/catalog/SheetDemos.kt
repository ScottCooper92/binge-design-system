@file:OnePerScreen
@file:CatalogGroup("Sheets")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.binge.designsystem.component.BingeBottomSheet
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.BingeModalSideSheet
import com.binge.designsystem.component.BingeOutlinedButton
import com.binge.designsystem.component.BingeSheetDockingHeader
import com.binge.designsystem.component.BingeSheetTopBar
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.component.LocalBingeSheetDock
import com.binge.designsystem.component.bingeSheetPinnedFooter
import com.binge.designsystem.preview.ScreenshotTheme

private enum class OpenSheet { Bottom, Docking, Side }

private const val DOCKING_ROWS = 30

/**
 * Every sheet in the design system, each opened for real from its own button, which a screenshot of
 * the sheet's body cannot show: the window, the scrim, the drag and the ways out. The line below the
 * buttons reports how the last one closed.
 */
@Composable
fun SheetsDemo() {
    ScreenshotTheme {
        var open by remember { mutableStateOf<OpenSheet?>(null) }
        var outcome by remember { mutableStateOf("Not opened yet") }

        fun close(how: String) {
            open = null
            outcome = how
        }
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            BingeOutlinedButton(label = "Bottom sheet", onClick = { open = OpenSheet.Bottom })
            BingeOutlinedButton(label = "Docking bottom sheet", onClick = { open = OpenSheet.Docking })
            BingeOutlinedButton(label = "Side sheet", onClick = { open = OpenSheet.Side })
            Text(outcome)
        }
        when (open) {
            OpenSheet.Bottom ->
                BingeBottomSheet(onDismissRequest = { close("Dismissed by gesture, scrim or back") }) {
                    SheetBody("A real modal bottom sheet: drag it, tap the scrim, press back, or", "Done") {
                        close("Dismissed by Done")
                    }
                }
            OpenSheet.Docking ->
                BingeBottomSheet(
                    onDismissRequest = { close("Dismissed by gesture, scrim, back or close") },
                    skipPartiallyExpanded = false,
                    dockable = true,
                ) {
                    DockingSheetBody { close("Dismissed by Done") }
                }
            OpenSheet.Side ->
                BingeModalSideSheet(onDismissRequest = { close("Dismissed by back") }) {
                    SheetBody("A real modal side sheet: press back, or", "Close") { close("Closed by button") }
                }
            null -> Unit
        }
    }
}

@Composable
private fun SheetBody(
    text: String,
    action: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        Text(text)
        BingeTextButton(label = action, onClick = onAction)
    }
}

/**
 * A docking sheet's body: a header that docks into a top bar when the sheet is pulled up, a list long enough to
 * fill the screen, and a footer pinned to the bottom edge while the sheet is part-way open.
 */
@Composable
private fun ColumnScope.DockingSheetBody(onDone: () -> Unit) {
    BingeSheetDockingHeader(
        header = {
            Text(
                text = "Pull me up to dock",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_m)),
            )
        },
        dockedTopBar = { onClose -> BingeSheetTopBar(title = "Docked", subtitle = "Close leaves the sheet", onClose = onClose) },
    )
    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
        repeat(DOCKING_ROWS) { row ->
            Text(text = "Row ${row + 1}", modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)))
        }
    }
    Column(
        modifier =
            Modifier
                .bingeSheetPinnedFooter(LocalBingeSheetDock.current, MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(dimensionResource(R.dimen.padding_m)),
    ) {
        BingeFilledButton(label = "Done", onClick = onDone, modifier = Modifier.fillMaxWidth())
    }
}

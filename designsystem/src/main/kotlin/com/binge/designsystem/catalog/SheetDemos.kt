@file:OnePerScreen
@file:CatalogGroup("Sheets")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import com.binge.designsystem.component.BingeModalSideSheet
import com.binge.designsystem.component.BingeOutlinedButton
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.preview.ScreenshotTheme

private enum class OpenSheet { Bottom, Side }

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

@file:CatalogGroup("Sheets")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.binge.designsystem.component.BingeLoadingIndicator
import com.binge.designsystem.component.BingeSheetStateSwitch
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.preview.ScreenshotTheme

private enum class SwitchState { Loading, Failed, Ready }

/**
 * A sheet's middle region swapping between loading, failed and ready. The buttons change the state: in the catalog app the old content
 * fades out as the new fades in and the height eases between them; the frame shows the ready state.
 */
@Composable
fun BingeSheetStateSwitchSample() {
    ScreenshotTheme {
        var state by remember { mutableStateOf(SwitchState.Ready) }
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
                SwitchState.entries.forEach { BingeTextButton(label = it.name, onClick = { state = it }) }
            }
            BingeSheetStateSwitch(state) { shown ->
                when (shown) {
                    SwitchState.Loading -> BingeLoadingIndicator()
                    SwitchState.Failed -> Text("The list didn't load.", style = MaterialTheme.typography.bodyMedium)
                    SwitchState.Ready ->
                        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
                            listOf("Alpha", "Beta", "Gamma").forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
                        }
                }
            }
        }
    }
}

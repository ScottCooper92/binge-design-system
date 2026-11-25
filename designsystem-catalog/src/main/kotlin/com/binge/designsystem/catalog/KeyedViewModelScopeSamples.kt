@file:CatalogGroup("State")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.viewmodel.KeyedViewModelScope

internal class CounterModel : ViewModel() {
    var count by mutableIntStateOf(0)
}

/**
 * A counter whose view model lives in [KeyedViewModelScope]. Add to Scope A, switch to Scope B and it starts at zero: A's view model went
 * with its scope. Switch back and A starts again at zero too, since it was cleared when it left. The words are the sample's own.
 */
@Composable
fun KeyedViewModelScopeSample() {
    var key by remember { mutableStateOf("Scope A") }
    ScreenshotTheme {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
                listOf("Scope A", "Scope B").forEach { name ->
                    BingeTextButton(label = name, onClick = { key = name })
                }
            }
            KeyedViewModelScope(key) {
                val model = viewModel<CounterModel>()
                Text(text = "$key: ${model.count}", style = MaterialTheme.typography.titleMedium)
                BingeFilledButton(label = "Add", onClick = { model.count++ })
            }
        }
    }
}

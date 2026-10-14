@file:CatalogGroup("Choices")

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.tv.component.TvOptionRow
import kotlinx.coroutines.delay
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val STATUS_AVAILABLE = "available"
private const val STATUS_PARTIAL = "partial"
private const val STATUS_PENDING = "pending"
private const val DEMO_SAVING_MILLIS = 2_000L

/**
 * A choice that applies at once and is saving: the focused row holds a spinner where its tick will go, while the current
 * value keeps its tick. OK on another row moves the spinner there.
 */
@Composable
fun TvOptionRowSavingSample() {
    var saving by remember { mutableStateOf<String?>(STATUS_AVAILABLE) }
    StatusOptions(selected = STATUS_PENDING, saving = saving, focused = STATUS_AVAILABLE, onChoose = { saving = it })
}

/** The real rows: OK on one saves for two seconds, its spinner turning, then it takes the tick. */
@Composable
fun TvOptionRowSavingDemo() {
    var selected by remember { mutableStateOf(STATUS_PENDING) }
    var saving by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(saving) {
        val target = saving ?: return@LaunchedEffect
        delay(DEMO_SAVING_MILLIS)
        selected = target
        saving = null
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        StatusOptions(selected = selected, saving = saving, focused = null, onChoose = { if (saving == null) saving = it })
    }
}

@Composable
private fun StatusOptions(
    selected: String,
    saving: String?,
    focused: String?,
    onChoose: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .padding(dimensionResource(DesR.dimen.padding_l))
                .width(dimensionResource(TvR.dimen.tv_text_field_width)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_option_row_gap)),
    ) {
        StatusRows.forEach { (key, label, icon) ->
            TvOptionRow(
                label = label,
                selected = selected == key,
                onClick = { onChoose(key) },
                initiallyFocused = focused == key,
                icon = icon,
                saving = saving == key,
            )
        }
    }
}

private val StatusRows =
    listOf(
        Triple(STATUS_AVAILABLE, "Available", Icons.Filled.CheckCircle),
        Triple(STATUS_PARTIAL, "Partially available", Icons.Filled.Tonality),
        Triple(STATUS_PENDING, "Pending", Icons.Filled.HourglassEmpty),
    )

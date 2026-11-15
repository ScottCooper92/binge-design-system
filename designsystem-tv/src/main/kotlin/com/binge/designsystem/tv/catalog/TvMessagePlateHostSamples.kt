@file:ScreenshotOnly

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.catalog.ScreenshotOnly
import com.binge.designsystem.tv.component.TvCompactButton
import com.binge.designsystem.tv.component.TvMessagePlate
import com.binge.designsystem.R as DesR

/**
 * A message plate placed in the layouts a screen puts one in. A plate asks nothing of its parent beyond a size, so each of these
 * must lay out; `TvMessagePlateHostTest` renders them and asserts the copy is on screen.
 */
@Composable
private fun SamplePlate(modifier: Modifier = Modifier) {
    TvMessagePlate(
        modifier = modifier,
        headline = "Nothing here yet",
        body = "Titles you add will show up in this list.",
        icon = Icons.Filled.SystemUpdate,
        alignment = Alignment.Center,
        actions = { TvCompactButton(label = "Refresh", onClick = {}) },
    )
}

/** The plate as an item of a `LazyColumn`, which offers it no height limit: it is its natural height. */
@Composable
fun TvMessagePlateInLazyColumnSample() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                "Above the plate",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_m)),
            )
        }
        item { SamplePlate() }
    }
}

/** The plate in a parent that sizes itself to its tallest child, `height(IntrinsicSize.Min)`, beside a sibling. */
@Composable
fun TvMessagePlateInIntrinsicMinSample() {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_m))) {
            Text("Sibling", style = MaterialTheme.typography.titleMedium)
        }
        SamplePlate(modifier = Modifier.height(IntrinsicSize.Min))
    }
}

/** The same with `IntrinsicSize.Max`. */
@Composable
fun TvMessagePlateInIntrinsicMaxSample() {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
        Column(modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_m))) {
            Text("Sibling", style = MaterialTheme.typography.titleMedium)
        }
        SamplePlate(modifier = Modifier.height(IntrinsicSize.Max))
    }
}

/** The plate filling a `Box`, as a full-screen state does. */
@Composable
fun TvMessagePlateInBoxSample() {
    Box(modifier = Modifier.fillMaxSize()) { SamplePlate() }
}

@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeSegmentedToggle
import com.binge.designsystem.preview.ScreenshotTheme

private val JoinLabels = listOf("OR", "AND")

/** Two values in one pill, OR picked: a tap on AND moves the pick. The second pill is dimmed and ignores taps. */
@Composable
fun BingeSegmentedToggleSample() {
    var picked by remember { mutableIntStateOf(0) }
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)), modifier = Modifier.fillMaxWidth()) {
            Text("Match any or all of the picks")
            BingeSegmentedToggle(JoinLabels, picked, { picked = it }, contentDescription = "Match any or all genres")
            Text("Needs two picks first")
            BingeSegmentedToggle(JoinLabels, 0, {}, contentDescription = "Match any or all genres", enabled = false)
        }
    }
}

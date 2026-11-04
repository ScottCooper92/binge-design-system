@file:OnePerScreen
@file:CatalogGroup("Sliders")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeNumberSlider

/** The readout follows the drag; the saved value changes only on release or a stepper tap. */
@Composable
fun NumberSliderCommitDemo() {
    var draft by rememberSaveable { mutableIntStateOf(7) }
    var saved by rememberSaveable { mutableIntStateOf(7) }
    Column(Modifier.padding(dimensionResource(R.dimen.padding_m))) {
        BingeNumberSlider(
            value = draft,
            onValueChange = { draft = it ?: 1 },
            range = 0..120,
            step = 5,
            format = { it.toString() },
            description = "Amount",
            onValueChangeFinished = { saved = draft },
        )
        Text("Saved value: $saved")
    }
}

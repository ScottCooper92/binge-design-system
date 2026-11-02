package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeStop
import com.binge.designsystem.component.BingeStopEmphasis
import com.binge.designsystem.component.BingeStopSlider
import com.binge.designsystem.preview.ScreenshotTheme

private val sampleStops =
    listOf(
        BingeStop("Strict", Icons.Filled.Star),
        BingeStop("Default", Icons.Filled.Public),
        BingeStop("Wide", Icons.Filled.Share),
        BingeStop("Anything", Icons.Filled.Block, BingeStopEmphasis.Warning),
    )

private val longStops =
    listOf(
        BingeStop("Public domain", Icons.Filled.Star),
        BingeStop("Attribution", Icons.Filled.Public),
        BingeStop("Creative Commons", Icons.Filled.Share),
        BingeStop("Anything goes", Icons.Filled.Block, BingeStopEmphasis.Warning),
    )

@Composable
private fun StopSliderSampleFrame(
    stops: List<BingeStop>,
    selected: Int,
    holdIndex: Int? = null,
) {
    // Each sample starts on its own stop, which is all a frame sees, and then commits for real.
    var current by remember { mutableIntStateOf(selected) }
    ScreenshotTheme {
        BingeStopSlider(
            stops = stops,
            selectedIndex = current,
            onCommit = { current = it },
            description = "Licences",
            holdIndex = holdIndex,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        )
    }
}

/** A normal stop selected: the track and thumb in the primary colour. */
@Composable
fun BingeStopSliderSample() {
    StopSliderSampleFrame(sampleStops, selected = 1)
}

/** The warning stop selected: the track, thumb and that stop's icon and name in the error colour. */
@Composable
fun BingeStopSliderWarningSample() {
    StopSliderSampleFrame(sampleStops, selected = 3)
}

/** The thumb held on the warning stop while the caller asks about it, though the saved stop is another. */
@Composable
fun BingeStopSliderHeldSample() {
    StopSliderSampleFrame(sampleStops, selected = 1, holdIndex = 3)
}

/** The longest names a stop should carry, at large fonts: they wrap by word under their icons inside the room each stop owns. */
@Composable
fun BingeStopSliderWorstCaseSample() {
    StopSliderSampleFrame(longStops, selected = 1)
}

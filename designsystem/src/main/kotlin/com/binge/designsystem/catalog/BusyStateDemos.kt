package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.component.ExpressiveIconButton
import com.binge.designsystem.component.IconButtonTone
import com.binge.designsystem.preview.ScreenshotTheme
import kotlinx.coroutines.delay

private const val BUSY_DEMO_MILLIS = 2_000L

/** [BingeTextButton] going busy on a tap for two seconds: the spinner replaces the label and taps are swallowed. */
@Composable
fun BingeTextButtonLoadingDemo() {
    ScreenshotTheme {
        var loading by remember { mutableStateOf(false) }
        var taps by remember { mutableStateOf(0) }
        LaunchedEffect(loading) {
            if (loading) {
                delay(BUSY_DEMO_MILLIS)
                loading = false
            }
        }
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            BingeTextButton(label = "Save changes", onClick = {
                loading = true
                taps++
            }, loading = loading)
            Text("Taps that reached the button: $taps")
        }
    }
}

/** [ExpressiveIconButton] going busy on a tap for two seconds, in the tonal tone. */
@Composable
fun ExpressiveIconButtonLoadingDemo() {
    ScreenshotTheme {
        var loading by remember { mutableStateOf(false) }
        var taps by remember { mutableStateOf(0) }
        LaunchedEffect(loading) {
            if (loading) {
                delay(BUSY_DEMO_MILLIS)
                loading = false
            }
        }
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            ExpressiveIconButton(
                onClick = {
                    loading = true
                    taps++
                },
                icon = Icons.Filled.Refresh,
                contentDescription = null,
                tone = IconButtonTone.Tonal,
                loading = loading,
            )
            Text("Taps that reached the button: $taps")
        }
    }
}

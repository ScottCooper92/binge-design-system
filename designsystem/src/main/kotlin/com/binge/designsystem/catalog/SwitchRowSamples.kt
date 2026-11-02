package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.SwitchRow
import com.binge.designsystem.preview.ScreenshotTheme

/** A [SwitchRow] off, then on with a supporting line, then disabled: the three states of one toggle. */
@Composable
fun SwitchRowSample() {
    var first by remember { mutableStateOf(false) }
    var second by remember { mutableStateOf(true) }
    ScreenshotTheme {
        Column(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_m))) {
            SwitchRow(label = "Notify me", checked = first, onCheckedChange = { first = it })
            SwitchRow(
                label = "Also delete the files",
                supportingText = "Removes them from the server as well",
                checked = second,
                onCheckedChange = { second = it },
            )
            SwitchRow(label = "Sync watchlists", checked = true, onCheckedChange = {}, enabled = false)
        }
    }
}

/** A long label and a two-line supporting line, for the large-font frames: the row grows past its minimum height, nothing clips. */
@Composable
fun SwitchRowWorstCaseSample() {
    var checked by remember { mutableStateOf(true) }
    ScreenshotTheme {
        Column(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_m))) {
            SwitchRow(
                label = "Also delete the files from the server and every copy",
                supportingText = "The files are removed from the server as well, and this cannot be undone once it has finished",
                supportingMaxLines = 2,
                checked = checked,
                onCheckedChange = { checked = it },
            )
        }
    }
}

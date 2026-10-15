package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Movie
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.binge.designsystem.tv.component.TvSideSheetPanel
import com.binge.designsystem.tv.component.TvSideSheetRow
import com.binge.designsystem.tv.component.TvSideSheetRows
import com.binge.designsystem.tv.component.TvSideSheetTitle
import kotlinx.coroutines.delay

private const val ROW_STANDARD = "standard"
private const val ROW_4K = "4k"
private const val ROW_SEASONS = "seasons"
private const val DEMO_SAVING_MILLIS = 2_000L

/**
 * A sheet row whose press is saving: the focused row holds a spinner where a tick would go, before its chevron, while
 * the rows around it rest. OK on another row moves the spinner there.
 */
@Composable
fun TvSideSheetRowSavingSample() {
    var saving by remember { mutableStateOf<String?>(ROW_STANDARD) }
    SavingSheet(saving = saving, focused = ROW_STANDARD, onPress = { saving = it })
}

/** The real sheet: OK on a row saves for two seconds, its spinner turning, then the row rests again. */
@Composable
fun TvSideSheetRowSavingDemo() {
    var saving by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(saving) {
        if (saving != null) {
            delay(DEMO_SAVING_MILLIS)
            saving = null
        }
    }
    SavingSheet(saving = saving, focused = null, onPress = { if (saving == null) saving = it })
}

@Composable
private fun SavingSheet(
    saving: String?,
    focused: String?,
    onPress: (String) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        TvSideSheetPanel {
            TvSideSheetTitle("Request")
            TvSideSheetRows {
                TvSideSheetRow(
                    label = "Request",
                    icon = Icons.Filled.Hd,
                    onClick = { onPress(ROW_STANDARD) },
                    initiallyFocused = focused == ROW_STANDARD,
                    saving = saving == ROW_STANDARD,
                )
                TvSideSheetRow(
                    label = "Request in 4K",
                    icon = Icons.Filled.HighQuality,
                    onClick = { onPress(ROW_4K) },
                    initiallyFocused = focused == ROW_4K,
                    saving = saving == ROW_4K,
                )
                TvSideSheetRow(
                    label = "Choose seasons",
                    icon = Icons.Filled.Movie,
                    onClick = { onPress(ROW_SEASONS) },
                    initiallyFocused = focused == ROW_SEASONS,
                    opensPage = true,
                    saving = saving == ROW_SEASONS,
                )
            }
        }
    }
}

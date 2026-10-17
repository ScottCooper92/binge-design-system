@file:OnePerScreen(fullScreen = true)

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.binge.designsystem.component.BingeChoiceList
import com.binge.designsystem.component.SingleChoiceList
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.BingeScreenScaffold
import com.binge.designsystem.template.ScreenBar
import com.binge.designsystem.theme.BingeShapes

/** A settings page that picks a language under its own top bar: the choice sheet's list with no header of its own. */
@Composable
fun SingleChoiceListScreenSample() {
    var selected by remember { mutableStateOf("es") }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        BingeScreenScaffold(title = "Language", onBack = LocalDemoBack.current, bar = ScreenBar.Small) { padding ->
            SingleChoiceList(
                choices = BingeChoiceList.Ready(SampleLanguages),
                selected = selected,
                onSelect = { selected = it },
                underNavigationBar = true,
                modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
            )
        }
    }
}

/**
 * The list at the foot of a sheet, reserving the navigation bar itself: the sheet's colour runs on under the last row,
 * so the row is not drawn under the bar.
 */
@Composable
fun SingleChoiceListUnderNavigationBarSample() {
    var selected by remember { mutableStateOf("es") }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Spacer(modifier = Modifier.weight(1f))
            SingleChoiceList(
                choices = BingeChoiceList.Ready(SampleLanguages),
                selected = selected,
                onSelect = { selected = it },
                underNavigationBar = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(BingeShapes.HeroTop)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            )
        }
    }
}

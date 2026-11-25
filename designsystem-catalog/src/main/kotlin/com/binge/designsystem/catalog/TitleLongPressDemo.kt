@file:OnePerScreen(fullScreen = true)
@file:CatalogGroup("Top app bars")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.BingeScreenScaffold
import com.binge.designsystem.template.ScreenBar

/** Long-press the title, or invoke its labelled accessibility action, to count the action. */
@Composable
fun TitleLongPressDemo() {
    var presses by rememberSaveable { mutableIntStateOf(0) }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        BingeScreenScaffold(
            title = "Long-press this title",
            bar = ScreenBar.Small,
            onTitleLongClick = { presses++ },
            onTitleLongClickLabel = "Count a long press",
        ) { padding -> Text("Long presses: $presses", Modifier.padding(padding)) }
    }
}

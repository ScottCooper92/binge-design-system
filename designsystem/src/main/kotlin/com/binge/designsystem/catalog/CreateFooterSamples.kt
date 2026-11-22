@file:OnePerScreen(fullScreen = true)
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.binge.designsystem.preview.PreviewSystemBarInsets
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.BingeScreenScaffold
import com.binge.designsystem.template.CreateFooter
import com.binge.designsystem.template.ScreenBar

@Composable
private fun CreateFooterPage(enabled: Boolean, loading: Boolean) {
    var created by remember { mutableStateOf(0) }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        BingeScreenScaffold(
            title = "Sliders",
            onBack = LocalDemoBack.current,
            bar = ScreenBar.Small,
            bottomBar = {
                CreateFooter(label = "Add slider", icon = Icons.Filled.Add, onClick = { created++ }, enabled = enabled, loading = loading)
            },
        ) { padding ->
            SampleRows(padding)
        }
    }
}

/** [CreateFooter] on a list page, at rest: the one primary under the rows, with the add glyph. Tap it to count a create. */
@Composable
fun CreateFooterSample() = CreateFooterPage(enabled = true, loading = false)

/** [CreateFooter] while the list is still loading, so the action cannot be used yet. */
@Composable
fun CreateFooterDisabledSample() = CreateFooterPage(enabled = false, loading = false)

/** [CreateFooter] with its create in flight: the button shows its busy state and takes no taps. */
@Composable
fun CreateFooterLoadingSample() = CreateFooterPage(enabled = true, loading = true)

/** [CreateFooter] under a 48dp navigation bar, which a frame reports as zero tall unless it asks: the button must clear it. */
@Composable
fun CreateFooterUnderSystemBarsSample() {
    PreviewSystemBarInsets { CreateFooterPage(enabled = true, loading = false) }
}

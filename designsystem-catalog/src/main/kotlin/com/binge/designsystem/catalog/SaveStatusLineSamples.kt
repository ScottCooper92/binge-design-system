@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.ListItem
import com.binge.designsystem.component.SaveStatusLine
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.FormSaveStatus

/** A switch that saves as it is flipped, under which the line says the write is on its way. */
@Composable
fun SaveStatusLineSavingSample() {
    SaveStatusSampleFrame(on = true, status = FormSaveStatus.Saving)
}

/** The write landed. The line stays until the next change starts. */
@Composable
fun SaveStatusLineSavedSample() {
    SaveStatusSampleFrame(on = true, status = FormSaveStatus.Saved)
}

/** The write could not be sent. The change stays on screen, and Retry sends it again. */
@Composable
fun SaveStatusLineFailedSample() {
    SaveStatusSampleFrame(on = true, status = FormSaveStatus.Failed(onRetry = {}))
}

@Composable
private fun SaveStatusSampleFrame(on: Boolean, status: FormSaveStatus) {
    ScreenshotTheme {
        ItemGroup(
            title = null,
            rows = listOf(ListItem(icon = Icons.Filled.Notifications, label = "Notify me", toggled = on)),
            belowRows = { SaveStatusLine(status) },
        )
    }
}

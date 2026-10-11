@file:OnePerScreen(fullScreen = true)

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.FormSaveStatus
import com.binge.designsystem.template.FormScreen
import com.binge.designsystem.template.FormSection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SAVE_MILLIS = 800L

/**
 * A save-as-made form for real, which a frame of one status cannot show. Each switch saves as it is flipped:
 * "Saving…", then "Saved". Turn on "Fail the next save" and the next change fails instead: it stays flipped, and
 * a snackbar offers Retry until a save succeeds.
 */
@Composable
fun FormScreenSaveAsMadeDemo() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        var status by remember { mutableStateOf<FormSaveStatus>(FormSaveStatus.Idle) }
        var notify by remember { mutableStateOf(true) }
        var sync by remember { mutableStateOf(false) }
        var failNext by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        fun save() {
            status = FormSaveStatus.Saving
            scope.launch {
                delay(SAVE_MILLIS)
                if (failNext) {
                    failNext = false
                    status = FormSaveStatus.Failed(onRetry = ::save)
                } else {
                    status = FormSaveStatus.Saved
                }
            }
        }
        FormScreen(title = "Notifications", onBack = LocalDemoBack.current, saveStatus = status) {
            FormSection(title = "Alerts") {
                DemoSwitch("Notify me", notify) {
                    notify = it
                    save()
                }
                DemoSwitch("Sync in the background", sync) {
                    sync = it
                    save()
                }
            }
            FormSection(title = "Demo") { DemoSwitch("Fail the next save", failNext) { failNext = it } }
        }
    }
}

@Composable
private fun DemoSwitch(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

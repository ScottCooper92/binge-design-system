@file:OnePerScreen
@file:CatalogGroup("Save as made")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.SaveStatus
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.ListItem
import com.binge.designsystem.component.SaveStatusLine
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.save.SaveAsMade
import com.binge.designsystem.template.FormSaveStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

private const val WRITE_MILLIS = 800L

/** A write that cannot be sent right now, which the engine keeps for Retry; any other failure it would drop. */
private class Offline : Exception()

/**
 * A switch wired to the real [SaveAsMade] engine, which a frame cannot show: flip it and the line says Saving, then
 * Saved. Turn the pretend connection off and the next flip fails, the change stays, and Retry sends it once the
 * connection is back.
 */
@Composable
fun SaveStatusLineDemo() {
    ScreenshotTheme {
        val scope = rememberCoroutineScope()
        var on by remember { mutableStateOf(false) }
        var online by remember { mutableStateOf(true) }
        var status by remember { mutableStateOf(SaveStatus.Idle as SaveStatus) }
        var failed by remember { mutableStateOf(false) }
        val engine =
            remember {
                SaveAsMade(
                    scope = scope,
                    appScope = scope,
                    dispatcher = Dispatchers.Default,
                    draft = { on },
                    canSave = { true },
                    write = { value ->
                        delay(WRITE_MILLIS)
                        if (!online) throw Offline()
                        value
                    },
                    adopt = { sent, _ -> on != sent },
                    failed = { failed = it },
                    isTransient = { it is Offline },
                    status = { status = it },
                )
            }
        val shown =
            when {
                failed -> FormSaveStatus.Failed(onRetry = engine::now)
                status == SaveStatus.Saving -> FormSaveStatus.Saving
                status == SaveStatus.Saved -> FormSaveStatus.Saved
                else -> FormSaveStatus.Idle
            }
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_m))) {
            ItemGroup(
                title = null,
                rows =
                    listOf(
                        ListItem(
                            icon = Icons.Filled.Notifications,
                            label = "Notify me",
                            toggled = on,
                            onClick = {
                                on = !on
                                engine.changed()
                            },
                        ),
                        ListItem(
                            icon = Icons.Filled.Notifications,
                            label = "Pretend connection is up",
                            toggled = online,
                            onClick = { online = !online },
                        ),
                    ),
                belowRows = { SaveStatusLine(shown) },
            )
            Text("The second switch is the demo's own control, not a saved setting.")
        }
    }
}

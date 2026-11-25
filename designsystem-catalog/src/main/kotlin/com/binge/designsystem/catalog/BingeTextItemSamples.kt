@file:CatalogGroup("Settings")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.TextSettingContent
import com.binge.designsystem.component.bingeTextItem
import com.binge.designsystem.preview.ScreenshotTheme

private const val EMPTY = "Not set"
private const val DONE = "Done"

/**
 * [bingeTextItem] in a group, as its rows read at rest: a value, one left empty, a required one left blank, a value the row's own
 * check refuses, and a key shown masked. Tap one to edit it in its sheet; Done saves and closes at once. The words are the sample's own.
 */
@Composable
fun BingeTextItemSample() {
    var address by remember { mutableStateOf("media.local:8096") }
    var note by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var path by remember { mutableStateOf("/media tv") }
    var key by remember { mutableStateOf("a1b2c3d4") }
    ScreenshotTheme {
        ItemGroup(
            title = "Server",
            rows =
                listOf(
                    bingeTextItem(Icons.Filled.Dns, "Address", address, { address = it }, EMPTY, DONE, hint = "Where the server answers"),
                    bingeTextItem(Icons.Filled.Notes, "Note", note, { note = it }, EMPTY, DONE, multiline = true),
                    bingeTextItem(Icons.Filled.Dns, "Name", name, { name = it }, EMPTY, DONE, requiredLabel = "Required"),
                    bingeTextItem(
                        Icons.Filled.Dns,
                        "Library path",
                        path,
                        { path = it },
                        EMPTY,
                        DONE,
                        check = { if (it.contains(' ')) "No spaces" else null },
                    ),
                    bingeTextItem(Icons.Filled.Key, "API key", key, { key = it }, EMPTY, DONE, secret = true, shown = "••••••••"),
                ),
        )
    }
}

/**
 * [TextSettingContent], the sheet a [bingeTextItem] opens, drawn directly: empty with a placeholder and a hint,
 * refused by a check once something is typed, and a masked secret. Each keeps its own text, so the field can be typed in.
 */
@Composable
fun TextSettingContentSample() {
    var empty by remember { mutableStateOf("") }
    var refused by remember { mutableStateOf("/media tv") }
    var secret by remember { mutableStateOf("a1b2c3d4") }
    ScreenshotTheme {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
        ) {
            TextSettingContent(
                "Address",
                empty,
                { empty = it },
                {},
                {},
                DONE,
                hint = "Where the server answers",
                placeholder = "media.local",
            )
            TextSettingContent("Library path", refused, { refused = it }, {}, {}, DONE, problem = "No spaces")
            TextSettingContent("API key", secret, { secret = it }, {}, {}, DONE, secret = true)
        }
    }
}

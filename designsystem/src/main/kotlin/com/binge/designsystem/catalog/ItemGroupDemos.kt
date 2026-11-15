@file:OnePerScreen(fullScreen = true)

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.ListItem
import com.binge.designsystem.component.ProvideRowFocus
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * A long screen of keyed groups: each button opens it on a row that is not on screen, and [ProvideRowFocus] steps the list to it,
 * scrolls it into view, pulses it and leaves it highlighted. A row is shown again only after the key has changed, so clear it first.
 */
@Composable
fun ItemGroupRowFocusDemo() {
    var target by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    ScreenshotTheme {
        ProvideRowFocus(key = target, listState = listState) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(dimensionResource(R.dimen.padding_l)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
            ) {
                item {
                    Column {
                        BingeFilledButton(label = "Find row 2.1", onClick = {
                            target = null
                            target = "row-2-1"
                        })
                        BingeFilledButton(label = "Find row 14.2", onClick = {
                            target = null
                            target = "row-14-2"
                        })
                    }
                }
                items(DEMO_GROUPS) { group ->
                    ItemGroup(
                        title = "Group $group",
                        rows = List(DEMO_ROWS) { row ->
                            ListItem(icon = Icons.Filled.Settings, label = "Row $group.$row", key = "row-$group-$row")
                        },
                    )
                }
            }
        }
    }
}

private const val DEMO_GROUPS = 16
private const val DEMO_ROWS = 3

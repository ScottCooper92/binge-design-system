@file:CatalogGroup("Top app bars")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeBarIconAction
import com.binge.designsystem.component.BingeOverflowMenu
import com.binge.designsystem.component.OverflowItem
import com.binge.designsystem.component.OverflowMenuItems
import com.binge.designsystem.preview.ScreenshotTheme

private fun sampleItems(onClick: (String) -> Unit = {}) =
    listOf(
        OverflowItem("Edit", Icons.Filled.Edit, onClick = { onClick("Edit") }),
        OverflowItem("Delete", Icons.Filled.Delete, onClick = { onClick("Delete") }, destructive = true),
        OverflowItem("Share", Icons.Filled.Share, onClick = { onClick("Share") }, enabled = false),
    )

/**
 * The entries of a [BingeOverflowMenu] on the menu surface: Edit, Share (disabled, so dimmed) and Delete (destructive, so
 * red and last although it was given second). A real menu is a popup a frame cannot capture, so this draws its content.
 */
@Composable
fun BingeOverflowMenuSample() {
    ScreenshotTheme {
        Surface(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
            shape = MaterialTheme.shapes.extraSmall,
            tonalElevation = MenuDefaults.TonalElevation,
            shadowElevation = MenuDefaults.ShadowElevation,
        ) {
            Column(modifier = Modifier.width(IntrinsicSize.Max)) { OverflowMenuItems(sampleItems(), onDismissRequest = {}) }
        }
    }
}

/** A [BingeOverflowMenu] opened for real from a more button: tap an entry and the menu closes. */
@Composable
fun BingeOverflowMenuDemo() {
    ScreenshotTheme {
        var open by remember { mutableStateOf(false) }
        Box(modifier = Modifier.padding(dimensionResource(R.dimen.padding_m))) {
            BingeBarIconAction(Icons.Filled.MoreVert, "More", onClick = { open = true })
            BingeOverflowMenu(expanded = open, onDismissRequest = { open = false }, items = sampleItems())
        }
    }
}

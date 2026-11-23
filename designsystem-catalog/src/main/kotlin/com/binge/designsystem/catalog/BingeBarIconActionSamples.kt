@file:CatalogGroup("Top app bars")

package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.BingeBarIconAction
import com.binge.designsystem.component.BingeMediumTopBar
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Three [BingeBarIconAction]s in a transparent top bar: Search, Add, and a Sort that is disabled. Each takes the bar's
 * tone, tint and glass fade, so they are scrim-backed circles beside Back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeBarIconActionSample() {
    ScreenshotTheme {
        BingeMediumTopBar(
            title = "Users",
            onBack = {},
            actions = { glassBackgroundAlpha ->
                BingeBarIconAction(Icons.Filled.Search, "Search", onClick = {}, glassBackgroundAlpha = glassBackgroundAlpha)
                BingeBarIconAction(Icons.Filled.Add, "Add", onClick = {}, glassBackgroundAlpha = glassBackgroundAlpha)
                BingeBarIconAction(
                    Icons.Filled.SwapVert,
                    "Sort",
                    onClick = {},
                    enabled = false,
                    glassBackgroundAlpha = glassBackgroundAlpha,
                )
            },
        )
    }
}

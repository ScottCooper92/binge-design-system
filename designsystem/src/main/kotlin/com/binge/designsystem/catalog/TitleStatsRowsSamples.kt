@file:CatalogGroup("Detail pages")

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.TitleStatsRows
import com.binge.designsystem.preview.ScreenshotTheme

/** A title's four stats under its hero: all on one row, and two to a row from a font scale of 1.5. */
@Composable
fun TitleStatsRowsSample() {
    ScreenshotTheme { TitleStatsRows(MediaPageStats) }
}

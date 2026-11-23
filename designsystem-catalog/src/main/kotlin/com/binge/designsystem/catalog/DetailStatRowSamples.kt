package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.DetailStat
import com.binge.designsystem.component.DetailStatRow
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [DetailStatRow] — divided icon/value/label cells spread across the detail header.
 * See the convention KDoc on [MediaCardRatedSample].
 */
@Composable
fun DetailStatRowSample() {
    ScreenshotTheme {
        DetailStatRow(
            stats = listOf(
                DetailStat(Icons.Filled.Star, "8.5", "Rating"),
                DetailStat(Icons.Filled.ThumbUp, "12.4K", "Votes"),
            ),
        )
    }
}

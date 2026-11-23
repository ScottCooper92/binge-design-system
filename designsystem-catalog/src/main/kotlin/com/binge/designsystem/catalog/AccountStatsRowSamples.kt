package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.AccountStat
import com.binge.designsystem.component.AccountStatsRow
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [AccountStatsRow] — a full-width row of equal-weight stat tiles (value over
 * label). See the convention KDoc on [MediaCardRatedSample].
 */
@Composable
fun AccountStatsRowSample() {
    ScreenshotTheme {
        AccountStatsRow(
            stats = listOf(
                AccountStat("42", "Watchlist"),
                AccountStat("186", "Watched"),
                AccountStat("23", "Reviews"),
            ),
        )
    }
}

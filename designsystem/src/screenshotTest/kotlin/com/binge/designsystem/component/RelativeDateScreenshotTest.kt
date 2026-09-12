package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.formatRelativeOrAbsolute
import com.binge.designsystem.preview.ScreenshotTheme

// A fixed reference instant so the relative spans below never depend on the wall clock — without it
// the rendered "X ago" copy would drift every day and break the committed baselines.
private const val FIXED_NOW = 1_781_222_400_000L // 12 June 2026 00:00 UTC

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/** The relative-or-absolute date formatter across its span tiers, anchored to a fixed [FIXED_NOW]. */
class RelativeDateScreenshotTest {
    @PreviewTest
    @Preview(name = "light", widthDp = 240)
    @Composable
    fun Spans() {
        ScreenshotTheme {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                RelativeDateLine(FIXED_NOW)
                RelativeDateLine(FIXED_NOW - 30 * MINUTE)
                RelativeDateLine(FIXED_NOW - 5 * HOUR)
                RelativeDateLine(FIXED_NOW - 6 * DAY)
                RelativeDateLine(FIXED_NOW - 20 * DAY)
                // Past the 30-day window: renders the absolute long-date fallback.
                RelativeDateLine(FIXED_NOW - 120 * DAY)
            }
        }
    }
}

@Composable
private fun RelativeDateLine(timeMillis: Long) {
    Text(
        text = formatRelativeOrAbsolute(timeMillis, now = FIXED_NOW).orEmpty(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

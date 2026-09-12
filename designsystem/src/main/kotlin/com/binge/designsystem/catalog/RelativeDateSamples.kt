package com.binge.designsystem.catalog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.binge.designsystem.formatRelativeOrAbsolute
import com.binge.designsystem.preview.ScreenshotTheme

private const val FIXED_NOW_MILLIS = 1_718_000_000_000L
private const val SIX_DAYS_MILLIS = 6L * 24 * 60 * 60 * 1000

/**
 * `formatRelativeOrAbsolute` is a string formatter, not a UI element, so the sample renders its
 * result as [Text]. A fixed [FIXED_NOW_MILLIS] reference keeps the relative span stable — the wall
 * clock would drift daily and break the committed baselines.
 */
@Composable
fun RelativeDateSample() {
    ScreenshotTheme {
        val label = formatRelativeOrAbsolute(FIXED_NOW_MILLIS - SIX_DAYS_MILLIS, now = FIXED_NOW_MILLIS)
        Text(text = label.orEmpty(), style = MaterialTheme.typography.bodyMedium)
    }
}

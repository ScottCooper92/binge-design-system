@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.formatRelativeOrAbsolute
import com.binge.designsystem.preview.ScreenshotTheme
import java.time.ZoneOffset

private const val FIXED_NOW_MILLIS = 1_718_000_000_000L

/**
 * `formatRelativeOrAbsolute` is a string formatter, not a UI element, so the sample renders its
 * results as [Text], one per age it asks for: minutes, hours and days, a three-week age, then the absolute date once
 * the age passes the relative window, and for a time in the future. Each line names the age it was
 * given, so the sample says what it shows. A fixed [FIXED_NOW_MILLIS] keeps the spans stable, and UTC
 * keeps the absolute dates on the same day wherever the frame is rendered.
 */
@Composable
fun RelativeDateSample() {
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs))) {
            RELATIVE_DATE_SPANS.forEach { (age, offset) ->
                val label = formatRelativeOrAbsolute(FIXED_NOW_MILLIS - offset, now = FIXED_NOW_MILLIS, zone = ZoneOffset.UTC)
                Text(text = "$age: ${label.orEmpty()}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private const val MINUTE_MILLIS = 60L * 1000
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
private const val DAY_MILLIS = 24 * HOUR_MILLIS

/** Each age the sample shows, named, and how far before the fixed now it falls; negative is ahead. */
private val RELATIVE_DATE_SPANS =
    listOf(
        "5 minutes old" to 5 * MINUTE_MILLIS,
        "3 hours old" to 3 * HOUR_MILLIS,
        "6 days old" to 6 * DAY_MILLIS,
        "3 weeks old" to 21 * DAY_MILLIS,
        "45 days old, past the window" to 45 * DAY_MILLIS,
        "2 days ahead" to -2 * DAY_MILLIS,
    )

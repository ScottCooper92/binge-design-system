package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.ExpandableOverview
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Copy longer than the collapsed three lines: collapsed, with the Show more toggle. Seeds
 * `initiallyOverflowing` so the screenshot frame holds the toggle; on a device the layout pass finds
 * the same overflow on its own.
 */
@Composable
fun ExpandableOverviewSample() {
    ScreenshotTheme {
        ExpandableOverview(
            text = "When the menace known as the Joker wreaks havoc and chaos on the people of " +
                "Gotham, Batman must accept one of the greatest psychological and physical tests of " +
                "his ability to fight injustice. Gotham's new district attorney Harvey Dent joins " +
                "the cause, but the trio soon find themselves prey to a reign of terror.",
            initiallyOverflowing = true,
        )
    }
}

/** Copy that fits in the collapsed three lines: no toggle, since there is nothing more to show. */
@Composable
fun ExpandableOverviewShortSample() {
    ScreenshotTheme {
        ExpandableOverview(text = "A short overview that fits within three lines.")
    }
}

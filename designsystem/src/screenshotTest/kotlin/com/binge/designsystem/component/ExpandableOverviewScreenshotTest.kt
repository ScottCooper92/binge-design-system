package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Collapsible overview text. The short variant fits in three lines so no toggle appears; the long
 * variant overflows the collapsed limit and surfaces the "Show more" button. The long case is
 * reflow-sensitive (truncation depends on width) so it runs the phone matrix.
 *
 * [LongCollapsed] seeds `initiallyOverflowing`, without which the toggle it is named for never
 * reached the frame: overflow is only reported by `onTextLayout`, one composition too late for the
 * preview lane to capture.
 */
class ExpandableOverviewScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Short() {
        ScreenshotTheme {
            ExpandableOverview(text = "A short overview that fits within three lines.")
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun LongCollapsed() {
        ScreenshotTheme {
            ExpandableOverview(
                text = "A billionaire dons a bat costume and wages a one-man war on crime in " +
                    "Gotham City. When a new criminal known as the Joker emerges, Batman must " +
                    "confront chaos itself. The Dark Knight faces his greatest test as moral lines " +
                    "blur and the city is pushed to the brink. This overview overflows the collapsed " +
                    "state to reveal the 'Show more' toggle.",
                initiallyOverflowing = true,
            )
        }
    }
}

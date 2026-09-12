package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.ExpandableOverview
import com.binge.designsystem.preview.ScreenshotTheme

@Composable
fun ExpandableOverviewSample() {
    ScreenshotTheme {
        ExpandableOverview(
            text = "When the menace known as the Joker wreaks havoc and chaos on the people of " +
                "Gotham, Batman must accept one of the greatest psychological and physical tests of " +
                "his ability to fight injustice. Gotham's new district attorney Harvey Dent joins " +
                "the cause, but the trio soon find themselves prey to a reign of terror.",
        )
    }
}

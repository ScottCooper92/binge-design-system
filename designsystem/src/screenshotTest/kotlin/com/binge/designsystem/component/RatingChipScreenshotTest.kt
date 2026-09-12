package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * Example Compose screenshot test. Each `@PreviewTest`-annotated `@Preview` in the
 * `screenshotTest` source set is rendered to a reference image by
 * `updateDebugScreenshotTest` and diffed by `validateDebugScreenshotTest`.
 *
 * `@PreviewTest` is required — previews without it are not executed.
 * `dynamicColor = false` keeps colours deterministic across machines.
 */
class RatingChipScreenshotTest {
    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun RatingChipLight() {
        BingeExpressiveTheme(darkTheme = false, dynamicColor = false) {
            RatingChip(rating = 8.5f, size = RatingChipSize.Lg)
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun RatingChipDark() {
        BingeExpressiveTheme(darkTheme = true, dynamicColor = false) {
            RatingChip(rating = 8.5f, size = RatingChipSize.Lg, tone = RatingChipTone.Surface)
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun RatingChipAccent() {
        BingeExpressiveTheme(darkTheme = false, dynamicColor = false) {
            RatingChip(rating = 8.5f, size = RatingChipSize.Sm, tone = RatingChipTone.Accent)
        }
    }
}

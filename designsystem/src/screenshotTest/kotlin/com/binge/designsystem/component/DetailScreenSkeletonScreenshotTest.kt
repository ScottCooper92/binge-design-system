package com.binge.designsystem.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.component.DetailSkeletonShape
import com.binge.designsystem.preview.ScreenPreviews
import com.binge.designsystem.preview.ScreenStatePreview
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Full-screen detail loading skeleton. Rendered across the device-class matrix so the compact hero
 * shape and the expanded cinematic (backdrop + inline poster) shape are both baselined — the skeleton
 * adapts at `isExpandedLayout()`, so a fixed-width preview would miss the expanded branch.
 *
 * **`phone-land` is a header-band cell, not full coverage.** At 411dp of height the compact hero
 * (`detail_hero_height`, 300dp under `values-land`) and the stat row fill the viewport, and a preview
 * captures scroll offset zero, so anything the shapes differ by *below* the header is off-frame. Making
 * the skeleton scroll (#2043) fixed the device-side fault — that content was unreachable, and the last
 * band was squashed into the leftover height rather than merely cut off — but a scrolled frame is not
 * something a `@Preview` can ask for.
 *
 * What it does separate is the header band itself. 891dp of width is `isExpandedLayout()`, so `Loading`
 * takes the cinematic header here and the two episode shapes take the compact hero (#2032); before that
 * gate all three were byte-identical. `LoadingEpisode` and `LoadingEpisodeSignedOut` remain identical to
 * each other in this cell, and correctly so — they differ only by the rating card, which is below the
 * fold. The `phone` and `phone-light` cells are what separate those two.
 */
class DetailScreenSkeletonScreenshotTest {
    @PreviewTest
    @ScreenPreviews
    @Composable
    fun Loading() {
        ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            DetailScreenSkeleton()
        }
    }

    @PreviewTest
    @ScreenPreviews
    @Composable
    fun PersonLoading() {
        ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            DetailScreenSkeleton(shape = DetailSkeletonShape.Person)
        }
    }

    @PreviewTest
    @ScreenPreviews
    @Composable
    fun LoadingEpisode() {
        ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            DetailScreenSkeleton(shape = DetailSkeletonShape.Episode)
        }
    }

    /** The signed-out episode page: no rating card between the stat row and the overview (#1991). */
    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun LoadingEpisodeSignedOut() {
        ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            DetailScreenSkeleton(shape = DetailSkeletonShape.EpisodeSignedOut)
        }
    }
}

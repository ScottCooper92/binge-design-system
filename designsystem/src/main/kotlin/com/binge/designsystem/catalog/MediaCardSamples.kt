package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.MediaCard
import com.binge.designsystem.component.MediaCardAction
import com.binge.designsystem.component.MediaCardSkeleton
import com.binge.designsystem.component.MediaTypeTagType
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Shared public-sample convention — the worked example.
 *
 * Without it a component's preview drifts across two places: a `private` inline `@Preview` (IDE
 * only) and a `screenshotTest @PreviewTest @Preview` (CI only). This `…Samples` layer gives the
 * component one public sample that both call, so the IDE render and the asserted screenshot are
 * pixel-identical, and the committed baselines double as the browsable catalog
 * (`tools/screenshot-gallery`).
 *
 * The convention:
 * 1. **One `<Component>Samples.kt` per component** in this `catalog/` package — keeps files under the
 *    400-line gate.
 * 2. **No-arg public `@Composable` samples**, one representative state each, so every consumer calls
 *    them identically. Add one per visually distinct variant.
 * 3. **Wrap in [ScreenshotTheme]** for deterministic render; the light/dark axis is the caller's
 *    `@Preview(uiMode = …)` (the screenshot test stacks both).
 * 4. **Fold `*PreviewData` builders in** as the data source; inline a single literal rather than
 *    inventing a builder. Sample files are preview tooling (kover/detekt exempt).
 * 5. **Compact, space-free `name` tokens** on the screenshot `@Preview` (see `DevicePreviews.kt`).
 *
 * A media card is a fixed [R.dimen.card_width] poster cell, so the sample bounds its own width to
 * match how it renders inside a carousel/grid.
 */
@Composable
fun MediaCardRatedSample() {
    ScreenshotTheme {
        MediaCard(
            posterUrl = null,
            title = "The Dark Knight",
            rating = 9.0f,
            onClick = {},
            modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
        )
    }
}

/**
 * The placeholder the grids draw in a card's place, at the same width, so the catalogued pair shows what a
 * cell reserves against what it fills — the comparison the skeleton exists to survive.
 */
@Composable
fun MediaCardSkeletonSample() {
    ScreenshotTheme {
        MediaCardSkeleton(modifier = Modifier.width(dimensionResource(R.dimen.card_width)))
    }
}

/**
 * Library poster variant: the contextual top-end action (a remove X), a movie/TV type badge
 * top-start for mixed collections, and the user's own rating as an accent chip bottom-end — the
 * metadata that survives the row→poster switch.
 */
@Composable
fun MediaCardLibrarySample() {
    ScreenshotTheme {
        MediaCard(
            posterUrl = null,
            title = "Past Lives",
            rating = 7.8f,
            onClick = {},
            topEndAction = MediaCardAction(icon = Icons.Filled.Close, contentDescription = "Remove", onClick = {}),
            typeBadge = MediaTypeTagType.Movie,
            userRating = 9.0f,
            modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
        )
    }
}

/** Unrated card with a long title that wraps to the two-line cap — the reflow variant. */
@Composable
fun MediaCardUnratedSample() {
    ScreenshotTheme {
        MediaCard(
            posterUrl = null,
            title = "A Very Long Movie Title That Should Wrap Or Truncate",
            rating = null,
            onClick = {},
            modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
        )
    }
}

package com.binge.designsystem.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Asserts a screen's skeleton reserves the geometry its resolved content fills: the invariant that loading
 * must not move what it stands in for, as a test rather than a review note.
 *
 * The check drives **one composition through the resolve**, rather than measuring two separate ones: it
 * composes the loading state, records where each anchor sits, flips the caller's flag, and measures again.
 * That is the moment the user actually sees, so a failure reports the jump they would have seen, in dp.
 *
 * Four ways to fail, and all matter:
 *
 * - **The anchor moves down the page.** The skeleton reserved the wrong amount of space, so content shifts
 *   on resolve.
 * - **The anchor moves across the page**, when [checkStartEdge] is on. The skeleton reserved the right
 *   height at the wrong inset — what a page that centres its reading column at an expanded width does to a
 *   skeleton still using the flat phone padding. Checking only the top edge let exactly that ship.
 * - **The anchor changes size**, when [checkSize] is on. A position-only check is blind to a row or grid
 *   whose cells disagree in *count*: a `Row` of weighted tiles anchored at index 0 starts at the same corner
 *   whether it holds four or eight, so the skeleton and the content agree on every edge this harness
 *   measured while every tile after the first landed somewhere else.
 * - **The anchor is missing on one side.** The skeleton promised a section the page does not lead with —
 *   a skeleton showing a rail of cast circles where the page leads with a seasons row, say. An offset
 *   comparison alone would not have caught it, because the wrong row still sat at roughly the right height.
 *
 * Bounds are taken **unclipped**: `getBoundsInRoot` reports a node clipped out of the viewport as zero-sized,
 * which would read as agreement between two things that are not there. Any test measuring scrolled content
 * needs the same.
 *
 * A tolerance exists because sub-pixel rounding differs between a plate and the text that replaces it; it is
 * deliberately tight. Widening it past a couple of dp defeats the check — reshape the skeleton instead.
 *
 * @param checkStartEdge opt in to the across-the-page comparison. Off by default because a semantics node's
 * bounds are taken where its semantics sit in the modifier chain, not at the layout node's outer edge: an
 * *interactive* content band — a `clickable` row merging a `Role: Button` node inside its own 16dp padding —
 * reports bounds 16dp in, while the inert skeleton plate standing in for it reports the outer edge. Measured, skeleton `Rect(0, 888, 822, 1040)` against content
 * `Rect(32, 888, 790, 1040)`: the same pixels on screen, a different node carrying the tag. On everywhere,
 * that fails a whole screen's tests over nothing a user could see. Screens whose two trees tag structurally
 * comparable nodes opt in and get the real check.
 *
 * @param checkSize opt in to the cell-size comparison, for a skeleton whose anchor is one cell of a row or
 * grid rather than a full-width band. Off by default for the same reason [checkStartEdge] is — a tag carried
 * by an inner interactive node reports that node's size, not the band's — plus a plainer one: a single-column
 * list has no count to disagree on, so the check would only add a second way for sub-pixel rounding to fail.
 * An anchor holding text needs `@GraphicsMode(NATIVE)` on the test: Robolectric's legacy mode measures text at
 * one pixel per character and never wraps it, so the band's height is fiction.
 *
 * The harness lives here, in the phone design system, rather than beside either surface's anchors: it names
 * no Material type at all — only `testTag` and bounds — so the tv-material separation the two component
 * layers keep does not reach it, and both surfaces assert the invariant with one implementation. TV callers
 * measure `com.binge.designsystem.tv.layout.TvLayoutAnchors`; phone callers measure
 * [com.binge.designsystem.layout.LayoutAnchors]. A consumer depends on it with
 * `testImplementation(testFixtures(libs.binge.designsystem))`, which the composite build's existing module
 * substitution resolves (#183).
 */
fun ComposeContentTestRule.assertSkeletonReservesGeometry(
    anchors: List<String>,
    tolerance: Dp = DEFAULT_TOLERANCE,
    checkStartEdge: Boolean = false,
    checkSize: Boolean = false,
    content: @Composable (resolved: Boolean) -> Unit,
) {
    require(anchors.isNotEmpty()) { "Name at least one anchor, or the assertion passes vacuously." }

    var resolved by mutableStateOf(false)
    setContent { content(resolved) }
    waitForIdle()
    val loading = anchors.associateWith { boxOf(it) }

    resolved = true
    waitForIdle()
    val settled = anchors.associateWith { boxOf(it) }

    val failures = anchors.mapNotNull { anchor ->
        describeDrift(anchor, loading[anchor], settled[anchor], tolerance, checkStartEdge, checkSize)
    }
    check(failures.isEmpty()) {
        "The skeleton does not reserve what the content fills:\n" + failures.joinToString("\n") { "  - $it" }
    }
}

/** The anchor's box, or null when no node carries it in this state. */
private fun ComposeContentTestRule.boxOf(anchor: String): AnchorBox? =
    runCatching {
        val bounds = onNodeWithTag(anchor, useUnmergedTree = true).getUnclippedBoundsInRoot()
        AnchorBox(
            top = bounds.top,
            start = bounds.left,
            width = bounds.right - bounds.left,
            height = bounds.bottom - bounds.top,
        )
    }.getOrNull()

/** What an anchor is held to: where the band sits down the page and across it, and how big it is. */
private data class AnchorBox(
    val top: Dp,
    val start: Dp,
    val width: Dp,
    val height: Dp,
)

/** One anchor's verdict, phrased so a failure names the screen's actual symptom. */
private fun describeDrift(
    anchor: String,
    loading: AnchorBox?,
    resolved: AnchorBox?,
    tolerance: Dp,
    checkStartEdge: Boolean,
    checkSize: Boolean,
): String? =
    when {
        loading == null && resolved == null ->
            "$anchor: absent from both states — the anchor is misspelled, or neither tree renders it."

        loading == null ->
            "$anchor: the resolved page renders it but the skeleton does not promise it, so it arrives unreserved."

        resolved == null ->
            "$anchor: the skeleton promises it but the resolved page does not lead with it — the skeleton is " +
                "describing a page that no longer exists."

        else -> listOfNotNull(
            describeAxis(anchor, "down the page", loading.top, resolved.top, tolerance),
            describeAxis(anchor, "across the page", loading.start, resolved.start, tolerance)
                .takeIf { checkStartEdge },
            describeResize(anchor, "wider", "narrower", loading.width, resolved.width, tolerance)
                .takeIf { checkSize },
            describeResize(anchor, "taller", "shorter", loading.height, resolved.height, tolerance)
                .takeIf { checkSize },
        ).joinToString("\n  - ").ifEmpty { null }
    }

/** One axis of one anchor, named so a failure says which way the band moved. */
private fun describeAxis(
    anchor: String,
    axis: String,
    loading: Dp,
    resolved: Dp,
    tolerance: Dp,
): String? =
    if (abs((resolved - loading).value) <= tolerance.value) {
        null
    } else {
        "$anchor: moves ${(resolved - loading).value.toInt()}dp $axis on resolve " +
            "(skeleton ${loading.value.toInt()}dp, content ${resolved.value.toInt()}dp) — content jumps rather than fills."
    }

/**
 * One dimension of one anchor. Phrased as a resize rather than a move because that is the symptom: the cell
 * stays put and every cell after it does not.
 */
private fun describeResize(
    anchor: String,
    grew: String,
    shrank: String,
    loading: Dp,
    resolved: Dp,
    tolerance: Dp,
): String? =
    if (abs((resolved - loading).value) <= tolerance.value) {
        null
    } else {
        "$anchor: the content is ${abs((resolved - loading).value).toInt()}dp " +
            (if (resolved > loading) grew else shrank) +
            " than the skeleton reserved (skeleton ${loading.value.toInt()}dp, content " +
            "${resolved.value.toInt()}dp) — the right corner at the wrong size, so the cells after it move."
    }

/** Tight on purpose: a plate and the text replacing it round differently, but only by a fraction. */
private val DEFAULT_TOLERANCE = 2.dp

package com.binge.designsystem.tv.layout

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Names the points a TV screen's **loading** and **resolved** trees must agree on.
 *
 * A skeleton exists to reserve the geometry the real content fills, so that resolving is a fill rather than a
 * relayout. Nothing enforced that: the two are separate composable trees that happened to agree only
 * because someone checked. They drift: a skeleton promises a row of circles where the resolved page leads with a
 * row of 2:3 posters, or never leaves a single flat plate.
 *
 * An anchor is the handle that makes the agreement *assertable*: both trees tag the same point with the same
 * string, and `assertSkeletonReservesGeometry` (testFixtures) measures whether it moves across the resolve.
 * The tag is deliberately keyed on the **section key** rather than a position, so a skeleton that promises
 * `cast` while the page leads with `seasons` fails by the anchor being absent — the identity is checked, not
 * just the offset.
 *
 * This is the same trade the TV surface already makes for focus: focus is a *parameter* so a focused state is
 * screenshot-testable (see `docs/tv-foundation.md`). Here a layout anchor is a tag so a reserved
 * geometry is unit-testable. Both put a small, named affordance in production to make an invariant checkable
 * rather than reviewable.
 */
object TvLayoutAnchors {
    private const val PREFIX = "tv-anchor:"

    /**
     * The anchor for one entry of a page — its hero or a content section — keyed by the entry's own key.
     *
     * A page that applies this to every entry itself gets its anchors by existing rather than by remembering to
     * tag anything.
     */
    fun entry(key: String): String = "$PREFIX$key"

    /** The key for a page's hero entry, which its skeleton counterpart has to match. */
    const val HERO_KEY: String = "hero"

    /** An immersive hub's copy band above its rows, which the hub's skeleton reserves. */
    const val HUB_COPY: String = "${PREFIX}hub-copy"

    /** An immersive hub's row at [index] among the rows it lays out, which the hub's skeleton reserves. */
    fun hubRow(index: Int): String = "${PREFIX}hub-row-$index"
}

/**
 * Marks this node as a layout anchor — a point a skeleton and its resolved content must agree on.
 *
 * A plain `testTag` today. It is a named modifier rather than a bare `testTag` call so the anchors are one
 * greppable set with one rationale ([TvLayoutAnchors]), and so that swapping the mechanism later (a semantics
 * key, a layout id) is one edit rather than a sweep.
 */
fun Modifier.tvLayoutAnchor(tag: String): Modifier = testTag(tag)

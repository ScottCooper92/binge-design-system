package com.binge.designsystem.layout

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Names the points a phone screen's **loading** and **resolved** trees must agree on.
 *
 * A skeleton exists to reserve the geometry the real content fills, so that resolving is a fill rather than a
 * relayout. Nothing enforced that on either surface: a skeleton and its content are separate
 * composable trees that agree only because someone checked. The television's drifted anyway — the show's
 * skeleton promised a row of cast circles where its seasons row lands, episode detail never migrated
 * off a flat plate — and the anchors that caught it are
 * [com.binge.designsystem.tv.layout.TvLayoutAnchors]. This is that object's phone counterpart, measured
 * by the same harness (`assertSkeletonReservesGeometry`, testFixtures).
 *
 * An anchor is the handle that makes the agreement *assertable*: both trees tag the same point with the same
 * string, and the harness measures whether it moves across the resolve. The tag is keyed on the **section's
 * identity** rather than its position, so a skeleton promising `actions` where the page leads with a rating
 * card fails by the anchor being absent — the identity is checked, not just the offset.
 *
 * The phone keeps its own object rather than sharing the television's because the two surfaces have
 * different section vocabularies, and a shared key set would invite a screen to assert against a section it
 * does not have. Only the harness — which names no section and no Material type — is shared.
 *
 * The vocabulary grows a section at a time, as each screen is brought onto the check.
 */
object LayoutAnchors {
    /** The anchor for one section of a screen, keyed by the section's own name. */
    fun section(key: String): String = "$PREFIX$key"

    /**
     * The detail screens' shared section keys.
     *
     * These are the sections the detail skeleton stands in for, and so the vocabulary a detail page's
     * loading state can promise. A page that leads with something else says so by tagging a different key.
     */
    object Detail {
        /** The immersive header — compact hero, or the cinematic backdrop at an expanded width. */
        const val HERO: String = "detail-hero"

        /** The runtime/rating/status row directly below the hero. */
        const val STATS: String = "detail-stats"

        /** The primary action row — play/request plus the icon buttons. */
        const val ACTIONS: String = "detail-actions"

        /** The overview/biography prose block. */
        const val OVERVIEW: String = "detail-overview"

        /**
         * The cast rail.
         *
         * Tagged by the detail skeleton but by no resolved page, so no geometry test names it. The media
         * pages put a data-dependent details block (director, studios, collection) between the overview
         * and the rail, and the details-section band now reserves an average three rows for it rather
         * than the nothing that left the rail 124dp adrift.
         *
         * An average is the most a fixed band can do — the real list runs from two rows to six — so the
         * residual is smaller but not zero, and this anchor stays unwired rather than asserting a number
         * that only holds for one shape of data. Wiring it would mean giving the details block its own
         * anchor and letting the rail float below it.
         */
        const val CAST: String = "detail-cast"

        /** A rating card standing where another page puts its action row (episode detail). */
        const val RATING: String = "detail-rating"

        /** A person page's profile card, which stands where a media page puts its hero. */
        const val PROFILE: String = "detail-profile"
    }

    /** The hub screens' shared section keys — the home tabs' hero-plus-carousels body. */
    object Hub {
        /** The hero carousel at the top of a hub. */
        const val HERO: String = "hub-hero"

        /** The Discover entry row between the hero and the carousels — shown wherever Discover has no tab. */
        const val DISCOVER_ENTRY: String = "hub-discover-entry"

        /**
         * The first content carousel below the entry row.
         *
         * Which carousel that is depends on the data — a watchlist row when the user has one, else the
         * on-your-services row, else the category row, else the leading genre row — so this anchor rides
         * whichever actually leads. That is the right reading: the skeleton promises *a* carousel there, and
         * what has to hold is that the first one lands where the first plate did.
         *
         * All four cases are named deliberately: an earlier version stopped at the category row, leaving a
         * genre-only hub with no anchored carousel while this KDoc still claimed one.
         */
        const val FIRST_CAROUSEL: String = "hub-first-carousel"
    }

    /** A grid or list body that fills a whole screen — Discover results, the gallery, the library, lists. */
    object Collection {
        /** The first cell of the grid, or the first row of the list. */
        const val FIRST_ITEM: String = "collection-first-item"
    }

    private const val PREFIX = "anchor:"
}

/**
 * Marks this node as a layout anchor — a point a skeleton and its resolved content must agree on.
 *
 * A plain `testTag` today. It is a named modifier rather than a bare `testTag` call so the anchors are one
 * greppable set with one rationale ([LayoutAnchors]), and so that swapping the mechanism later (a semantics
 * key, a layout id) is one edit rather than a sweep. The television's `tvLayoutAnchor` is the same modifier
 * for the same reason.
 */
fun Modifier.layoutAnchor(tag: String): Modifier = testTag(tag)

/**
 * Marks this node as a layout anchor only when [applies].
 *
 * Grids and lists anchor their **leading** cell rather than their container — the container fills the
 * screen, so its top never moves and anchoring it would pass whatever the cells did. That makes
 * "tag this one, not the others" the commonest shape at a call site, and a named form of it keeps the
 * condition beside the tag instead of wrapped around the whole modifier chain.
 */
fun Modifier.layoutAnchorIf(applies: Boolean, tag: String): Modifier = if (applies) layoutAnchor(tag) else this

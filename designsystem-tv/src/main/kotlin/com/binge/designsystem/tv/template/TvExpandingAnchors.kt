package com.binge.designsystem.tv.template

import com.binge.designsystem.tv.layout.TvLayoutAnchors

/**
 * The layout anchors on the expanding hub's open row and row headings, which [TvExpandingHubLoadingSkeleton] and the hub's loading rows reserve.
 * Both the real row and its skeleton tag the same point, so a test measures that resolving moves nothing.
 */
object TvExpandingAnchors {
    /** The open row's backdrop-sized slot. */
    val OPEN_SLOT: String = TvLayoutAnchors.entry("expanding-open-slot")

    /** The poster beside the open row's slot: the first the strip shows. */
    val OPEN_FIRST_POSTER: String = TvLayoutAnchors.entry("expanding-open-first-poster")

    /** The heading of the hub's row at [rowIndex], among the rows it lays out. */
    fun heading(rowIndex: Int): String = TvLayoutAnchors.entry("expanding-heading-$rowIndex")
}

/** The hub list's key for its hero, which a saved focus position names. An app's own row keys are text too, so it carries the reserved `binge:` prefix and cannot collide with them. */
internal const val TV_EXPANDING_HERO_KEY = "binge:tv-expanding-hero"

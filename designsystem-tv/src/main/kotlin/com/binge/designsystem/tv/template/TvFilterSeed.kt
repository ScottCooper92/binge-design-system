package com.binge.designsystem.tv.template

/**
 * Which control a frame draws focused: the header (or the view's pinned count row, or its switch), or the row keyed [key].
 * Production leaves it empty, as focus is a parameter only so a baseline can show it.
 */
data class TvFilterSeed(
    val header: Boolean = false,
    val pinned: Boolean = false,
    val switch: Boolean = false,
    val key: String? = null,
)

/** The category and value focus was last on in the pane, which Back and re-entry return to. */
data class TvFilterPosition(
    val category: String? = null,
    val value: String? = null,
)

/** Which control a frame draws focused in a range view: the header, a bound's tab, or the scale row keyed [key]. */
data class TvFilterRangeSeed(
    val header: Boolean = false,
    val minTab: Boolean = false,
    val maxTab: Boolean = false,
    val key: String? = null,
)

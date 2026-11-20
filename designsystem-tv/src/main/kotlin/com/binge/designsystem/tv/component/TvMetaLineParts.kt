package com.binge.designsystem.tv.component

import com.binge.designsystem.formatRating

/** Genres beyond this are dropped: a meta line that wraps stops being a glanceable line. */
internal const val MAX_META_GENRES = 2

/** Public so a screen's own meta lines (a person's stat line) share the separator and cannot drift a space apart. */
const val META_SEPARATOR = "  ·  "

/**
 * The score as every 10-foot surface writes it, ready for [TvRatingBadge]. It is [formatRating], the one rendering phone and TV
 * share, so the decimal separator follows the locale (`7,9` in Spanish). Null for an unrated zero ("nobody voted", not "rated
 * zero"), so the badge is absent, not `0.0`.
 */
internal fun tvRatingValue(rating: Float?): String? = rating?.takeIf { it > 0f }?.formatRating()

package com.binge.designsystem.tv.template

import com.binge.designsystem.tv.template.TvHubRow
import com.binge.designsystem.tv.template.TvVisitDepth

/**
 * Focus arrived on the row [key] of [rows], below the hero when [hasHero]: the place the legacy hub reports, so both hub
 * layouts feed one metric — the hero as 0 and each row as its position in the hub's full row list after it.
 */
internal fun TvVisitDepth.reachRow(
    rows: List<TvHubRow<TvHubItem>>,
    key: String,
    hasHero: Boolean,
) = reach(
    rows.indexOfFirst { it.key == key } + hasHero.compareTo(false),
)

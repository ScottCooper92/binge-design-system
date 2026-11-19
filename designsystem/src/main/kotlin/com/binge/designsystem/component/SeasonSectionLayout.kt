package com.binge.designsystem.component

/**
 * The rows a season takes below its header: a loaded one its real rows; a loading one as many skeleton rows
 * as it declares (capped), so header positions are known before any episode is; a failed one, a single row.
 */
internal fun SeasonSectionUi.rowCount(): Int =
    when (val s = state) {
        is SeasonSectionState.Loaded -> s.episodes.size
        is SeasonSectionState.Loading -> s.rowCount.coerceIn(0, MAX_SEASON_SKELETON_ROWS)
        is SeasonSectionState.Failed -> 1
    }

/** Each section is one header item plus its rows, so a section's header sits at the running total before it. */
internal fun List<SeasonSectionUi>.headerIndices(): List<Int> {
    val indices = mutableListOf<Int>()
    var running = 0
    forEach { section ->
        indices += running
        running += 1 + section.rowCount()
    }
    return indices
}

/**
 * The keys of the sections to load for the list items at [visibleIndices]: every section with its header or a
 * row on screen, plus the next one below, so scrolling down never reaches an empty section.
 */
internal fun List<SeasonSectionUi>.sectionsNeeded(headerIndices: List<Int>, visibleIndices: List<Int>): List<String> {
    val visible = visibleIndices
        .map { index -> headerIndices.indexOfLast { it <= index } }
        .filter { it >= 0 }
        .toSortedSet()
    val withNext = visible.lastOrNull()?.let { visible + (it + 1) } ?: visible
    return withNext.mapNotNull { getOrNull(it)?.key }
}

/**
 * The key of the row at [index] in a season's [this] list: its section and episode number, which is unique within a season. A data
 * source that repeats a number would make a lazy list throw on the duplicate key, so a repeated number takes its index too.
 */
internal fun List<EpisodeBrowseUi>.rowKey(
    sectionKey: String,
    index: Int,
    episode: EpisodeBrowseUi,
): String =
    if (count { it.number == episode.number } > 1) "ep_${sectionKey}_${episode.number}_$index" else "ep_${sectionKey}_${episode.number}"

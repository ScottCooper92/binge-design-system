package com.binge.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test

class SeasonSectionLayoutTest {
    private val ep = EpisodeBrowseUi(number = 1, name = "Pilot")

    private fun section(key: String, state: SeasonSectionState) = SeasonSectionUi(key, "S$key", "", state)

    private val sections = listOf(
        section("1", SeasonSectionState.Loaded(listOf(ep, ep.copy(number = 2)))),
        section("2", SeasonSectionState.Loading(rowCount = 100)),
        section("3", SeasonSectionState.Failed("m", "r")),
        section("4", SeasonSectionState.Loading(rowCount = 3)),
    )

    @Test
    fun rowCountsFollowStateAndCapSkeletons() {
        assertEquals(listOf(2, MAX_SEASON_SKELETON_ROWS, 1, 3), sections.map { it.rowCount() })
    }

    @Test
    fun headerIndicesAccumulateHeaderPlusRows() {
        assertEquals(listOf(0, 3, 34, 36), sections.headerIndices())
    }

    @Test
    fun visibleSectionsPlusTheNextAreNeeded() {
        val headers = sections.headerIndices()
        assertEquals(listOf("1", "2"), sections.sectionsNeeded(headers, listOf(0, 1, 2)))
        assertEquals(listOf("2", "3", "4"), sections.sectionsNeeded(headers, listOf(10, 34)))
        assertEquals(listOf("4"), sections.sectionsNeeded(headers, listOf(37)))
        assertEquals(emptyList<String>(), sections.sectionsNeeded(headers, emptyList()))
    }
}

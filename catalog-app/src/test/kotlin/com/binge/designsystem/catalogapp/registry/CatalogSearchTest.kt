package com.binge.designsystem.catalogapp.registry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CatalogSearchTest {
    private fun entry(
        id: String,
        group: String,
        name: String,
        description: String = "",
    ) = CatalogEntry(id, group, name, description) {}

    private val entries =
        listOf(
            entry("A", "Button", "Button family", "Filled and outlined"),
            entry("B", "Button", "Filled button loading", "A spinner replaces the label"),
            entry("C", "MediaCard", "Media card rated", "Poster with a rating chip"),
        )

    @Test
    fun `a blank query keeps everything in order`() {
        assertEquals(listOf("A", "B", "C"), entries.matching("   ").map { it.id })
    }

    @Test
    fun `matches on name, group and description without regard to case`() {
        assertEquals(listOf("B"), entries.matching("SPINNER").map { it.id })
        assertEquals(listOf("C"), entries.matching("mediacard").map { it.id })
        assertEquals(listOf("A", "B"), entries.matching("filled").map { it.id })
    }

    @Test
    fun `every word must match, in any order`() {
        assertEquals(listOf("B"), entries.matching("loading filled").map { it.id })
        assertEquals(emptyList<String>(), entries.matching("filled poster").map { it.id })
    }

    @Test
    fun `groups keep registry order and their members`() {
        val grouped = entries.byGroup()

        assertEquals(listOf("Button", "MediaCard"), grouped.map { it.first })
        assertEquals(listOf("A", "B"), grouped.first().second.map { it.id })
    }
}

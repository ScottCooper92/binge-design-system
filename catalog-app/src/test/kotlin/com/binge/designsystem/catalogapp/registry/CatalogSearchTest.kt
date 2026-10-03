package com.binge.designsystem.catalogapp.registry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CatalogSearchTest {
    private fun entry(
        id: String,
        group: String,
        name: String,
        description: String = "",
        kind: CatalogKind = CatalogKind.Sample,
    ) = CatalogEntry(id = id, group = group, name = name, description = description, kind = kind) {}

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
    fun `matches on the function name, so a component is found by its code name`() {
        val named = listOf(entry("ButtonFamilySample", "Button", "Family"), entry("ButtonIconSample", "Button", "Icon"))

        assertEquals(listOf("ButtonFamilySample"), named.matching("buttonfamilysample").map { it.id })
    }

    @Test
    fun `components keep registry order and their variants`() {
        val components = entries.components()

        assertEquals(listOf("Button", "MediaCard"), components.map { it.group })
        assertEquals(listOf("A", "B"), components.first().variants.map { it.id })
    }

    @Test
    fun `a component previews its first sample, and a demo only when it has no sample`() {
        val withDemoFirst = listOf(entry("D", "TopBar", "Enter always", kind = CatalogKind.Demo), entry("S", "TopBar", "Default"))
        val demoOnly = listOf(entry("L", "Locale", "Localised strings", kind = CatalogKind.Demo))

        val topBar = withDemoFirst.components().single()

        assertEquals("S", topBar.preview.id)
        assertTrue(topBar.hasDemo)
        assertEquals(
            "L",
            demoOnly
                .components()
                .single()
                .preview.id,
        )
        assertFalse(entries.components().first().hasDemo)
    }

    @Test
    fun `a component is one per screen only when a variant's file says so`() {
        val marked = CatalogEntry("T", "TopBar", name = "Default", description = "", onePerScreen = true) {}
        val demo = entry("D", "Sheet", "Open", kind = CatalogKind.Demo)

        assertTrue(listOf(marked, entry("U", "TopBar", "Transparent")).components().single().onePerScreen)
        assertFalse(listOf(demo).components().single().onePerScreen)
        assertFalse(entries.components().first().onePerScreen)
    }
}

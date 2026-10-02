package com.binge.designsystem.catalogapp.registry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Checks the generated registry the app is built with, not the generator (that has its own tests). */
class CatalogRegistryTest {
    @Test
    fun `the generated registry lists the catalog`() {
        assertTrue(CatalogRegistry.size > 100, "expected the design system's samples, got ${CatalogRegistry.size}")
        assertTrue(CatalogRegistry.any { it.id == "ButtonFamilySample" })
    }

    @Test
    fun `ids are unique`() {
        assertEquals(CatalogRegistry.size, CatalogRegistry.map { it.id }.toSet().size)
    }
}

package com.binge.designsystem.tv.template

import org.junit.Assert.assertEquals
import org.junit.Test

class TvDetailGalleryMixTest {
    private fun landscape(vararg index: Int) = index.map { TvDetailGalleryImage("l$it", portrait = false, index = it) }

    private fun portrait(vararg index: Int) = index.map { TvDetailGalleryImage("p$it", portrait = true, index = it) }

    private fun List<TvDetailGalleryImage>.indices() = map { it.index }

    @Test
    fun `four landscape then one portrait, repeating`() {
        val mixed = (landscape(0, 1, 2, 3, 4, 5, 6, 7) + portrait(10, 11)).mixedForGrid()

        assertEquals(listOf(0, 1, 2, 3, 10, 4, 5, 6, 7, 11), mixed.indices())
    }

    @Test
    fun `an odd leftover landscape image goes after the remaining portraits so none sits half a column out`() {
        val mixed = (landscape(0, 1, 2, 3, 4) + portrait(10, 11)).mixedForGrid()

        assertEquals(listOf(0, 1, 2, 3, 10, 11, 4), mixed.indices())
    }

    @Test
    fun `fewer than a column's worth of landscape images leaves the order portraits first`() {
        val mixed = (landscape(0, 1) + portrait(10)).mixedForGrid()

        assertEquals(listOf(10, 0, 1), mixed.indices())
    }

    @Test
    fun `only one orientation keeps its order`() {
        assertEquals(listOf(0, 1, 2), landscape(0, 1, 2).mixedForGrid().indices())
        assertEquals(listOf(5, 6), portrait(5, 6).mixedForGrid().indices())
        assertEquals(emptyList<Int>(), emptyList<TvDetailGalleryImage>().mixedForGrid().indices())
    }

    @Test
    fun `every image keeps its index in the whole gallery`() {
        val images = landscape(0, 1, 2, 3) + portrait(4)

        assertEquals(images.indices().sorted(), images.mixedForGrid().indices().sorted())
    }
}

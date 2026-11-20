package com.binge.designsystem.tv.template

import com.binge.designsystem.tv.component.META_SEPARATOR
import com.binge.designsystem.tv.component.tvExpandingFacts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvExpandingTitlesTest {
    private fun row(count: Int) =
        TvHubRow(
            key = "row",
            title = "Row",
            items = List(count) { TvHubItem(key = "movie_$it", posterUrl = null, title = "T$it", rating = null) },
        )

    private fun pages(count: Int, ended: Boolean) = TvRowPages<TvHubItem>(count = count, endReached = ended, at = { null })

    @Test
    fun `a long row with no pages loops, a short one does not`() {
        assertTrue(tvExpandingTitles(row(8), pages = null).loops)
        assertFalse(tvExpandingTitles(row(7), pages = null).loops)
    }

    @Test
    fun `a paged row loops only once its source has ended`() {
        assertFalse(tvExpandingTitles(row(3), pages(count = 20, ended = false)).loops)
        assertTrue(tvExpandingTitles(row(3), pages(count = 20, ended = true)).loops)
    }

    @Test
    fun `a paged row reads its own titles once its first page is in, and the hub's until then`() {
        assertEquals(20, tvExpandingTitles(row(3), pages(count = 20, ended = false)).count)
        assertEquals(3, tvExpandingTitles(row(3), pages(count = 0, ended = false)).count)
    }

    @Test
    fun `a position wraps in a looping row and is held within a short one`() {
        val looping = tvExpandingTitles(row(8), pages = null)
        val short = tvExpandingTitles(row(3), pages = null)

        assertEquals(1, looping.index(9))
        assertEquals(2, short.index(9))
        assertTrue(looping.canStepRight(100))
        assertFalse(short.canStepRight(2))
    }

    private fun titles(vararg items: TvHubItem) = TvExpandingTitles(count = items.size, at = { items.getOrNull(it) }, loops = false)

    private fun item(id: Int, kind: String) = TvHubItem(key = "${kind}_$id", posterUrl = null, title = "$kind $id", rating = null)

    /** TMDB movie and TV ids are separate number spaces: a mixed row can hold a movie and a show with the same id (#4548). */
    @Test
    fun `following a movie does not jump to a show that shares its id`() {
        val movie = item(7, "Movie")
        val show = item(7, "Tv")
        val held = movie.key

        assertEquals(1, titles(show, movie).follow(position = 0, key = held))
    }

    @Test
    fun `following a title finds it where it moved, or takes the place it left`() {
        val a = item(1, "Movie")
        val b = item(2, "Movie")
        val c = item(3, "Movie")

        assertEquals(2, titles(c, a, b).follow(position = 1, key = b.key))
        assertEquals(1, titles(a, c).follow(position = 1, key = b.key))
        assertEquals(1, titles(a, b).follow(position = 1, key = b.key))
    }

    @Test
    fun `facts keep the genres that fit, then the trailing facts, skipping blanks`() {
        val genres = listOf("Drama", "Crime")

        assertEquals("Drama${META_SEPARATOR}Crime${META_SEPARATOR}2024", tvExpandingFacts(genres, listOf("2024"), genreCount = 2))
        assertEquals("Drama${META_SEPARATOR}2024", tvExpandingFacts(genres, listOf("2024"), genreCount = 1))
        assertEquals("2024${META_SEPARATOR}2h 1m", tvExpandingFacts(genres, listOf("2024", " ", "2h 1m"), genreCount = 0))
    }
}

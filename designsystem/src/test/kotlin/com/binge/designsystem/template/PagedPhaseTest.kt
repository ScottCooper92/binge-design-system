package com.binge.designsystem.template

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException

private val loading = LoadState.Loading
private val idle = LoadState.NotLoading(endOfPaginationReached = false)
private val complete = LoadState.NotLoading(endOfPaginationReached = true)
private val failure = IOException("unreachable")

private val rows = PagedPhase.Rows(refreshing = false, refreshError = null)
private val refreshingRows = PagedPhase.Rows(refreshing = true, refreshError = null)

private data class Frame(
    val source: LoadState,
    val mediator: LoadState?,
    val count: Int = 0,
    val refresh: PagedRefresh? = null,
)

private fun Frame.states(): CombinedLoadStates =
    CombinedLoadStates(
        refresh = mediator ?: source,
        prepend = idle,
        append = idle,
        source = LoadStates(refresh = source, prepend = idle, append = idle),
        mediator = mediator?.let { LoadStates(refresh = it, prepend = idle, append = idle) },
    )

private fun phases(vararg frames: Frame): List<PagedPhase> {
    val tracker = PagedPhaseTracker()
    return frames.map { tracker.phase(it.states(), it.count, it.refresh) }
}

private fun wrote(rows: Int) = PagedRefresh(rowsWritten = rows, sequence = rows.toLong() + 1)

/** What each sequence a paged list goes through reads as on screen, with and without a mediator. */
class PagedPhaseTest {
    @Test
    fun `a list with no mediator is the skeleton while it loads, then its rows`() {
        assertEquals(
            listOf(PagedPhase.Skeleton, rows),
            phases(Frame(source = loading, mediator = null), Frame(source = complete, mediator = null, count = 3)),
        )
    }

    @Test
    fun `a list with no mediator and no rows is empty once it has loaded`() {
        assertEquals(listOf(PagedPhase.Empty), phases(Frame(source = complete, mediator = null)))
    }

    @Test
    fun `a first load that fails with nothing to show is failed`() {
        assertEquals(listOf(PagedPhase.Failed(failure)), phases(Frame(source = LoadState.Error(failure), mediator = null)))
    }

    @Test
    fun `a refresh behind rows on screen keeps the rows and says it is refreshing`() {
        assertEquals(listOf(refreshingRows), phases(Frame(source = idle, mediator = loading, count = 3)))
    }

    @Test
    fun `a refresh failing behind rows on screen keeps the rows and carries the error`() {
        assertEquals(
            listOf(PagedPhase.Rows(refreshing = false, refreshError = failure)),
            phases(Frame(source = idle, mediator = LoadState.Error(failure), count = 3)),
        )
    }

    @Test
    fun `a mediator list that never records a refresh stays on the skeleton with an idle, empty cache`() {
        assertEquals(
            listOf(PagedPhase.Skeleton, PagedPhase.Skeleton),
            phases(Frame(source = idle, mediator = complete), Frame(source = complete, mediator = complete)),
        )
    }

    @Test
    fun `a mediator list that records its skipped refresh as writing nothing reads empty`() {
        assertEquals(
            listOf(PagedPhase.Empty),
            phases(Frame(source = complete, mediator = complete, refresh = wrote(0))),
        )
    }

    @Test
    fun `a mediator list stays on the skeleton until its refresh is known, then reads empty`() {
        assertEquals(
            listOf(PagedPhase.Skeleton, PagedPhase.Skeleton, PagedPhase.Empty),
            phases(
                Frame(source = loading, mediator = loading),
                Frame(source = idle, mediator = complete),
                Frame(source = idle, mediator = complete, refresh = wrote(0)),
            ),
        )
    }

    @Test
    fun `a refresh that wrote rows stays on the skeleton until the cache shows them`() {
        assertEquals(
            listOf(PagedPhase.Skeleton, rows),
            phases(
                Frame(source = idle, mediator = complete, refresh = wrote(3)),
                Frame(source = idle, mediator = complete, count = 3, refresh = wrote(3)),
            ),
        )
    }

    @Test
    fun `once its rows have shown, a list emptied by the user reads empty rather than loading`() {
        assertEquals(
            listOf(rows, PagedPhase.Empty),
            phases(
                Frame(source = idle, mediator = complete, count = 1, refresh = wrote(1)),
                Frame(source = idle, mediator = complete, count = 0, refresh = wrote(1)),
            ),
        )
    }
}

package com.binge.designsystem.tv.template

import com.binge.designsystem.ErrorKind

/**
 * What a [TvGridPage] should show. **Deliberately Paging-free**, so all four states are screenshot-renderable: a
 * `LazyPagingItems` collects in a `LaunchedEffect` and a baseline runs no coroutines, so it would only ever
 * capture the loading state. Callers translate their source into one of these at the screen's edge.
 */
sealed interface TvGridResults<out T : Any> {
    data object Loading : TvGridResults<Nothing>

    /**
     * The load failed, carrying **why**: the [kind] picks the shared glyph and copy, and a [title] or [message] overrides
     * that copy for a failure only the caller can name (a rejected key is not "Not allowed"). A payload rather than a bare
     * object, so every grid renders the wording for its failure and not the generic one.
     */
    data class Error(
        val kind: ErrorKind,
        val title: String? = null,
        val message: String? = null,
    ) : TvGridResults<Nothing>

    data object Empty : TvGridResults<Nothing>

    class Items<out T : Any>(
        val count: Int,
        val key: (Int) -> Any,
        val at: (Int) -> T?,
    ) : TvGridResults<T>
}

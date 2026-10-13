package com.binge.designsystem.tv.template

/**
 * What a [TvImmersiveGrid] shows. Paging-free, so every state renders in a frame: a caller translates its source into
 * one of these at the screen's edge.
 */
sealed interface TvImmersiveGridContent<out T : Any> {
    /** The first page has not arrived. The grid draws its `loading` slot where the cells will be. */
    data object Loading : TvImmersiveGridContent<Nothing>

    /**
     * Nothing to show as cells: an empty result or a failure. The grid centres its `message` slot in the pane, since a
     * message is not a grid. [key] tells one message from the next, so focus held in one is kept when it is swapped.
     */
    data class Message(
        val key: Any = Unit,
    ) : TvImmersiveGridContent<Nothing>

    /**
     * [count] cells. [key] names an index stably without loading it; [at] reads the item, which is what asks a paging
     * source for the next page, and returns null for a cell not loaded yet.
     */
    class Items<out T : Any>(
        val count: Int,
        val key: (Int) -> Any,
        val at: (Int) -> T?,
    ) : TvImmersiveGridContent<T>
}

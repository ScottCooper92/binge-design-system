package com.binge.designsystem.tv.template

/**
 * A paged hub row as the TV row reads it: how many titles have loaded, each by position, and whether the list has ended. Plain
 * values rather than paging-compose's types, which stay with the feature screens; [at] may load the next page as it reads.
 */
class TvRowPages<T : Any>(
    val count: Int,
    val endReached: Boolean,
    val at: (Int) -> T?,
)

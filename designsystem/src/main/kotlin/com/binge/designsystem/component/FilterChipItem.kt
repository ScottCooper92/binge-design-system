package com.binge.designsystem.component

import androidx.compose.runtime.Immutable

/** One filter chip: a [label] and an optional [count] rendered as a sub-pill. */
@Immutable
data class FilterChipItem(
    val label: String,
    val count: Int? = null,
)

package com.binge.designsystem.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One filter chip: a [label], an optional [count] rendered as a sub-pill, and an optional [icon] drawn
 * before the label.
 */
@Immutable
data class FilterChipItem(
    val label: String,
    val count: Int? = null,
    val icon: ImageVector? = null,
)

package com.binge.designsystem.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One filter chip: a [label], an optional [count] rendered as a sub-pill, and an optional [icon] drawn
 * before the label. A [contentDescription], when set, is what a screen reader announces for the chip in place of the
 * visible [label], for a label that is short on screen and needs more to be understood by ear: a "Warnings" level chip
 * that also shows errors reads "Warnings and errors". Null leaves the label as the announcement.
 */
@Immutable
data class FilterChipItem(
    val label: String,
    val count: Int? = null,
    val icon: ImageVector? = null,
    val contentDescription: String? = null,
)

package com.binge.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/** One thing a decision screen says about its choice: an [icon], a short [title] and the [detail] under it. */
@Immutable
data class DecisionPoint(
    val icon: ImageVector,
    val title: String,
    val detail: String,
)

/** What a decision screen asks: an optional [kicker] over the [title], the [subtitle] under it, and a [note]. */
@Immutable
data class DecisionCopy(
    val title: String,
    val subtitle: String,
    val kicker: String? = null,
    val note: String? = null,
)

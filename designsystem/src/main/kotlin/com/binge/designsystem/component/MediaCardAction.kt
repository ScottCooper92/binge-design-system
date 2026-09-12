package com.binge.designsystem.component

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A contextual corner action drawn on top of a [MediaCard] poster: a circular scrim-backed button
 * whose 40dp box is its own tap target, separate from the card body (which opens detail). Wired by
 * the Library grid — a tick to mark watched, an X to remove from the current collection.
 */
data class MediaCardAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
)

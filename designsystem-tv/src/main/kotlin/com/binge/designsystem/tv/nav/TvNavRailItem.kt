package com.binge.designsystem.tv.nav

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One destination on the TV navigation rail. A non-null [displayName] renders a circular avatar in place
 * of [icon] — the account item at the top of the rail: the Gravatar image when [avatarUrl] resolves, the
 * name's initials while it loads, on failure, or when there is no image. Every other item shows [icon].
 */
data class TvNavRailItem(
    val key: Any,
    val label: String,
    val icon: ImageVector,
    val displayName: String? = null,
    val avatarUrl: String? = null,
)

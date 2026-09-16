package com.binge.designsystem.tv.nav

import androidx.compose.ui.graphics.vector.ImageVector
import com.binge.designsystem.component.NavSuiteBadge

/**
 * One destination on the TV navigation rail. A non-null [displayName] renders a circular avatar in place
 * of [icon] — the account item at the top of the rail: the Gravatar image when [avatarUrl] resolves, the
 * name's initials while it loads, on failure, or when there is no image. Every other item shows [icon].
 *
 * [badge] is the same [NavSuiteBadge] the phone's nav bar renders, so a caller with a count or an alert
 * to show formats it once and hands the identical value to both surfaces.
 */
data class TvNavRailItem(
    val key: Any,
    val label: String,
    val icon: ImageVector,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val badge: NavSuiteBadge = NavSuiteBadge.None,
)

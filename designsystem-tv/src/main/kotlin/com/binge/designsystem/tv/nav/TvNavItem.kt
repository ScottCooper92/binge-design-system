package com.binge.designsystem.tv.nav

import androidx.compose.ui.graphics.vector.ImageVector
import com.binge.designsystem.component.NavSuiteBadge

/**
 * One destination in a TV shell's navigation. A non-null [displayName] renders a circular avatar in place
 * of [icon] — the account item at the start of the top bar: the image at [avatarUrl] when it resolves, the
 * name's initials while it loads, on failure, or when there is no image. Every other item shows [icon].
 *
 * [badge] is the same [NavSuiteBadge] the phone's nav bar renders, so a caller with a count or an alert
 * to show formats it once and hands the identical value to both surfaces.
 */
data class TvNavItem(
    val key: Any,
    val label: String,
    val icon: ImageVector,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val badge: NavSuiteBadge = NavSuiteBadge.None,
)

/** The name [TvNavItem] had while a navigation rail drew it. */
@Deprecated("Renamed: the item is the top bar's, not a rail's.", ReplaceWith("TvNavItem", "com.binge.designsystem.tv.nav.TvNavItem"))
typealias TvNavRailItem = TvNavItem

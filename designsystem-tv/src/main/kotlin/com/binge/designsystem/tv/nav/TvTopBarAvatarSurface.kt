package com.binge.designsystem.tv.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.BingeTvInitialsAvatar
import com.binge.designsystem.tv.R as TvR

private const val CURRENT_ALPHA = 0.2f

/**
 * The top bar's account avatar: the profile's initials or image signed in, the person glyph signed out (there is no profile,
 * and initials made from the "Account" label would mislead). [selected] wears the same faint pill the current tab does, as a
 * ring and a fill; [isFocused] swaps the ring for a solid one. The caller supplies the click, focus and semantics in [modifier].
 *
 * @need draw a tv top bar's account avatar, signed in or out
 * @group tv
 * @sample TvTopBarAvatarSample
 */
@Composable
fun TvTopBarAvatarSurface(
    entry: TvNavItem,
    selected: Boolean,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val current = if (selected) scheme.onSurface.copy(alpha = CURRENT_ALPHA) else Color.Transparent
    val diameter = dimensionResource(TvR.dimen.tv_top_bar_avatar_size)
    val ring = dimensionResource(TvR.dimen.tv_top_bar_avatar_ring)
    Box(
        modifier = modifier
            .size(diameter)
            .clip(BingeShapes.Pill)
            .background(current)
            .border(ring, if (isFocused) scheme.onSurface else current, BingeShapes.Pill)
            .padding(ring),
        contentAlignment = Alignment.Center,
    ) {
        val name = entry.displayName
        if (name == null) {
            Icon(
                imageVector = entry.icon,
                contentDescription = null,
                tint = scheme.onSurface,
                modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)),
            )
        } else {
            BingeTvInitialsAvatar(name = name, size = diameter - ring * 2, avatarUrl = entry.avatarUrl)
        }
    }
}

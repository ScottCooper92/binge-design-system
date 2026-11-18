package com.binge.designsystem.tv.nav

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.BingeTvInitialsAvatar
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.R as TvR

/**
 * The account avatar as a control: the one drawing the top bar, the account menu's anchor and the catalog sample share, so the
 * three cannot drift apart. The picture, or the initials of [name], sits in a ring of [ringColor] (transparent at rest), and the
 * avatar is smaller than a button but in a button-sized slot, so the bar's height and the places of the tabs and of the open
 * panel's avatar (which is drawn over this slot) stay where they were.
 *
 * What differs per use is passed in: the ring's colour for its focus and selection, and [semantics], which names the control
 * (and, for the menu, says it is selected). The modifier order is the layout: size, clip, border, click, semantics, then padding.
 */
@Composable
internal fun TvAvatarButton(
    name: String,
    avatarUrl: String?,
    ringColor: Color,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    semantics: SemanticsPropertyReceiver.() -> Unit,
    modifier: Modifier = Modifier,
) {
    val slot = dimensionResource(TvR.dimen.tv_icon_button_diameter)
    val diameter = dimensionResource(TvR.dimen.tv_top_bar_avatar_size)
    val ring = dimensionResource(TvR.dimen.tv_top_bar_avatar_ring)
    Box(modifier = modifier.size(slot), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(diameter)
                .clip(BingeShapes.Pill)
                .border(ring, ringColor, BingeShapes.Pill)
                .tvClickable(role = Role.Button, onFocusChanged = onFocusChanged, onClick = onClick)
                .clearAndSetSemantics(semantics)
                .padding(ring),
            contentAlignment = Alignment.Center,
        ) {
            BingeTvInitialsAvatar(name = name, size = diameter - ring * 2, avatarUrl = avatarUrl)
        }
    }
}

package com.binge.designsystem.tv.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import com.binge.designsystem.theme.BingeShapes

/**
 * The TV corner scale: the compact, squarer look of the v2 pages, one radius for the surfaces a person reads and presses.
 * It is separate from [BingeShapes] on purpose. The phone components and Binge's phone surfaces share those tokens, and a
 * squarer TV must not move them. (`BingeTvShapes` is tv-material's own `Shapes` container for the theme, a different thing.)
 *
 * What stays round is the control that is round by design: pill buttons, switch tracks, chips and badges, and an avatar.
 */
object BingeTvCorners {
    /** Dialogs, cards, rows, tabs and QR plates: the same 8dp as [BingeShapes.TvListItem], so a row and the card holding it agree. */
    val Small: RoundedCornerShape = BingeShapes.TvListItem

    /**
     * A poster or media card, and the placeholder that stands in for one: the same 12dp as [BingeShapes.MoreCard], so the
     * focus ring drawn around it matches.
     */
    val Poster: RoundedCornerShape = BingeShapes.MoreCard
}

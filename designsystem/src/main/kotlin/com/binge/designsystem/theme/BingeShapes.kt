package com.binge.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object BingeShapes {
    val MediaCard = RoundedCornerShape(20.dp)

    /** Role-neutral radii: reach for these on any rounded surface/container, not just buttons/cards. */
    val Medium = RoundedCornerShape(16.dp)
    val Large = RoundedCornerShape(18.dp)

    val MoreCard = RoundedCornerShape(12.dp)
    val ElementSmall = RoundedCornerShape(8.dp)
    val ElementExtraSmall = RoundedCornerShape(4.dp)

    val Pill = RoundedCornerShape(50)
    val Chip = RoundedCornerShape(10.dp)

    /** Compact category/type/role tag ([com.binge.designsystem.component.BingeTag]) — tighter than [Chip]. */
    val Tag = RoundedCornerShape(6.dp)
    val AccountCard = RoundedCornerShape(26.dp)

    /** A [com.binge.designsystem.component.ListRow] poster: tighter than [Medium], a thumbnail beside text rather than a card. */
    val ListRowPoster = RoundedCornerShape(14.dp)

    /** The profile poster heading a person's detail page, and its skeleton: rounder than [MediaCard], the header's one surface. */
    val ProfilePoster = RoundedCornerShape(24.dp)

    /** A full-bleed hero surface — a signed-out prompt, an onboarding hero — rounder than any card so it reads as a stage. */
    val Hero = RoundedCornerShape(28.dp)

    /** A card that stacks rows (an onboarding points list): between [MediaCard] and [AccountCard], so it sits with both. */
    val ListCard = RoundedCornerShape(22.dp)

    /** A bar in a small chart (a rating distribution): the [Tag] radius, kept separate so a chart never moves with the tags. */
    val ChartBar = RoundedCornerShape(6.dp)

    /**
     * tv-material's list-row corner, spelled explicitly because `ListItemDefaults.shape()` returns a
     * `ListItemShape` bundle, not a `Shape` — so a non-`ListItem` (the nav rail) can't reference it,
     * and this keeps the rail and list rows aligned. A deliberate *mirror* of tv-material 1.1.0's
     * `RoundedCornerShape(8.dp)`, not an alias of [ElementSmall]: the number is owned by the library,
     * so it stays checkable against it rather than moving with Binge's own element scale.
     */
    val TvListItem = RoundedCornerShape(8.dp)

    /**
     * The M3 [Shapes] scale, populated from the tokens above so `MaterialTheme.shapes.*` and any
     * M3 component that reads it (Card, Chip, AlertDialog, …) carry Binge's expressive radii rather
     * than the framework defaults. `small` matches the M3 default (8dp); `medium`/`large`/`extraLarge`
     * round up to the card scale. `BingeShapes` is the single source of truth — see CLAUDE.md.
     */
    val Material3 = Shapes(
        extraSmall = ElementExtraSmall,
        small = ElementSmall,
        // M3's medium step is 18dp (our Large token); M3's large is 20dp (MediaCard).
        medium = Large,
        large = MediaCard,
        extraLarge = AccountCard,
    )
}

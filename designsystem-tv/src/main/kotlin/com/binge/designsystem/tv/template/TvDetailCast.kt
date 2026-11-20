package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.binge.designsystem.tv.component.TvCastCardAvatar
import com.binge.designsystem.tv.component.TvStripSeeAllCard
import com.binge.designsystem.tv.component.readAs
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val PORTRAIT_ASPECT = 2f / 3f

/** One person in the Cast tab: what the card draws, the [key] that keeps it stable across a reorder, and their photo if there is one. */
class TvCastCell(
    val key: String,
    val name: String,
    val role: String?,
    val profileUrl: String?,
)

private const val SEE_ALL_PEEK = 3

/**
 * The Cast tab as a [TvDetailSlotCarousel] of portraits. OK opens the person, reported by position rather than by any id.
 * With [onSeeAll], a See-all card after the last person opens the full cast and crew list. When there is no portrait to show (the
 * only credit is a director with no photo) the card stands alone, so the full list stays reachable from the tab.
 *
 * @need show a title's cast as portraits in a detail carousel with an optional see-all card
 * @group tv
 * @replaces a hand-built cast row
 */
@Composable
fun TvDetailCast(
    people: List<TvCastCell>,
    onCastClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
    initialPosition: Int = 0,
    initialFocused: Boolean = false,
) {
    val seeAllLabel = stringResource(DesR.string.gallery_see_all)
    if (people.isEmpty()) {
        onSeeAll?.let { TvCastSeeAllOnly(seeAllLabel, it, initialFocused, modifier) }
        return
    }
    TvDetailSlotCarousel(
        items = people,
        key = { it.key },
        aspect = PORTRAIT_ASPECT,
        slotHeight = dimensionResource(TvR.dimen.tv_detail_cast_slot_height),
        stripHeight = dimensionResource(TvR.dimen.tv_detail_cast_strip_height),
        onClick = onCastClick,
        image = { TvCastCardAvatar(it.profileUrl) },
        caption = { TvDetailCaption(it.name, it.role) },
        modifier = modifier,
        initialPosition = initialPosition,
        initialFocused = initialFocused,
        // The full list takes crew as well, so the card carries no count; its peek is the first portraits that have a photo.
        seeAll = onSeeAll?.let { open ->
            TvSlotCarouselSeeAll(
                seeAllLabel,
                people
                    .filter {
                        it.profileUrl != null
                    }.take(SEE_ALL_PEEK)
                    .map { person -> { TvCastCardAvatar(person.profileUrl) } },
                open,
            )
        },
    )
}

/** The Cast tab's one focus stop when [TvDetailCast] has no portrait: the See-all card at the slot's size, ringed while focused. */
@Composable
private fun TvCastSeeAllOnly(
    label: String,
    onClick: () -> Unit,
    initialFocused: Boolean,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(initialFocused) }
    val slotHeight = dimensionResource(TvR.dimen.tv_detail_cast_slot_height)
    Column(
        modifier = modifier.fillMaxSize().padding(start = tvContentGutterStart()),
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.tvClickable(role = Role.Button, onFocusChanged = { focused = it }, onClick = onClick).readAs(label)) {
            TvStripSeeAllCard(
                label = label,
                isFocused = focused,
                onClick = {},
                width = slotHeight * PORTRAIT_ASPECT,
                height = slotHeight,
                peek = emptyList(),
            )
        }
    }
}

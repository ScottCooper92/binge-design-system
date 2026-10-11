package com.binge.designsystem.tv.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.badgeCountLabel
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.TvArrivalFocus
import com.binge.designsystem.tv.focus.tvArrivalTarget
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvSelectionFocusGroup
import com.binge.designsystem.tv.focus.tvSelectionTarget
import kotlin.coroutines.cancellation.CancellationException
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * Tints a count's sub-pill from its own content colour, so it reads on the resting tab and on the focus fill
 * without a colour token of its own.
 */
internal const val TAB_COUNT_SCRIM_ALPHA = 0.18f

/**
 * A row of mutually exclusive tabs over what they switch: which slice of a collection, which season, which mode.
 *
 * **Selection follows focus**, as tabs do on a television: moving onto a tab selects it, with nothing to press. So
 * entry into the row is routed to the selected tab rather than left to a directional search, which would select
 * whichever tab sits nearest the beam. Arriving on the tab already selected commits nothing. For a switch that
 * should wait for OK, use [TvChoiceRow].
 *
 * The selected tab is marked with [TvRowEmphasis]: the full fill while the row holds focus, a dim accent once focus
 * has moved on to what the tab shows. The row scrolls, and brings the selected tab into view when the selection
 * arrives from elsewhere. [initiallyFocused] seeds the row as focused for a frame; production passes false.
 * [entryFocusRequester] and [arrival] let a page aim focus at the row, landing on the selected tab either way.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvTabRow(
    tabs: List<TvTabUi>,
    selectedKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    initiallyFocused: Boolean = false,
    entryFocusRequester: FocusRequester? = null,
    arrival: TvArrivalFocus? = null,
) {
    if (tabs.isEmpty()) return
    // One flag for the whole row is enough, because the focused tab is always the selected one.
    var rowHasFocus by remember { mutableStateOf(initiallyFocused) }
    val selectedEntry = remember { FocusRequester() }
    val selectedIntoView = remember { BringIntoViewRequester() }
    LaunchedEffect(selectedKey, tabs) {
        runCatching { selectedIntoView.bringIntoView() }.onFailure { if (it is CancellationException) throw it }
    }
    Row(
        modifier =
            modifier
                .onFocusChanged { rowHasFocus = it.hasFocus }
                // Without the scroll, a walked-to tab in an overflowing row is laid out off-screen with nothing to
                // bring it into view, and focus flies off the edge.
                .horizontalScroll(rememberScrollState())
                // Outside the group, so a request aimed at the row lands in it and is routed to the selected tab.
                .then(entryFocusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
                .then(arrival?.let { Modifier.tvArrivalTarget(it) } ?: Modifier)
                .tvSelectionFocusGroup(selectedEntry),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        tabs.forEach { tab ->
            val isSelected = tab.key == selectedKey
            TvTab(
                label = tab.label,
                count = tab.count,
                emphasis = tvRowEmphasis(isDescribedRow = isSelected, columnHasFocus = rowHasFocus),
                selected = isSelected,
                onSelect = { onSelect(tab.key) },
                modifier =
                    Modifier
                        .tvSelectionTarget(isSelected, selectedEntry)
                        .then(if (isSelected) Modifier.bringIntoViewRequester(selectedIntoView) else Modifier),
            )
        }
    }
}

/**
 * One tab of a [TvTabRow]: the key the caller switches on, the word the user reads, and an optional [count] of what
 * the tab's slice holds. A key rather than an index, because a position means something different per screen.
 */
@Immutable
data class TvTabUi(
    val key: String,
    val label: String,
    val count: TvTabCount = TvTabCount.None,
)

/**
 * Whether a tab carries a count, and whether that count is known yet.
 *
 * Three cases rather than a nullable count, because the two absences pull opposite ways: a row with no counts must
 * not reserve a pill, while a row whose counts have not arrived must, or the tabs reflow when the numbers land.
 */
@Immutable
sealed interface TvTabCount {
    /** This row shows no counts. No pill, and no width reserved for one. */
    data object None : TvTabCount

    /** The count has not arrived. The pill is drawn at its floor width, holding a dash. */
    data object Unknown : TvTabCount

    /** A settled count. Zero is a real answer and reads `0`. */
    data class Known(
        val value: Int,
    ) : TvTabCount
}

/**
 * One tab's pill, coloured by [emphasis]. No border at any emphasis, so a tab cannot be mistaken for a filter chip.
 * Both commit paths are guarded on [selected], so neither re-selects the current tab and re-runs what it shows.
 */
@Composable
private fun TvTab(
    label: String,
    count: TvTabCount,
    emphasis: TvRowEmphasis,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = emphasis.contentColor(resting = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(
        modifier =
            modifier
                .height(dimensionResource(TvR.dimen.tv_button_height))
                .clip(BingeShapes.Pill)
                .background(emphasis.containerColor(resting = Color.Transparent))
                .tvClickable(
                    onFocusChanged = { gained -> if (gained && !selected) onSelect() },
                    onClick = { if (!selected) onSelect() },
                ).padding(horizontal = dimensionResource(TvR.dimen.tv_choice_padding_horizontal))
                .semantics {
                    role = Role.Tab
                    this.selected = selected
                },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val countLabel =
            when (count) {
                TvTabCount.None -> null
                TvTabCount.Unknown -> stringResource(TvR.string.tv_tab_count_unknown)
                is TvTabCount.Known -> badgeCountLabel(count.value)
            }
        countLabel?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .clip(BingeShapes.Pill)
                        .background(contentColor.copy(alpha = TAB_COUNT_SCRIM_ALPHA))
                        .widthIn(min = dimensionResource(TvR.dimen.tv_tab_count_min_width))
                        .padding(horizontal = dimensionResource(DesR.dimen.padding_s)),
            )
        }
    }
}

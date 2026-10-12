package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.TvArrivalFocus
import com.binge.designsystem.tv.focus.tvArrivalTarget
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusContentColor
import com.binge.designsystem.tv.focus.tvFocusFill
import com.binge.designsystem.tv.focus.tvSelectionFocusGroup
import com.binge.designsystem.tv.focus.tvSelectionTarget
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * A short row of mutually exclusive options where **OK is the commit**: the chosen one carries a tick, and moving
 * focus across the row changes nothing until the user presses.
 *
 * The deliberate opposite of [TvTabRow], where focus is the commit. Use this where a switch is costly, such as one
 * that re-runs a query or drops what the user picked under the old value. Pressing the option already chosen is a
 * no-op, guarded here so no caller has to remember to. The pills sit side by side, so they carry no second line;
 * [TvOptionRow] is the stacked form that does. [initialFocusedKey] seeds one option as focused for a frame;
 * production passes null.
 *
 * [entryFocusRequester] and [arrival] let a page aim focus at the row, as [TvTabRow]'s do: the request lands on the
 * chosen option, and so does focus entering the row from outside. Without either, entry is the ordinary search.
 */
@Composable
fun TvChoiceRow(
    choices: List<TvChoiceUi>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialFocusedKey: String? = null,
    entryFocusRequester: FocusRequester? = null,
    arrival: TvArrivalFocus? = null,
) {
    val selectedEntry = remember { FocusRequester() }
    val aimed = entryFocusRequester != null || arrival != null
    Row(
        modifier =
            modifier
                .selectableGroup()
                // Outside the group, so a request aimed at the row lands in it and is routed to the chosen option.
                .then(entryFocusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
                .then(arrival?.let { Modifier.tvArrivalTarget(it) } ?: Modifier)
                .then(if (aimed) Modifier.tvSelectionFocusGroup(selectedEntry) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        choices.forEach { choice ->
            val isSelected = choice.key == selectedKey
            TvChoice(
                label = choice.label,
                selected = isSelected,
                initiallyFocused = choice.key == initialFocusedKey,
                onSelect = { if (!isSelected) onSelect(choice.key) },
                icon = choice.icon,
                modifier = Modifier.tvSelectionTarget(isSelected, selectedEntry),
            )
        }
    }
}

/**
 * One option of a [TvChoiceRow]: the key the caller switches on, and the word the user reads. A key rather than an
 * index, because a position means something different the moment the set changes.
 */
@Immutable
data class TvChoiceUi(
    val key: String,
    val label: String,
    val icon: ImageVector? = null,
)

/**
 * One option's pill. Focus fills it; being chosen adds the tick, and the two are independent. The tick takes the
 * label's colour, so it inverts with the fill rather than vanishing into it. Selection is announced as well as drawn.
 * An [icon] leads the label on its tinted plate.
 */
@Composable
private fun TvChoice(
    label: String,
    selected: Boolean,
    initiallyFocused: Boolean,
    onSelect: () -> Unit,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    val contentColor = tvFocusContentColor(isFocused = focused, resting = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(
        modifier =
            modifier
                .height(dimensionResource(TvR.dimen.tv_button_height))
                .clip(BingeShapes.Pill)
                .tvFocusFill(isFocused = focused, shape = BingeShapes.Pill)
                .tvClickable(onFocusChanged = { focused = it }, onClick = onSelect)
                .padding(horizontal = dimensionResource(TvR.dimen.tv_choice_padding_horizontal))
                .semantics {
                    role = Role.RadioButton
                    this.selected = selected
                },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        icon?.let { TvRowIcon(icon = it, isFocused = focused) }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (selected) TvSelectedTick(tint = contentColor)
    }
}

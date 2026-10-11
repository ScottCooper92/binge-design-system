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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusContentColor
import com.binge.designsystem.tv.focus.tvFocusFill
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * A short row of mutually exclusive options where **OK is the commit**: the chosen one carries a tick, and moving
 * focus across the row changes nothing until the user presses.
 *
 * The deliberate opposite of [TvTabRow], where focus is the commit. Use this where a switch is costly, such as one
 * that re-runs a query or drops what the user picked under the old value. Pressing the option already chosen is a
 * no-op, guarded here so no caller has to remember to. [initialFocusedKey] seeds one option as focused for a frame;
 * production passes null.
 */
@Composable
fun TvChoiceRow(
    choices: List<TvChoiceUi>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialFocusedKey: String? = null,
) {
    Row(
        modifier = modifier.selectableGroup(),
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
)

/**
 * One option's pill. Focus fills it; being chosen adds the tick, and the two are independent. The tick takes the
 * label's colour, so it inverts with the fill rather than vanishing into it. Selection is announced as well as drawn.
 */
@Composable
private fun TvChoice(
    label: String,
    selected: Boolean,
    initiallyFocused: Boolean,
    onSelect: () -> Unit,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    val contentColor = tvFocusContentColor(isFocused = focused, resting = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(
        modifier =
            Modifier
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

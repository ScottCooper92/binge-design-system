package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvPaneEntry
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * A filter as a range — rating, content rating. Minimum and Maximum are pinned above the scale, each showing its bound, and
 * the one picked is the one the scale sets; "Any" clears that bound. Back leaves the category in one press, as does Left from the header, the Minimum tab or the scale.
 *
 * @param onChange the range as `(from, to)` after a pick, already clamped so a bound that would cross the other has cleared it
 * @param initialBound the bound the scale sets on entry
 * @param offScaleLabel how a stored bound that is on no step reads on its tab, rather than as "Any"
 * @param rank how bounds are ordered when the scale's keys carry a value of their own; the step's place when null
 */
@Composable
internal fun TvFilterRange(
    label: String,
    steps: List<TvRangeStep>,
    from: String?,
    to: String?,
    onBack: () -> Unit,
    onChange: (Pair<String?, String?>) -> Unit,
    entry: FocusRequester,
    copy: TvFilterPaneCopy,
    modifier: Modifier = Modifier,
    stepIcon: ImageVector? = null,
    initialBound: TvRangeBound = TvRangeBound.Min,
    offScaleLabel: (String) -> String = { it },
    rank: ((String) -> Float)? = null,
    seed: TvFilterRangeSeed = TvFilterRangeSeed(),
    onEntryPlaced: () -> Unit = {},
    entryLooksFocused: Boolean = false,
) {
    var bound by rememberSaveable { mutableStateOf(initialBound) }
    val current = if (bound == TvRangeBound.Min) from else to
    val anyLabel = copy.any
    val labelOf = { key: String? -> key?.let { k -> steps.firstOrNull { it.key == k }?.label ?: offScaleLabel(k) } ?: anyLabel }
    val leftGoesBack = Modifier.tvFilterLeftGoesBack(onBack)
    Column(modifier) {
        TvFilterRow(
            label = label,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onClick = onBack,
            initiallyFocused = seed.header,
            modifier = leftGoesBack,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(DesR.dimen.padding_xs)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
        ) {
            TvFilterBoundTab(
                title = copy.minimum,
                value = labelOf(from),
                selected = bound == TvRangeBound.Min,
                initiallyFocused = seed.minTab,
                onClick = { bound = TvRangeBound.Min },
                modifier = Modifier.weight(1f).then(leftGoesBack),
            )
            TvFilterBoundTab(
                title = copy.maximum,
                value = labelOf(to),
                selected = bound == TvRangeBound.Max,
                initiallyFocused = seed.maxTab,
                onClick = { bound = TvRangeBound.Max },
                modifier = Modifier.weight(1f),
            )
        }
        TvFilterDivider()
        TvFilterScale(
            steps = steps,
            current = current,
            stepIcon = stepIcon,
            seedKey = seed.key,
            entry = entry,
            anyLabel = anyLabel,
            onEntryPlaced = onEntryPlaced,
            entryLooksFocused = entryLooksFocused,
            onPick = {
                onChange(
                    if (rank ==
                        null
                    ) {
                        rangeAfterPick(steps, bound, from, to, it)
                    } else {
                        rangeAfterPick(steps, bound, from, to, it, rank)
                    },
                )
            },
            onBack = onBack,
            modifier = Modifier.weight(1f),
        )
    }
}

/** The scale under the tabs: "Any", then each step, the current one ticked and the one focus returns to. */
@Composable
private fun TvFilterScale(
    steps: List<TvRangeStep>,
    current: String?,
    stepIcon: ImageVector?,
    seedKey: String?,
    entry: FocusRequester,
    anyLabel: String,
    onPick: (String?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    entryLooksFocused: Boolean = false,
    onEntryPlaced: () -> Unit = {},
) {
    val leftGoesBack = Modifier.tvFilterLeftGoesBack(onBack)
    // A stored bound on no step has no row to return to, so focus enters on Any rather than on nothing.
    val onScale = current == null || steps.any { it.key == current }
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_xs)),
        contentPadding = PaddingValues(
            top = dimensionResource(DesR.dimen.padding_xs),
            bottom = dimensionResource(TvR.dimen.tv_overscan_vertical),
        ),
    ) {
        item(key = "any") {
            TvFilterRow(
                label = anyLabel,
                trailing = if (current == null) Icons.Filled.Check else null,
                reserveTrailing = true,
                initiallyFocused = seedKey == TV_FILTER_KEY_ANY || (entryLooksFocused && (current == null || !onScale)),
                onClick = { onPick(null) },
                modifier = leftGoesBack.then(if (current == null || !onScale) Modifier.tvPaneEntry(entry, onEntryPlaced) else Modifier),
            )
        }
        items(steps, key = { "step-${it.key}" }) { step ->
            TvFilterRow(
                label = step.label,
                icon = stepIcon,
                trailing = if (current == step.key) Icons.Filled.Check else null,
                reserveTrailing = true,
                initiallyFocused = seedKey == step.key || (entryLooksFocused && current == step.key),
                onClick = { onPick(step.key) },
                modifier = leftGoesBack.then(if (current == step.key) Modifier.tvPaneEntry(entry, onEntryPlaced) else Modifier),
            )
        }
    }
}

/** One bound of a range: its name over its value, filled faintly while it is the one the scale sets. */
@Composable
private fun TvFilterBoundTab(
    title: String,
    value: String,
    selected: Boolean,
    initiallyFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    val scheme = MaterialTheme.colorScheme
    val container = when {
        focused -> scheme.onSurface
        selected -> scheme.onSurface.copy(alpha = TV_FILTER_DIVIDER_ALPHA)
        else -> Color.Transparent
    }
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .tvClickable(role = Role.Tab, onFocusChanged = { focused = it }, onClick = onClick)
            .padding(horizontal = dimensionResource(DesR.dimen.padding_m), vertical = dimensionResource(DesR.dimen.padding_s)),
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall, color = if (focused) scheme.surface else scheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, color = if (focused) scheme.surface else scheme.onSurface)
    }
}

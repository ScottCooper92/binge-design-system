package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.tv.component.TvFilterCategory
import com.binge.designsystem.tv.component.TvFilterValue
import com.binge.designsystem.tv.component.TvFilterValueState
import com.binge.designsystem.tv.component.TvSwitch
import com.binge.designsystem.tv.focus.tvPaneEntry
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** ← goes back to the list from anything that has nothing further left, as Back does. */
internal fun Modifier.tvFilterLeftGoesBack(onBack: () -> Unit): Modifier =
    onPreviewKeyEvent { event ->
        val left = event.key == Key.DirectionLeft && event.type == KeyEventType.KeyDown
        if (left) onBack()
        left
    }

/**
 * One category's values, under a row naming it that leads back to the list: for a multi-select, a "2 selected … Clear" row
 * always (Clear once anything is chosen), and its Match all switch, all pinned. [entryKey] is the value focus returns to.
 * Back, or ←, leaves the category in one press and lands on it in the list.
 */
@Composable
internal fun TvFilterValues(
    category: TvFilterCategory,
    entryKey: String?,
    onFocused: (String) -> Unit,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
    onClearCategory: () -> Unit,
    entry: FocusRequester,
    copy: TvFilterPaneCopy,
    modifier: Modifier = Modifier,
    matchAllKey: String = TV_FILTER_KEY_MATCH_ALL,
    anyKey: String = TV_FILTER_KEY_ANY,
    pinnedRow: (option: TvFilterValue, selected: Int) -> TvFilterPinnedRowSpec? = { _, _ -> null },
    valueExtras: (categoryKey: String, value: TvFilterValue) -> TvFilterValueExtras? = { _, _ -> null },
    seed: TvFilterSeed = TvFilterSeed(),
    onEntryPlaced: () -> Unit = {},
    entryLooksFocused: Boolean = false,
) {
    val selected = category.selectedCount(anyKey)
    val joinOption = category.pinnedOptions.firstOrNull { it.key == matchAllKey }
    val servicesOption = category.pinnedOptions.firstOrNull { pinnedRow(it, selected) != null }
    val rows = category.pinnedOptions.filterNot { it == joinOption || it == servicesOption } + category.values
    val pinnedFocus = remember { TvFilterPinnedFocus() }
    // The header, Clear and the any/all switch stay put above the values, which scroll beneath them.
    Column(modifier) {
        TvFilterValuesPinned(
            category = category,
            joinOption = joinOption,
            servicesOption = servicesOption,
            pinnedRow = pinnedRow,
            selected = selected,
            copy = copy,
            focus = pinnedFocus,
            seed = seed,
            onBack = onBack,
            onToggle = onToggle,
            onClearCategory = onClearCategory,
        )
        // Every sub-view pins at least its header, so the rule always parts the pinned rows from the values.
        TvFilterDivider()
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_xs)),
            contentPadding = PaddingValues(
                top = dimensionResource(DesR.dimen.padding_xs),
                bottom = dimensionResource(TvR.dimen.tv_overscan_vertical),
            ),
        ) {
            val focusKey = rows.firstOrNull { it.key == entryKey }?.key ?: category.values.firstOrNull()?.key ?: rows.firstOrNull()?.key
            items(rows, key = { "value-${it.key}" }) { value ->
                TvFilterValueRow(
                    value = value,
                    extras = valueExtras(category.key, value),
                    seeded = value.key == seed.key || (entryLooksFocused && value.key == focusKey),
                    onToggle = onToggle,
                    onFocused = onFocused,
                    modifier = Modifier.tvFilterLeftGoesBack(onBack).then(
                        if (value.key ==
                            focusKey
                        ) {
                            Modifier.tvPaneEntry(entry, onEntryPlaced)
                        } else {
                            Modifier
                        },
                    ),
                )
            }
        }
    }
}

/** The requesters Back steps between: the header, the count (or services) row, and the switch. */
private class TvFilterPinnedFocus(
    val header: FocusRequester = FocusRequester(),
    val count: FocusRequester = FocusRequester(),
    val switch: FocusRequester = FocusRequester(),
)

/** One value: the app's extras (a flag, an own name, a code) where it has them, and its tick or cross. */
@Composable
private fun TvFilterValueRow(
    value: TvFilterValue,
    extras: TvFilterValueExtras?,
    seeded: Boolean,
    onToggle: (String) -> Unit,
    onFocused: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TvFilterRow(
        label = value.label,
        detail = extras?.detail,
        leadingText = extras?.leadingText,
        code = extras?.code,
        icon = value.icon,
        logoUrl = value.logoUrl,
        trailing = value.marker(),
        reserveTrailing = true,
        excluded = value.state == TvFilterValueState.Excluded,
        onClick = { onToggle(value.key) },
        onFocused = { onFocused(value.key) },
        initiallyFocused = seeded,
        modifier = modifier,
    )
}

/**
 * The rows pinned above the values: the header leading back, then for a multi-select its count with Clear, or Where to
 * watch's services row, then the Match all switch. A multi-select always shows its count and, where it has one, its
 * switch, so ticking a value never moves the list.
 */
@Composable
private fun TvFilterValuesPinned(
    category: TvFilterCategory,
    joinOption: TvFilterValue?,
    servicesOption: TvFilterValue?,
    pinnedRow: (TvFilterValue, Int) -> TvFilterPinnedRowSpec?,
    selected: Int,
    copy: TvFilterPaneCopy,
    focus: TvFilterPinnedFocus,
    seed: TvFilterSeed,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
    onClearCategory: () -> Unit,
) {
    val leftGoesBack = Modifier.tvFilterLeftGoesBack(onBack)
    val belowHeader = Modifier.padding(top = dimensionResource(DesR.dimen.padding_xs))
    TvFilterRow(
        label = category.label,
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        onClick = onBack,
        initiallyFocused = seed.header,
        modifier = leftGoesBack.focusRequester(focus.header),
    )
    val pinnedSpec = servicesOption?.let { pinnedRow(it, selected) }
    if (servicesOption != null && pinnedSpec != null) {
        TvFilterAppPinnedRow(
            spec = pinnedSpec,
            clearLabel = copy.clear,
            onClear = onClearCategory,
            onUse = { onToggle(servicesOption.key) },
            initiallyFocused = seed.pinned,
            modifier = leftGoesBack.focusRequester(focus.count).then(belowHeader),
        )
    } else if (category.multiSelect) {
        TvFilterClearRow(
            text = if (selected == 0) copy.noneSelected else copy.selectedCount(selected),
            canClear = selected > 0,
            clearLabel = copy.clear,
            // Clearing makes this row unfocusable, so focus goes to a pinned control that stays: the switch, else the header.
            onClear = {
                runCatching { (if (joinOption != null) focus.switch else focus.header).requestFocus() }
                onClearCategory()
            },
            initiallyFocused = seed.pinned,
            modifier = leftGoesBack.focusRequester(focus.count).then(belowHeader),
        )
    }
    if (joinOption != null) {
        TvFilterRow(
            label = joinOption.label,
            onClick = { onToggle(joinOption.key) },
            trailingContent = { focused -> TvSwitch(checked = joinOption.state != TvFilterValueState.Neutral, isFocused = focused) },
            initiallyFocused = seed.switch,
            modifier = leftGoesBack.focusRequester(focus.switch),
        )
    }
}

/**
 * The one pinned row the app words itself (Where to watch's services row, in the host app): its [spec] gives the text and icon, and
 * says whether a press clears the category or uses the row's own value. It is one composable whose text follows the spec, so the
 * focused row is never replaced and focus stays put across the swap. Where focus enters and leaves it is decided here, not by the
 * caller.
 */
@Composable
private fun TvFilterAppPinnedRow(
    spec: TvFilterPinnedRowSpec,
    clearLabel: String,
    onClear: () -> Unit,
    onUse: () -> Unit,
    initiallyFocused: Boolean,
    modifier: Modifier = Modifier,
) {
    TvFilterPinnedRow(
        text = spec.text,
        icon = spec.icon,
        action = if (spec.clears) clearLabel else null,
        focusable = true,
        onClick = if (spec.clears) onClear else onUse,
        initiallyFocused = initiallyFocused,
        modifier = modifier,
    )
}

/**
 * What a pinned option's row says, for an app that words one itself: [text] and an optional [icon]; [clears] says a press clears the
 * category (and the row then ends in Clear) rather than using the option's own value.
 */
class TvFilterPinnedRowSpec(
    val text: String,
    val icon: ImageVector? = null,
    val clears: Boolean = false,
)

/** What a value row adds: a flag or other leading text, a second line, and a trailing code. */
class TvFilterValueExtras(
    val leadingText: String? = null,
    val detail: String? = null,
    val code: String? = null,
)

/** How many of the category's values are chosen or ruled out, the value keyed [anyKey] not being a choice. */
internal fun TvFilterCategory.selectedCount(anyKey: String): Int =
    values.count { it.state != TvFilterValueState.Neutral && it.key != anyKey }

/** The default key of a switch-like pinned option that joins a multi-select's values (match all). */
const val TV_FILTER_KEY_MATCH_ALL: String = "match_all"

/** The default key of a value that stands for "no choice", which a count does not include. */
const val TV_FILTER_KEY_ANY: String = "any"

/** A chosen value's tick, a ruled-out one's cross, and nothing for the rest. */
private fun TvFilterValue.marker(): ImageVector? =
    when (state) {
        TvFilterValueState.Included -> Icons.Filled.Check
        TvFilterValueState.Excluded -> Icons.Filled.Close
        TvFilterValueState.Neutral -> null
    }

package com.binge.designsystem.tv.component

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One filter, and the values it can take.
 *
 * The values are always a list, even for what the phone models as a continuous range (year, rating, vote
 * count): a D-pad has no gesture for a slider here, since ← / → move between the two panes. Callers bucket a
 * range into named options ("Any", "2020s") first, which also gives the left column a summary to derive.
 *
 * [icon] leads the row. Unlike Settings, which omits icons, rows here differ in *kind* (an ordering, a
 * taxonomy, a date range, a service list), so an icon is a scan target readable from a sofa.
 */
data class TvFilterCategory(
    val key: String,
    val label: String,
    val values: List<TvFilterValue>,
    val icon: ImageVector? = null,
    /**
     * Whether the values are checkboxes that can be combined (genres, services) rather than radio rows that
     * replace one another (a sort order, a range). Tri-state [TvFilterValueState.Excluded] needs it.
     */
    val multiSelect: Boolean = false,
    /**
     * Rows pinned above [values] that act *on* the values rather than being one of them — the AND/OR control,
     * or a bulk shortcut like "My services".
     *
     * Kept out of [values], not flagged inside it, so the summary and chip derivations that read [values] as
     * "what the user chose" can't mistake a control for a choice. Rendered above the values behind a divider
     * as ordinary focusable rows, so they're reachable without a second gesture.
     */
    val pinnedOptions: List<TvFilterValue> = emptyList(),
)

/**
 * One value within a category. [state] is selection, not focus — the two are independent: the chosen
 * genre keeps its tick while the focus fill passes over it. An [action] opens something rather than being chosen
 * (a pinned shortcut to a settings screen): it announces as a button, with no tick, and ignores [logoUrl],
 * [excluded][TvFilterValueState.Excluded] state and any leading slot, since the button row takes only a label and icon.
 */
data class TvFilterValue(
    val key: String,
    val label: String,
    val state: TvFilterValueState = TvFilterValueState.Neutral,
    val logoUrl: String? = null,
    val icon: ImageVector? = null,
    val action: Boolean = false,
)

/**
 * What a value row is doing to the query: nothing, narrowing to this, or ruling this out.
 *
 * An enum, not two booleans, so "included and excluded at once" can't be written. Not the phone's
 * `IncludeExcludeState` because this module must not depend on a feature module; the feature maps
 * between them. All three states cycle from OK on one row (neutral → included → excluded → neutral), since
 * tv-material's `ListItem` has no `onLongClick` and a long-press is invisible on a remote.
 */
enum class TvFilterValueState {
    Neutral,
    Included,
    Excluded,
}

/**
 * The state for a value that is either chosen ([TvFilterValueState.Included]) or not ([TvFilterValueState.Neutral]).
 * For single-select rows, where [TvFilterValueState.Excluded] is deliberately absent: excluding a sort order is
 * meaningless, and a third state would let one press produce an inexpressible query.
 */
fun chosenIf(chosen: Boolean): TvFilterValueState = if (chosen) TvFilterValueState.Included else TvFilterValueState.Neutral

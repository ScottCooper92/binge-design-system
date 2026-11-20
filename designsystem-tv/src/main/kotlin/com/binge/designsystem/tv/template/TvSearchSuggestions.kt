package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.tv.focus.tvFocusContentColor
import com.binge.designsystem.tv.focus.tvFocusFill
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.R as TvR

/** Where a suggestion came from, which is the only thing its icon says: a clock for [Recent], a magnifier for [Suggested]. */
enum class TvSearchSuggestionSource { Recent, Suggested }

/** One line of the list under the search field: the [text] a press searches for, and where it [source]d from. */
data class TvSearchSuggestion(
    val text: String,
    val source: TvSearchSuggestionSource,
) {
    /** What the page remembers the focused row by, so it comes back to it. */
    val key: String get() = "${source.name}_$text"
}

/** The list under the query field: what you searched before, then what is suggested. */
@Composable
internal fun TvSearchSuggestions(
    suggestions: List<TvSearchSuggestion>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    entryFocusRequester: FocusRequester? = null,
    initialFocusedKey: String? = null,
    restoredKey: String? = null,
    onRowFocused: (String) -> Unit = {},
    firstRowAnchor: String? = null,
    firstIconAnchor: String? = null,
    loading: Boolean = false,
) {
    // Where the column was left, so ↓ into the results and ↑ back returns to the same row rather than the top.
    // `focusRestorer` is not enough on its own: with nothing saved it falls back to the geometric search, which
    // is the landing this replaces.
    val columnEntry = remember { FocusRequester() }
    var lastFocusedKey by remember { mutableStateOf(restoredKey) }
    val entryKey = lastFocusedKey?.takeIf { key -> suggestions.any { it.key == key } }
        ?: suggestions.firstOrNull()?.key
    // While [loading] with nothing yet, the same rows are plates: focusable, inert, and the rows the suggestions arrive in.
    val rows = if (suggestions.isEmpty() && loading) List(SUGGESTION_PLATE_COUNT) { null } else suggestions
    // A plain Column, not a lazy one: the list scrolls as a whole from outside, so it never needs a lazy column of its own.
    Column(
        modifier = modifier.fillMaxWidth().tvEntryFocusGroup(columnEntry),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_search_key_gap)),
    ) {
        rows.forEachIndexed { index, suggestion ->
            val isEntry = if (suggestion == null) index == 0 else suggestion.key == entryKey
            TvSuggestionRow(
                suggestion = suggestion,
                index = index,
                initiallyFocused = suggestion != null && suggestion.key == initialFocusedKey,
                onClick = { suggestion?.let { onSuggestionClick(it.text) } },
                onFocusGained = {
                    if (suggestion != null) {
                        lastFocusedKey = suggestion.key
                        onRowFocused(suggestion.key)
                    }
                },
                iconModifier = if (index == 0 && firstIconAnchor != null) Modifier.tvLayoutAnchor(firstIconAnchor) else Modifier,
                modifier = Modifier
                    .then(if (index == 0 && firstRowAnchor != null) Modifier.tvLayoutAnchor(firstRowAnchor) else Modifier)
                    .suggestionEntry(isEntry, columnEntry, entryFocusRequester),
            )
        }
    }
}

/** The row that takes entry carries both requesters: the column's own and the screen's. */
private fun Modifier.suggestionEntry(
    isEntry: Boolean,
    columnEntry: FocusRequester,
    screenEntry: FocusRequester?,
): Modifier =
    if (!isEntry) {
        this
    } else {
        focusRequester(columnEntry).then(if (screenEntry != null) Modifier.focusRequester(screenEntry) else Modifier)
    }

@Composable
private fun TvSuggestionRow(
    suggestion: TvSearchSuggestion?,
    index: Int,
    initiallyFocused: Boolean,
    onClick: () -> Unit,
    onFocusGained: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
) {
    var focused by rememberFocusFlag(initiallyFocused)
    val content = tvFocusContentColor(isFocused = focused, resting = MaterialTheme.colorScheme.onSurface)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(TvR.dimen.tv_search_suggestion_height))
            .clip(BingeShapes.Pill)
            .tvFocusFill(isFocused = focused, shape = BingeShapes.Pill)
            .tvClickable(
                onFocusChanged = { gained ->
                    focused = gained
                    if (gained) onFocusGained()
                },
                onClick = onClick,
            ).padding(horizontal = dimensionResource(TvR.dimen.tv_search_suggestion_gap)),
    ) {
        if (suggestion == null) {
            TvSuggestionRowPlate(index, iconModifier)
        } else {
            Icon(
                imageVector = when (suggestion.source) {
                    TvSearchSuggestionSource.Recent -> Icons.Filled.History
                    TvSearchSuggestionSource.Suggested -> Icons.Filled.Search
                },
                contentDescription = null,
                tint = content,
                modifier = iconModifier.size(dimensionResource(TvR.dimen.tv_search_suggestion_icon)),
            )
            Spacer(Modifier.width(dimensionResource(TvR.dimen.tv_search_suggestion_gap)))
            Text(
                text = suggestion.text,
                style = MaterialTheme.typography.bodyMedium,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

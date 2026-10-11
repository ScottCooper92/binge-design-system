package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.component.TvIllustration
import com.binge.designsystem.tv.component.TvOptionRow
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.focus.tvFocusGroup
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * A [TvListPaneBoard]'s end pane: the row's hero or illustration, its name, what it means, its options and a
 * closing note. The only place a value changes; the list only names rows. Nothing to describe leaves it empty.
 *
 * It scrolls, because a column that cannot scroll starves its last children into slivers that are still
 * focusable. ← from any option goes to [backToListFocus], the described row.
 */
@Composable
internal fun TvListPane(
    row: TvPaneRow?,
    backToListFocus: FocusRequester,
    modifier: Modifier = Modifier,
    initialFocusedOptionLabel: String? = null,
) {
    if (row == null) {
        Box(modifier = modifier.fillMaxSize())
        return
    }
    TvStableFocusScroll {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = dimensionResource(TvR.dimen.tv_focus_ring_bleed)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val hero = row.hero
            val icon = row.icon
            when {
                hero != null -> hero()
                icon != null -> TvIllustration(icon = icon)
            }
            Text(
                text = row.label,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = row.body,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (row.options.isNotEmpty()) {
                PaneOptions(
                    options = row.options,
                    backToListFocus = backToListFocus,
                    initialFocusedOptionLabel = initialFocusedOptionLabel,
                )
            }
            row.note?.let { note -> PaneNote(note = note, announce = row.announceNote) }
        }
    }
}

/**
 * The options, at one fixed width and centred: a pane-wide row would split each label from its tick, and one width
 * for the set makes the options scan as a set. → back into the pane resumes on the option last focused.
 */
@Composable
private fun PaneOptions(
    options: List<TvPaneOption>,
    backToListFocus: FocusRequester,
    initialFocusedOptionLabel: String?,
) {
    Column(
        modifier =
            Modifier
                .width(dimensionResource(TvR.dimen.tv_list_pane_option_width))
                .padding(top = dimensionResource(DesR.dimen.padding_s))
                .focusProperties { left = backToListFocus }
                .tvFocusGroup(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_option_row_gap)),
    ) {
        options.forEach { option ->
            TvOptionRow(
                label = option.label,
                selected = option.selected,
                onClick = option.onSelect,
                initiallyFocused = option.label == initialFocusedOptionLabel,
                modifier = option.focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier,
            )
        }
    }
}

/** The pane's closing note, read politely when [announce] says it reports an outcome. */
@Composable
private fun PaneNote(note: String, announce: Boolean) {
    Text(
        text = note,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .padding(top = dimensionResource(DesR.dimen.padding_xs))
                .then(if (announce) Modifier.semantics { liveRegion = LiveRegionMode.Polite } else Modifier),
    )
}

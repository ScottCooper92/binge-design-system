package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeTheme
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val SELECTED_TAB_ALPHA = 0.2f

/**
 * The tabs, behind a chevron that says which way the page will fold. At rest OK or ↓ on a tab expands onto it; expanded,
 * focusing a tab shows it and ↑ folds the page.
 */
@Composable
internal fun TvDetailTabStrip(
    tabs: List<TvDetailTab>,
    selectedKey: String?,
    expanded: Boolean,
    requesters: Map<String, FocusRequester>,
    onExpand: (String) -> Unit,
    onFocusTab: (String) -> Unit,
    onUpFromTop: () -> Unit,
    modifier: Modifier = Modifier,
    focusedKey: String? = null,
) {
    var focused by remember { mutableStateOf(focusedKey) }
    // Resolved here, from the live selection: a requester captured when the movable strip was first made would go stale.
    val entry = selectedKey?.let { requesters[it] } ?: requesters.values.firstOrNull() ?: return
    Row(
        modifier = modifier.tvEntryFocusGroup(entry),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = BingeTheme.colors.onScrim,
            modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)),
        )
        tabs.forEachIndexed { index, tab ->
            key(tab.key) {
                TvDetailTabItem(
                    label = tab.label,
                    selected = expanded && tab.key == selectedKey,
                    isFocused = focused == tab.key,
                    modifier = Modifier
                        .tvLayoutAnchor(TvDetailAnchors.tab(index))
                        .focusRequester(requesters.getValue(tab.key))
                        .tvDetailTabKeys(expanded, { onExpand(tab.key) }, onUpFromTop),
                    onFocusChanged = { isFocused ->
                        if (isFocused) {
                            focused = tab.key
                            onFocusTab(tab.key)
                        } else if (focused == tab.key) {
                            focused = null
                        }
                    },
                    onClick = { onExpand(tab.key) },
                )
            }
        }
    }
}

/** At rest ↓ on a tab expands onto it; expanded, ↑ on a tab folds the page. */
private fun Modifier.tvDetailTabKeys(
    expanded: Boolean,
    onDown: () -> Unit,
    onUp: () -> Unit,
): Modifier =
    onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        when {
            !expanded && event.key == Key.DirectionDown -> {
                onDown()
                true
            }
            expanded && event.key == Key.DirectionUp -> {
                onUp()
                true
            }
            else -> false
        }
    }

/** One tab: filled when [isFocused], tinted when [selected] (the open tab), plain otherwise. */
@Composable
internal fun TvDetailTabItem(
    label: String,
    selected: Boolean,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container = when {
        isFocused -> scheme.onSurface
        selected -> scheme.onSurface.copy(alpha = SELECTED_TAB_ALPHA)
        else -> Color.Transparent
    }
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        color = if (isFocused) scheme.surface else scheme.onSurface,
        maxLines = 1,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .tvClickable(role = Role.Tab, onFocusChanged = onFocusChanged, onClick = onClick)
            .padding(
                horizontal = dimensionResource(TvR.dimen.tv_detail_tab_item_padding_horizontal),
                vertical = dimensionResource(DesR.dimen.padding_s),
            ),
    )
}

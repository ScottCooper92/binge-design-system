package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.clearAndSetSemantics

/** The list column's width as a fraction of the pane it is drawn in, from the board's own split. */
private val ListColumnInPane = TvTwoPaneSplit.ListDetail.copyShare / (1f - TvTwoPaneSplit.ListDetail.copyShare)

/**
 * A [TvListPaneBoard]'s list column on its own, as it will look once a row steps into it: [groups] at the list
 * column's width, with no row described. It goes in a row's `paneContent`, so the pane shows the real next level
 * rather than a summary of it. It is inert: focus cannot enter it and a screen reader skips it, since the row that
 * leads to it carries what it says and OK or → on that row opens it.
 */
@Composable
fun TvListPaneListPreview(groups: List<TvPaneGroup>, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth(ListColumnInPane)
                .focusProperties { onEnter = { cancelFocusChange() } }
                .focusGroup()
                .clearAndSetSemantics {},
    ) {
        TvPaneList(groups = groups, describedKey = null, onFocusRow = {}, describedRowFocus = remember { FocusRequester() })
    }
}

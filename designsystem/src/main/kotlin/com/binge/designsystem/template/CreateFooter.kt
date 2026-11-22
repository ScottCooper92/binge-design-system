package com.binge.designsystem.template

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.binge.designsystem.component.BingeFilledButton

/**
 * A page's create action as the footer's one primary button: always in the same place, within thumb reach, and not scrolled away
 * with the list. Pass it as [BingeScreenScaffold]'s `bottomBar`. It is the commit CLAUDE.md puts in the footer for a screen whose action is
 * the reason it exists, for a list page that is not a form; a form's own commit is `FormActionPlacement.Footer`.
 *
 * It is part of the page's structure, so it does not pop in: [visible] says whether the page has the action at all (not on a failure
 * that replaces the page, and not on a sub-page), [enabled] whether it can be used yet (not while the list is still loading), and
 * [loading] that the create it starts is in flight.
 *
 * @need pin a create or add action to the bottom of a list page
 * @group bars
 * @replaces a hand-built bottom bar holding a full-width create button
 */
@Composable
fun CreateFooter(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    if (!visible) return
    ScreenFooter(modifier = modifier) {
        BingeFilledButton(
            label = label,
            onClick = onClick,
            leadingIcon = icon,
            enabled = enabled,
            loading = loading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

package com.binge.designsystem.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R

/**
 * The space a task sheet's first row keeps from the top edge. A bottom sheet's drag handle stands there already; the side
 * sheet a task sheet becomes on a wide window has none, so a page written for the bottom sheet gets the same clearance.
 *
 * @need keep a task sheet page clear of the top edge
 * @group sheets
 * @replaces a hand-built top spacer in a sheet page
 */
@Composable
fun TaskSheetTopClearance(nav: BingeSheetNav, modifier: Modifier = Modifier) {
    if (nav.inSideSheet) Spacer(modifier.height(dimensionResource(R.dimen.padding_m)))
}

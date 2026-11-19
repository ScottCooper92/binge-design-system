package com.binge.designsystem.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeBottomSheet
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.component.HintCard

/**
 * One filter in a sheet of its own, stacked over a [FilterSheet]: its title with a Clear of its own, a [hint] where the
 * control needs one, the [section]'s control, and a single [doneLabel] button that only closes the sheet, since each edit
 * goes to the caller as it is made. [footerAccessory] sits beside the button at its height, for a control that belongs to
 * the filter but is not one of the section's own (a match-all / match-any toggle, say).
 *
 * @need edit one filter of a filter sheet in a sheet of its own
 * @group sheets
 */
@Composable
fun FilterSectionSheet(
    section: FilterSectionUi,
    clearLabel: String,
    clearEnabled: Boolean,
    doneLabel: String,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    handlers: FilterControlHandlers = FilterControlHandlers(),
    footerAccessory: (@Composable RowScope.() -> Unit)? = null,
) {
    BingeBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        FilterSectionContent(
            section = section,
            clearLabel = clearLabel,
            clearEnabled = clearEnabled,
            doneLabel = doneLabel,
            onClear = onClear,
            onDone = onDismiss,
            hint = hint,
            handlers = handlers,
            footerAccessory = footerAccessory,
        )
    }
}

/**
 * The body of [FilterSectionSheet] without its host, for the side sheet's in-place detail or a frame. A non-null [onBack]
 * draws the back arrow a detail shown in place of the rows needs.
 *
 * @need show one filter's detail inside a host the caller already has
 * @group sheets
 */
@Composable
fun ColumnScope.FilterSectionContent(
    section: FilterSectionUi,
    clearLabel: String,
    clearEnabled: Boolean,
    doneLabel: String,
    onClear: () -> Unit,
    onDone: () -> Unit,
    hint: String? = null,
    handlers: FilterControlHandlers = FilterControlHandlers(),
    onBack: (() -> Unit)? = null,
    footerAccessory: (@Composable RowScope.() -> Unit)? = null,
) {
    SectionSheetHeader(
        title = section.title,
        clearLabel = clearLabel,
        clearEnabled = clearEnabled,
        onClear = onClear,
        onBack = onBack,
    )
    Column(
        modifier =
            Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = dimensionResource(R.dimen.filter_sheet_padding_h),
                    end = dimensionResource(R.dimen.filter_sheet_padding_h),
                    bottom = dimensionResource(R.dimen.padding_m),
                ),
    ) {
        if (hint != null) {
            HintCard(text = hint, modifier = Modifier.padding(bottom = dimensionResource(R.dimen.filter_sheet_section_title_gap)))
        }
        FilterSectionControl(section = section, callbacks = handlers)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.padding_m))
                .padding(top = dimensionResource(R.dimen.padding_sm), bottom = dimensionResource(R.dimen.padding_l)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
    ) {
        BingeFilledButton(label = doneLabel, onClick = onDone, modifier = Modifier.weight(1f))
        footerAccessory?.invoke(this)
    }
}

@Composable
private fun SectionSheetHeader(
    title: String,
    clearLabel: String,
    clearEnabled: Boolean,
    onClear: () -> Unit,
    onBack: (() -> Unit)?,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(if (onBack != null) R.dimen.padding_xs else R.dimen.filter_sheet_padding_h),
                    end = dimensionResource(R.dimen.filter_sheet_padding_h),
                    top = dimensionResource(R.dimen.padding_sm),
                    bottom = dimensionResource(R.dimen.padding_sm),
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_navigate_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        BingeTextButton(label = clearLabel, onClick = onClear, enabled = clearEnabled)
    }
}

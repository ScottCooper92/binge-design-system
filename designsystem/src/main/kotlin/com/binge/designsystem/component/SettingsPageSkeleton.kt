package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.binge.designsystem.R
import com.binge.designsystem.resolvedContentInset

/**
 * The height of one [ItemGroup] row at the current font size: the taller of its icon plate and its label with a detail line
 * under it, plus the row's padding above and below. The text is measured in sp, so the height follows the font scale as
 * the settled row does. Without a [detail] line the text is one line.
 */
@Composable
internal fun itemGroupRowHeight(detail: Boolean = true): Dp {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val typography = MaterialTheme.typography
    val text =
        with(density) {
            val label = measurer
                .measure("A", typography.titleMedium)
                .size.height
                .toDp()
            if (detail) {
                label + dimensionResource(R.dimen.account_group_label_detail_spacing) +
                    measurer
                        .measure("A", typography.bodySmall)
                        .size.height
                        .toDp()
            } else {
                label
            }
        }
    return max(dimensionResource(R.dimen.item_group_icon_size), text) + dimensionResource(R.dimen.item_group_row_padding_v) * 2
}

/**
 * The loading state of a settings page: [rows] placeholders, each as tall as an [ItemGroup] row at the current font size, so
 * the page settles into its groups without jumping. [contentPadding] is the scaffold's inner padding.
 *
 * A page that leads with an intro passes [intro] (a [PageIntro]) or [plainIntro] (a plain line of text) and gets the form's
 * own content inset around the body, which [formInset] sets on its own for a page with no intro. The intro is shown as its
 * own text, not a plate, so the first row lands where the first group does at any font size. [lead] is what the settled page
 * puts between the intro and its first group.
 *
 * [description] names the loading state for a screen reader, once for the page. The words are the caller's. [rowDetail] is
 * false for rows that settle to a label alone.
 *
 * @need show a loading placeholder for a settings page of grouped rows
 * @group feedback
 * @replaces a hand-built ListRowSkeletonColumn sized to settings rows
 */
@Composable
fun SettingsPageSkeleton(
    contentPadding: PaddingValues,
    rows: Int,
    modifier: Modifier = Modifier,
    intro: String? = null,
    plainIntro: String? = null,
    formInset: Boolean = intro != null || plainIntro != null,
    description: String? = null,
    rowDetail: Boolean = true,
    lead: (@Composable () -> Unit)? = null,
) {
    val inset = if (formInset) resolvedContentInset() else 0.dp
    val direction = LocalLayoutDirection.current
    // The settled body spaces its blocks by padding_l, the skeleton column by list_row_spacing: the header makes up the difference.
    val gap = (dimensionResource(R.dimen.padding_l) - dimensionResource(R.dimen.list_row_spacing)).coerceAtLeast(0.dp)
    ListRowSkeletonColumn(
        contentPadding =
            PaddingValues(
                start = contentPadding.calculateStartPadding(direction) + inset,
                top = contentPadding.calculateTopPadding() + inset,
                end = contentPadding.calculateEndPadding(direction) + inset,
                bottom = contentPadding.calculateBottomPadding() + inset,
            ),
        modifier = modifier,
        count = rows,
        height = itemGroupRowHeight(rowDetail),
        description = description,
        header =
            (intro ?: plainIntro)?.let { text ->
                {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
                        modifier = Modifier.padding(bottom = gap),
                    ) {
                        if (intro != null) PageIntro(text) else Text(text, style = MaterialTheme.typography.bodyMedium)
                        lead?.invoke()
                    }
                }
            },
    )
}

package com.binge.designsystem.tv.component

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.template.TvDetailPageScope
import com.binge.designsystem.uppercaseLocalised
import kotlin.math.ceil
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** One label/value read-out in a [TvDetailInfoFooter]. Blank [value]s are dropped by the footer. */
data class TvInfoGroup(
    val label: String,
    val value: String,
)

/** Up to this many groups flow into two columns; more take three. */
private const val MAX_TWO_COLUMN_GROUPS = 5

/** How a [TvDetailInfoFooter] lays its groups out. */
enum class TvInfoFooterLayout {
    /**
     * Label over value, flowed into 2–3 columns by group count. The default: a label-left table tops out at two
     * columns before a value has no room left, so this is the only layout that spans the panel's full width for
     * seven groups.
     */
    Columns,

    /**
     * A single column of label-left / value-right rows on one alignment axis. Every value starts at the same x,
     * so the block reads as a table at ten feet, but it wraps to its widest row rather than filling the panel,
     * which used about half the screen on a real device. Kept as an escape hatch for a short block.
     */
    Table,
}

/** How a [TvDetailInfoFooter] styles its section header, independent of [TvInfoFooterLayout]. */
enum class TvInfoFooterHeaderStyle {
    /** The detail pages' standard row heading. */
    Large,

    /** A tighter weight, for a page whose rows above it are smaller. */
    Medium,
}

/**
 * The **Details** tail of a TV detail page: a header plus long-tail metadata (dates, status, languages, who
 * made it or asked for it) as label/value groups. [layout] and [headerStyle] are independent.
 *
 * The block is one focus target that draws a ring, not a fill: content scrolls by focus stop, so a
 * non-focusable block below the last row could be unreachable. Blank groups drop out.
 */
@Composable
fun TvDetailInfoFooter(
    header: String,
    groups: List<TvInfoGroup>,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    layout: TvInfoFooterLayout = TvInfoFooterLayout.Columns,
    headerStyle: TvInfoFooterHeaderStyle = TvInfoFooterHeaderStyle.Large,
) {
    val shown = groups.filter { it.value.isNotBlank() }
    if (shown.isEmpty()) return
    Column(
        modifier = modifier
            .padding(
                start = tvContentGutterStart(),
                end = dimensionResource(TvR.dimen.tv_overscan_horizontal) +
                    dimensionResource(TvR.dimen.tv_focus_ring_bleed),
            )
            // Ring before the inner padding, so it sits just outside the block's edge like every other TV
            // focus stop; the block has no fill, so the ring is the only focus signal.
            .tvFocusIndicator(isFocused = isFocused, shape = BingeShapes.Medium)
            .onFocusChanged { onFocusChanged(it.isFocused) }
            .focusable()
            .padding(dimensionResource(DesR.dimen.padding_m)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
    ) {
        Text(
            text = header,
            style = when (headerStyle) {
                TvInfoFooterHeaderStyle.Large -> MaterialTheme.typography.titleLarge
                TvInfoFooterHeaderStyle.Medium -> MaterialTheme.typography.titleMedium
            },
            color = MaterialTheme.colorScheme.onSurface,
        )
        when (layout) {
            TvInfoFooterLayout.Columns -> TvInfoFooterColumns(shown)
            TvInfoFooterLayout.Table -> TvInfoFooterTable(shown)
        }
    }
}

/** The `TvDetailPage` entry key the Details footer is registered under, on every page that ends on one. */
const val TV_DETAILS_SECTION_KEY: String = "details"

/**
 * Register [TvDetailInfoFooterSection] as the page's closing section, and only when it has rows to show:
 * registered unconditionally it draws nothing yet still costs a list entry and a gap.
 *
 * [header] and [groups] are resolved by the caller because the page's section builder isn't a composable
 * scope, so the emptiness has to be known before the section is registered. The guard mirrors the footer's
 * own blank-value filter, not `isEmpty()`, so an all-blank group list is treated as empty here too.
 */
fun TvDetailPageScope.detailsSection(
    header: String,
    groups: List<TvInfoGroup>,
    key: String = TV_DETAILS_SECTION_KEY,
    layout: TvInfoFooterLayout = TvInfoFooterLayout.Columns,
    headerStyle: TvInfoFooterHeaderStyle = TvInfoFooterHeaderStyle.Large,
    initialFocused: Boolean = false,
) {
    if (groups.none { it.value.isNotBlank() }) return
    section(key) { _ ->
        TvDetailInfoFooterSection(
            header = header,
            groups = groups,
            layout = layout,
            headerStyle = headerStyle,
            initialFocused = initialFocused,
        )
    }
}

/**
 * The Details block itself: [TvDetailInfoFooter] wrapped with the `isFocused` state it needs, so a caller
 * doesn't hand-roll the focus wiring. Reach for [detailsSection] rather than this to put it on a
 * [com.binge.designsystem.tv.template.TvDetailPage]. [initialFocused] seeds the ring for a screenshot and is
 * `false` in production.
 */
@Composable
fun TvDetailInfoFooterSection(
    header: String,
    groups: List<TvInfoGroup>,
    modifier: Modifier = Modifier,
    layout: TvInfoFooterLayout = TvInfoFooterLayout.Columns,
    headerStyle: TvInfoFooterHeaderStyle = TvInfoFooterHeaderStyle.Large,
    initialFocused: Boolean = false,
) {
    var focused by remember { mutableStateOf(initialFocused) }
    TvDetailInfoFooter(
        header = header,
        groups = groups,
        isFocused = focused,
        onFocusChanged = { focused = it },
        modifier = modifier,
        layout = layout,
        headerStyle = headerStyle,
    )
}

@Composable
private fun TvInfoFooterColumns(shown: List<TvInfoGroup>) {
    val columnCount = if (shown.size <= MAX_TWO_COLUMN_GROUPS) 2 else 3
    val perColumn = ceil(shown.size / columnCount.toFloat()).toInt()
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
        verticalAlignment = Alignment.Top,
    ) {
        shown.chunked(perColumn).forEach { column ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
            ) {
                column.forEach { group -> TvInfoGroupItem(group) }
            }
        }
    }
}

@Composable
private fun TvInfoGroupItem(group: TvInfoGroup) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_xs))) {
        Text(
            text = group.label.uppercaseLocalised(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = group.value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TvInfoFooterTable(shown: List<TvInfoGroup>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_detail_info_row_gap)),
    ) {
        shown.forEach { group ->
            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            ) {
                Text(
                    text = group.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(dimensionResource(TvR.dimen.tv_detail_info_label_width)),
                )
                Text(
                    text = group.value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

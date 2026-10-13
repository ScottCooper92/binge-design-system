package com.binge.designsystem.tv.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.tv.component.TvDetailInfoFooterSection
import com.binge.designsystem.tv.component.TvInfoFooterHeaderStyle
import com.binge.designsystem.tv.component.TvInfoFooterLayout
import com.binge.designsystem.tv.component.TvInfoGroup

private const val SAMPLE_HEADER = "Details"

private val FiveGroups = listOf(
    TvInfoGroup("Directed by", "Denis Villeneuve"),
    TvInfoGroup("Release date", "1 March 2024"),
    TvInfoGroup("Language", "English"),
    TvInfoGroup("Studios", "Legendary Pictures · Warner Bros."),
    TvInfoGroup("Status", "Released"),
)

private val SevenGroups = FiveGroups + listOf(
    TvInfoGroup("Networks", "HBO · Max"),
    TvInfoGroup("Ep. runtime", "58 min"),
)

/** Five groups, label over value, flowing into two columns: the footer at rest. */
@Composable
fun TvDetailInfoFooterSample() {
    TvDetailInfoFooterSection(header = SAMPLE_HEADER, groups = FiveGroups)
}

/** Seven groups take three columns, with the ring lit: focus is the block's only signal. */
@Composable
fun TvDetailInfoFooterFocusedSample() {
    TvDetailInfoFooterSection(header = SAMPLE_HEADER, groups = SevenGroups, initialFocused = true)
}

/** The tighter header, for a page whose rows above it are smaller. */
@Composable
fun TvDetailInfoFooterMediumHeaderSample() {
    TvDetailInfoFooterSection(header = SAMPLE_HEADER, groups = FiveGroups, headerStyle = TvInfoFooterHeaderStyle.Medium)
}

/** The single-axis table: label left, every value on one alignment line. */
@Composable
fun TvDetailInfoFooterTableSample() {
    TvDetailInfoFooterSection(header = SAMPLE_HEADER, groups = FiveGroups, layout = TvInfoFooterLayout.Table)
}

/** The table with the ring lit. */
@Composable
fun TvDetailInfoFooterTableFocusedSample() {
    TvDetailInfoFooterSection(
        header = SAMPLE_HEADER,
        groups = FiveGroups,
        layout = TvInfoFooterLayout.Table,
        initialFocused = true,
    )
}

package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.badgeCountLabel
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** Whether a row with these values draws anything in its trailing slot. */
internal fun hasTvRowStatus(
    badgeCount: Int?,
    badgeAlert: Boolean,
    loading: Boolean,
): Boolean = loading || badgeAlert || (badgeCount ?: 0) > 0

/**
 * A list row's trailing mark: a spinner while its value loads, else a "!" pill when it needs the user, else its count
 * on the tab row's pill. Loading wins over the alert, and the alert over the count. Everything is in the row's content
 * colour except the alert, so it inverts with the focus fill. The badges speak as words rather than as their glyph;
 * the spinner says nothing, because the row announces that it is in progress.
 */
@Composable
internal fun TvRowStatus(
    badgeCount: Int?,
    badgeAlert: Boolean,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    val content = LocalContentColor.current
    when {
        loading ->
            TvSpinner(
                color = content,
                diameter = dimensionResource(TvR.dimen.tv_list_pane_row_spinner),
                stroke = dimensionResource(TvR.dimen.tv_list_pane_row_spinner_stroke),
                modifier = modifier,
            )
        badgeAlert ->
            StatusPill(
                text = stringResource(DesR.string.badge_alert),
                description = stringResource(TvR.string.tv_pane_row_badge_alert),
                textColor = MaterialTheme.colorScheme.onError,
                pillColor = MaterialTheme.colorScheme.error,
                modifier = modifier,
            )
        badgeCount != null && badgeCount > 0 ->
            StatusPill(
                text = badgeCountLabel(badgeCount),
                description = pluralStringResource(TvR.plurals.tv_pane_row_badge_count, badgeCount, badgeCount),
                textColor = content,
                pillColor = content.copy(alpha = TAB_COUNT_SCRIM_ALPHA),
                modifier = modifier,
            )
    }
}

@Composable
private fun StatusPill(
    text: String,
    description: String,
    textColor: Color,
    pillColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .clearAndSetSemantics { contentDescription = description }
                .clip(BingeShapes.Pill)
                .background(pillColor)
                .widthIn(min = dimensionResource(TvR.dimen.tv_tab_count_min_width))
                .padding(horizontal = dimensionResource(DesR.dimen.padding_s)),
    )
}

package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.BingeIcons
import com.binge.designsystem.DISABLED_ALPHA
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.contrastSafeOn
import com.binge.designsystem.theme.labelLargeEmphasis

/**
 * A pill that opens a screen's filters: the compass and the [label], with how many filters are applied.
 *
 * With no filter applied the pill is a plain `surfaceContainerHigh` one. With [activeCount] above 0 it fills with `primary`, takes
 * the contrast-safe ink on it, and shows a [CountBadge], so the state reads at a glance. The words are parameters, so each app
 * words and translates its own: [label] is what the pill says, and [contentDescription] is what a screen reader announces, which
 * should include the count when filters are applied ("Filters, 3 applied").
 *
 * The pill is one click target and one accessibility item with the button role. A pill that is not [enabled] takes no taps and dims by
 * [DISABLED_ALPHA].
 *
 * @param activeCount how many filters are applied; 0 shows none.
 * @param enabled whether the pill takes taps.
 * @param onClick called when the pill is tapped.
 * @param label the pill's text.
 * @param contentDescription what a screen reader announces.
 *
 * @need open a screen's filters from a pill that shows how many are applied
 * @group inputs
 * @replaces a hand-built pill with a compass, a label and a count
 */
@Composable
fun FilterPill(
    activeCount: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    label: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val active = activeCount > 0
    val container = if (active) colors.primary else colors.surfaceContainerHigh
    val content = if (active) colors.onPrimary else colors.onSurface
    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(BingeShapes.Pill)
            .background(container)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .clearAndSetSemantics {
                this.contentDescription = contentDescription
                role = Role.Button
                if (enabled) {
                    onClick {
                        onClick()
                        true
                    }
                }
            }.padding(
                horizontal = dimensionResource(R.dimen.filter_pill_padding_h),
                vertical = dimensionResource(R.dimen.padding_s),
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.filter_pill_spacing)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = BingeIcons.Discover,
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.filter_pill_icon)),
            tint = content,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLargeEmphasis,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (active) CountBadge(count = activeCount, containerColor = colors.onPrimary, contentColor = colors.primary)
    }
}

/**
 * The filter action of a collapsing top bar: the compass as an icon button that arrives as the bar collapses, standing in for the
 * [FilterPill] that scrolled away.
 *
 * It fades in with [collapsedFraction] (0 hidden, 1 fully shown), and while it is hidden it takes no taps and is not an accessibility
 * item, so a screen reader never lands on an invisible button. With [activeCount] above 0 the icon takes the `primary` tint, kept at
 * its contrast against the bar, and a [CountBadge] in `error` and `onError` sits at its corner. The caller words
 * [contentDescription], with the count when filters are applied. A button that is not [enabled] takes no taps and dims by
 * [DISABLED_ALPHA].
 *
 * @param activeCount how many filters are applied; 0 shows no badge.
 * @param enabled whether the action takes taps.
 * @param collapsedFraction how far the bar has collapsed, from 0 to 1; the action's alpha.
 * @param contentDescription what a screen reader announces.
 * @param onClick called when the action is tapped.
 *
 * @need offer a screen's filters from the top bar once a pill has scrolled away
 * @group bars
 * @replaces a hand-built top-bar icon button with a compass and a count
 */
@Composable
fun CollapsedFilterAction(
    activeCount: Int,
    enabled: Boolean,
    collapsedFraction: Float,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val fraction = collapsedFraction.coerceIn(0f, 1f)
    val visible = fraction > 0f
    val active = activeCount > 0
    val tint = if (active) contrastSafeOn(colors.background, colors.primary) else LocalContentColor.current
    Box(
        modifier = modifier
            .alpha(fraction * (if (enabled) 1f else DISABLED_ALPHA))
            .then(if (visible) Modifier else Modifier.clearAndSetSemantics {}),
    ) {
        IconButton(onClick = onClick, enabled = enabled && visible) {
            BadgedBox(
                badge = {
                    if (active) CountBadge(count = activeCount, containerColor = colors.error, contentColor = colors.onError)
                },
            ) {
                Icon(imageVector = BingeIcons.Discover, contentDescription = contentDescription, tint = tint)
            }
        }
    }
}

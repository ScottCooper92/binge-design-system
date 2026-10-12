package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * A TV page's frame: the theme background, the overscan-safe insets [hosting] calls for, an optional
 * [TvScreenHeading] on the rail's top line, and [content] below it.
 *
 * The frame every board, list and form page sits in, so none of them hand-roll the padding and background.
 * [entry] is where focus lands when it enters the page; whether the page also claims it is [hosting]'s call
 * (see [TvPageHosting]), and [arrivalEnabled] holds the claim back until the target exists. [trailingContent] sits in
 * the heading band, so it is not shown when [title] is null.
 */
@Composable
fun TvBoard(
    title: String?,
    modifier: Modifier = Modifier,
    hosting: TvPageHosting = currentTvPageHosting(),
    entry: FocusRequester? = null,
    arrivalEnabled: Boolean = true,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .then(tvPageArrival(hosting, entry, arrivalEnabled, key = Unit))
                .padding(tvPagePadding(hosting)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
    ) {
        if (title != null) TvScreenHeading(title = title, trailingContent = trailingContent)
        content()
    }
}

/**
 * [TvBoard] under its old name for the heading slot, `trailing`; it is `trailingContent` now. Kotlin cannot hold two
 * overloads that differ only in parameter names, so this one has no default for `trailing`: a call that passes none
 * resolves to the new function, and a call that passes `trailing` by name lands here.
 */
@Deprecated(
    message = "Renamed: trailing is trailingContent.",
    replaceWith = ReplaceWith("TvBoard(title, modifier, hosting, entry, arrivalEnabled, trailingContent = trailing, content = content)"),
)
@Suppress("ModifierParameter", "LongParameterList")
@Composable
fun TvBoard(
    title: String?,
    trailing: (@Composable RowScope.() -> Unit)?,
    modifier: Modifier = Modifier,
    hosting: TvPageHosting = currentTvPageHosting(),
    entry: FocusRequester? = null,
    arrivalEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) = TvBoard(
    title = title,
    modifier = modifier,
    hosting = hosting,
    entry = entry,
    arrivalEnabled = arrivalEnabled,
    trailingContent = trailing,
    content = content,
)

/**
 * A page's title, in a band as tall as the rail's header line so the two share a centre line. [trailingContent] sits
 * at the band's end, unweighted, so an action there is never squeezed out by a long title.
 */
@Composable
fun TvScreenHeading(
    title: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(dimensionResource(TvR.dimen.tv_screen_heading_height)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailingContent?.invoke(this)
    }
}

/** [TvScreenHeading] under its old name for the end slot, `trailing`; see the deprecated [TvBoard]. */
@Deprecated(
    message = "Renamed: trailing is trailingContent.",
    replaceWith = ReplaceWith("TvScreenHeading(title, modifier, trailingContent = trailing)"),
)
@Suppress("ModifierParameter")
@Composable
fun TvScreenHeading(
    title: String,
    trailing: (@Composable RowScope.() -> Unit)?,
    modifier: Modifier = Modifier,
) = TvScreenHeading(title = title, modifier = modifier, trailingContent = trailing)

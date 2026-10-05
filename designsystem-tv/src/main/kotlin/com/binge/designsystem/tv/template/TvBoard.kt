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
 * (see [TvPageHosting]), and [arrivalEnabled] holds the claim back until the target exists.
 */
@Composable
fun TvBoard(
    title: String?,
    modifier: Modifier = Modifier,
    hosting: TvPageHosting = currentTvPageHosting(),
    entry: FocusRequester? = null,
    arrivalEnabled: Boolean = true,
    trailing: (@Composable RowScope.() -> Unit)? = null,
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
        if (title != null) TvScreenHeading(title = title, trailing = trailing)
        content()
    }
}

/**
 * A page's title, in a band as tall as the rail's header line so the two share a centre line. [trailing] sits
 * at the band's end, unweighted, so an action there is never squeezed out by a long title.
 */
@Composable
fun TvScreenHeading(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
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
        trailing?.invoke(this)
    }
}

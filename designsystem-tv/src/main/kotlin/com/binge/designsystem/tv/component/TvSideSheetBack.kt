package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * A page's way back as a top app bar draws it: an arrow and the name of the page Back returns to. Nothing on the root page.
 * It is no control and takes no focus; the remote's Back is the action. [TvSideSheetTitle] draws it above its own text, and a
 * page with no title of its own (a header leads it) calls it directly.
 */
@Composable
fun TvSideSheetBack(modifier: Modifier = Modifier) {
    val parent = LocalTvSideSheetPage.current?.parentTitle ?: return
    Row(
        modifier = modifier.padding(bottom = dimensionResource(DesR.dimen.padding_s)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)),
        )
        Text(
            text = parent,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

package com.binge.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R

/**
 * The bar above a stacked sheet page: Back at the start, as a page that is not the first of its stack has no other way out but the scrim, and
 * the page's [title] beside it. [BingeAdaptiveSheet] draws this above every stacked page, so a page of an app's own that draws it too reads the same.
 *
 * The [title] is announced as a heading and ellipsizes on one line; with none the bar is Back alone. With a [backLabel], the name of the page Back
 * returns to sits beside the arrow and the pair is one target. [enabled] false leaves Back drawn but inert, for the moment a page is being left.
 *
 * @need show the back arrow and title above a stacked sheet page
 * @group bars
 * @replaces a hand-built Row of back button and title on a sheet page
 */
@Composable
fun BingeSheetPageHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    enabled: Boolean = true,
    backLabel: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.padding_xs)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (backLabel == null) {
            IconButton(onClick = onBack, enabled = enabled) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_navigate_back))
            }
        } else {
            Row(
                modifier = Modifier
                    .clickable(enabled = enabled, role = Role.Button, onClick = onBack)
                    .minimumInteractiveComponentSize()
                    .padding(horizontal = dimensionResource(R.dimen.padding_s)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_navigate_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = backLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = dimensionResource(R.dimen.padding_s))
                    .semantics { heading() },
            )
        }
    }
}

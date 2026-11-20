@file:CatalogGroup("Filters")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.CollapsedFilterAction
import com.binge.designsystem.component.CountBadge
import com.binge.designsystem.component.FilterPill
import com.binge.designsystem.preview.ScreenshotTheme

private fun described(count: Int) = if (count == 0) "Filters" else "Filters, $count applied"

/**
 * [FilterPill] with no filter applied, three and twelve, side by side. Tap one to apply another: its count goes up and the
 * announcement follows. The words are the sample's own, since each app words its own.
 */
@Composable
fun FilterPillSample() {
    ScreenshotTheme {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_s)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
                listOf(0, 3, 12).forEach { initial ->
                    var count by remember { mutableIntStateOf(initial) }
                    FilterPill(
                        activeCount = count,
                        enabled = true,
                        onClick = { count++ },
                        label = "Filters",
                        contentDescription = described(count),
                    )
                }
            }
            FilterPill(activeCount = 3, enabled = false, onClick = {}, label = "Filters", contentDescription = described(3))
        }
    }
}

/**
 * [CollapsedFilterAction] with no filter applied and with three, each hidden (a collapsed fraction of 0) and fully shown (1). A hidden
 * action draws nothing and takes no taps; the captions say which is which. Tap a shown one to apply another filter.
 */
@Composable
fun CollapsedFilterActionSample() {
    ScreenshotTheme {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_s)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
        ) {
            listOf(0 to 0f, 0 to 1f, 3 to 0f, 3 to 1f).forEach { (initial, fraction) ->
                var count by remember { mutableIntStateOf(initial) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CollapsedFilterAction(
                        activeCount = count,
                        enabled = true,
                        collapsedFraction = fraction,
                        contentDescription = described(count),
                        onClick = { count++ },
                    )
                    Text(text = "$count, ${fraction.toInt()}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * [CountBadge] with one digit, two, and three (which reads "99+"), in the pill's colours and the action's. The number stays at the
 * same size at any font scale, so the badge never outgrows the control it sits on.
 */
@Composable
fun CountBadgeSample() {
    ScreenshotTheme {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_s)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(1, 12, 150).forEach { count ->
                CountBadge(
                    count = count,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
            listOf(1, 12, 150).forEach { count ->
                CountBadge(
                    count = count,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                )
            }
        }
    }
}

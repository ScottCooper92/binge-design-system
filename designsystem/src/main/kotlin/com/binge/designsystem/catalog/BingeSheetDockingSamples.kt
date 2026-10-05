@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeSheetTopBar
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.component.DockingHeaderLayout
import com.binge.designsystem.component.DockingSheetShape
import com.binge.designsystem.preview.ScreenshotTheme

private const val SHEET_TITLE = "Filters"
private const val SHEET_SUBTITLE = "3 applied"
private const val SHEET_ACTION = "Clear all"
private const val SHEET_BODY = "The sheet's own content scrolls under its header."
private const val HALF_DOCKED = 0.5f

/** The bar a docking sheet shows once it fills the screen: close, a title with a subtitle, and an action. */
@Composable
fun BingeSheetTopBarSample() {
    ScreenshotTheme {
        BingeSheetTopBar(
            title = SHEET_TITLE,
            subtitle = SHEET_SUBTITLE,
            onClose = {},
            actions = { BingeTextButton(label = SHEET_ACTION, onClick = {}) },
        )
    }
}

/**
 * A docking sheet's top at three points of its travel: floating (drag handle and header, round corners), half-way
 * through the crossfade, and docked (the top bar, square corners). `BingeBottomSheet` is a modal window that
 * does not capture, so this renders the header and the sheet's outline directly, at a fixed fraction each.
 */
@Composable
fun BingeSheetDockingHeaderSample() {
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
            listOf(0f, HALF_DOCKED, 1f).forEach { fraction -> DockingSheetTop(fraction) }
        }
    }
}

@Composable
private fun DockingSheetTop(fraction: Float) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(DockingSheetShape(fraction))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        DockingHeaderLayout(
            fraction = { fraction },
            header = {
                Text(
                    text = SHEET_TITLE,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_m)),
                )
            },
            dockedTopBar = {
                BingeSheetTopBar(
                    title = SHEET_TITLE,
                    subtitle = SHEET_SUBTITLE,
                    onClose = {},
                    actions = { BingeTextButton(label = SHEET_ACTION, onClick = {}) },
                )
            },
        )
        Text(
            text = SHEET_BODY,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        )
    }
}

@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.binge.designsystem.component.BingeSheetHeader
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.component.DockingSheetShape
import com.binge.designsystem.preview.ScreenshotTheme

private const val HEADER_TITLE = "Upcoming episodes"
private const val HEADER_SUBTITLE = "The Expanse"
private const val HEADER_CAPTION = "Marking one watched also marks every episode before it."

/** The full header: title, what it is for, an action at the end, and the caption across the width below. */
@Composable
fun BingeSheetHeaderSample() {
    ScreenshotTheme {
        HeaderSurface {
            BingeSheetHeader(HEADER_TITLE, subtitle = HEADER_SUBTITLE, caption = HEADER_CAPTION) {
                BingeTextButton(label = "Done", onClick = {})
            }
        }
    }
}

/** The smallest header: a title alone. */
@Composable
fun BingeSheetHeaderTitleOnlySample() {
    ScreenshotTheme {
        HeaderSurface { BingeSheetHeader(HEADER_TITLE) }
    }
}

/** The sheet's surface around a header, as a floating sheet draws it. */
@Composable
private fun HeaderSurface(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(DockingSheetShape(0f))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        content = content,
    )
}

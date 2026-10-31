@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.bingeSheetWash
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.TaskSheetBottomChrome
import com.binge.designsystem.preview.TaskSheetPreviewFrame

private const val DETAIL = "detail"
private const val SAMPLE_WASH = 0xFFB3261EL

/**
 * A task sheet, as the window shows it: a side sheet on the end edge at the expanded width (840dp and up), the bottom
 * sheet it was below that. Both are the sheet's resting chrome without the modal window around it, which does not capture,
 * so the scrim and the slide are not framed. The list behind is a plain surface where an app's list-detail layout would be.
 */
@Composable
fun BingeAdaptiveTaskSheetSample() {
    ScreenshotTheme {
        SheetBackdrop { TaskSheetPreviewFrame { SampleRootPage() } }
    }
}

/**
 * The task sheet after a row opened a second page. At the expanded width the page stacks inside the side sheet under a
 * Back; below it the page is the second bottom sheet an app opens there, since pages only stack in the side sheet.
 */
@Composable
fun BingeAdaptiveTaskSheetStackedSample() {
    ScreenshotTheme {
        SheetBackdrop { TaskSheetPreviewFrame(page = DETAIL) { SampleDetailPage() } }
    }
}

/**
 * The task sheet with its header washed: the block that leads the page asks the sheet to take its colour, from the top of the
 * sheet down behind the header, fading into the sheet below it.
 */
@Composable
fun BingeAdaptiveTaskSheetWashSample() {
    ScreenshotTheme {
        SheetBackdrop {
            TaskSheetPreviewFrame {
                SampleWashedHeader()
                SampleRow("Seasons")
                SampleRow("Quality profile")
                SampleBottomSpace()
            }
        }
    }
}

/** A page opened over a washed one: Back names the page it returns to, and the page brings its own wash. */
@Composable
fun BingeAdaptiveTaskSheetBackLabelSample() {
    ScreenshotTheme {
        SheetBackdrop {
            TaskSheetPreviewFrame(page = DETAIL, backLabel = "Companions") {
                SampleWashedHeader()
                SampleRow("Season 1")
                SampleRow("Season 2")
                SampleBottomSpace()
            }
        }
    }
}

/** A menu sheet: a short choice, a capped bottom sheet at every width. */
@Composable
fun BingeAdaptiveMenuSheetSample() {
    ScreenshotTheme {
        SheetBackdrop {
            TaskSheetBottomChrome {
                SampleRow("Open in browser")
                SampleRow("Copy link")
                SampleRow("Share")
                SampleBottomSpace()
            }
        }
    }
}

@Composable
private fun SheetBackdrop(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        content = content,
    )
}

@Composable
private fun ColumnScope.SampleRootPage() {
    SampleTitle("Edit request")
    SampleRow("Seasons")
    SampleRow("Destination server")
    SampleRow("Quality profile")
    SampleBottomSpace()
}

@Composable
private fun ColumnScope.SampleDetailPage() {
    SampleTitle("Seasons")
    SampleRow("Season 1")
    SampleRow("Season 2")
    SampleRow("Season 3")
    SampleBottomSpace()
}

@Composable
private fun SampleWashedHeader() {
    Text(
        text = "Washed header",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.fillMaxWidth().bingeSheetWash(Color(SAMPLE_WASH)).padding(dimensionResource(R.dimen.padding_m)),
    )
}

@Composable
private fun SampleTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
    )
}

@Composable
private fun SampleRow(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth().padding(
            horizontal = dimensionResource(R.dimen.padding_m),
            vertical = dimensionResource(R.dimen.padding_s),
        ),
    )
}

/** The space a real sheet keeps below its last row, which the frame's bare chrome does not add. */
@Composable
private fun SampleBottomSpace() {
    Spacer(Modifier.height(dimensionResource(R.dimen.padding_l)))
}

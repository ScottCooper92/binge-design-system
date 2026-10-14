@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeSheetNav
import com.binge.designsystem.component.BingeTaskSheetPanel
import com.binge.designsystem.isExpandedLayout
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeShapes

private const val ROOT = "root"
private const val DETAIL = "detail"

/**
 * A task sheet, as the window shows it: a side sheet on the end edge at the expanded width (840dp and up), the bottom
 * sheet it was below that. Both are the sheet's resting chrome without the modal window around it, which does not capture,
 * so the scrim and the slide are not framed. The list behind is a plain surface where an app's list-detail layout would be.
 */
@Composable
fun BingeAdaptiveTaskSheetSample() {
    ScreenshotTheme {
        SheetBackdrop {
            if (isExpandedLayout()) {
                BingeTaskSheetPanel(pages = listOf(ROOT), onPush = {}, onBack = {}) { SampleRootPage(it) }
            } else {
                BottomSheetChrome { SampleRootPage(null) }
            }
        }
    }
}

/**
 * The task sheet after a row opened a second page. At the expanded width the page stacks inside the side sheet under a
 * Back; below it the page is the second bottom sheet an app opens there, since pages only stack in the side sheet.
 */
@Composable
fun BingeAdaptiveTaskSheetStackedSample() {
    ScreenshotTheme {
        SheetBackdrop {
            if (isExpandedLayout()) {
                BingeTaskSheetPanel(pages = listOf(ROOT, DETAIL), onPush = {}, onBack = {}) { SampleDetailPage() }
            } else {
                BottomSheetChrome { SampleDetailPage() }
            }
        }
    }
}

/** A menu sheet: a short choice, a capped bottom sheet at every width. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeAdaptiveMenuSheetSample() {
    ScreenshotTheme {
        SheetBackdrop(sideSheet = false) {
            BottomSheetChrome {
                SampleRow("Open in browser")
                SampleRow("Copy link")
                SampleRow("Share")
            }
        }
    }
}

@Composable
private fun SheetBackdrop(sideSheet: Boolean = true, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = if (sideSheet && isExpandedLayout()) Alignment.CenterEnd else Alignment.BottomCenter,
    ) { content() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetChrome(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
            .fillMaxWidth()
            .clip(BingeShapes.HeroTop)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(bottom = dimensionResource(R.dimen.padding_l)),
        content = content,
    )
}

@Composable
private fun ColumnScope.SampleRootPage(nav: BingeSheetNav?) {
    SampleTitle("Edit request")
    SampleRow("Seasons")
    SampleRow("Destination server")
    SampleRow("Quality profile")
    // The depth is read so the root says where it sits in a stack, as an app's first page words itself.
    if (nav != null && nav.depth == 1) SampleRow("Opens a page")
}

@Composable
private fun ColumnScope.SampleDetailPage() {
    SampleTitle("Seasons")
    SampleRow("Season 1")
    SampleRow("Season 2")
    SampleRow("Season 3")
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

package com.binge.designsystem.preview

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.binge.designsystem.component.BingeTaskSheetPanel
import com.binge.designsystem.component.ROOT_PAGE
import com.binge.designsystem.component.TaskSheetTopClearance
import com.binge.designsystem.isExpandedLayout
import com.binge.designsystem.theme.BingeShapes

/**
 * A task sheet's two forms, one cell each: the phone, where it is a bottom sheet, and the unfolded foldable at the
 * expanded width, where it is a side sheet. Same specs as [ScreenPreviews]' cells.
 */
@PreviewWrapper(ScreenshotThemeWrapper::class)
@Preview(name = "phone", device = PHONE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
@Preview(name = "foldable", device = FOLDABLE_PORTRAIT_SPEC, uiMode = UI_MODE_NIGHT_YES, locale = SOURCE_LOCALE)
annotation class TaskSheetPreviews

/**
 * A task sheet's resting chrome as the window shows it, without the modal window around it, which does not capture: the
 * side sheet's panel at the expanded width, with the clearance a page keeps there, and a bottom sheet's top below it.
 * A [page] other than the root is drawn as a pushed page, under its Back bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSheetPreviewFrame(page: String = ROOT_PAGE, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize()) {
        if (isExpandedLayout()) {
            // A page past the root draws the side sheet's Back bar over it, as a pushed page does.
            val pages = if (page == ROOT_PAGE) listOf(ROOT_PAGE) else listOf(ROOT_PAGE, page)
            BingeTaskSheetPanel(pages = pages, onPush = {}, onBack = {}, modifier = Modifier.align(Alignment.CenterEnd)) {
                TaskSheetTopClearance(it)
                content()
            }
        } else {
            TaskSheetBottomChrome(content)
        }
    }
}

/**
 * The top of a bottom sheet as the window shows it, anchored to the bottom of its [Box]: the rounded corners, the
 * sheet's width cap and its drag handle. The one imitation of that chrome, shared by [TaskSheetPreviewFrame] and the
 * catalog's menu sample.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BoxScope.TaskSheetBottomChrome(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
            .fillMaxWidth()
            .clip(BingeShapes.HeroTop)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        BottomSheetDefaults.DragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
        content()
    }
}

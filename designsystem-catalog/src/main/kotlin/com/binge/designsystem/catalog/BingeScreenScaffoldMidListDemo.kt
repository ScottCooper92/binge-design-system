@file:OnePerScreen(fullScreen = true)
@file:CatalogGroup("Top app bars")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.BingeScreenScaffold

private const val OPENED_AT_ROW = 5
private const val ROWS = 40

/**
 * A screen opened part way down, as an all-episodes page jumping to a season does: the list starts at row 6 and the
 * collapsing bar arrives collapsed over it. The screen passes its list state as `listState`. Scroll to the top and
 * the bar expands; it is not collapsed again under you. A frame cannot show this, since the collapse runs after the
 * first layout, so the demo runs live and no screenshot renders it.
 */
@Composable
fun BingeScreenScaffoldMidListDemo() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = OPENED_AT_ROW)
        BingeScreenScaffold(
            title = "Opened at row 6",
            onBack = LocalDemoBack.current,
            listState = listState,
        ) { padding -> SampleRows(padding, listState = listState, rows = ROWS) }
    }
}

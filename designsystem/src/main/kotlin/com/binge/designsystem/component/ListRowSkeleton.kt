package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchorIf
import com.binge.designsystem.modifier.skeleton
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

private const val DEFAULT_LIST_ROW_SKELETON_COUNT = 8

/**
 * Loading placeholder for the shared full-width [ListRow] — a shimmer clipped to the row's `large`
 * corner. [height] defaults to a poster-leading row so the loading→content swap doesn't reflow;
 * pass a shorter one for a row shape that resolves shorter, such as an avatar-leading row.
 */
@Composable
fun ListRowSkeleton(modifier: Modifier = Modifier, height: Dp = dimensionResource(R.dimen.list_row_skeleton_height)) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .skeleton(visible = true, shape = BingeShapes.Large),
    )
}

/**
 * A non-scrolling column of [count] [ListRowSkeleton] placeholders laid out with [contentPadding] and
 * the standard row spacing, so it stands in for the loaded [LazyColumn] directly.
 *
 * [height] passes through to each [ListRowSkeleton]; match the resolved row's height or the
 * loading→content swap reflows.
 *
 * [header] is the plate for a leading item the resolved list puts *inside* its own column — the list detail
 * screen's name-and-count block. Without it the first row arrived 128dp below where it was reserved.
 * A subtitle that sits *above* the column instead (the Lists screen's) is the caller's own to place, since
 * this column's content padding would otherwise be applied on the wrong side of it.
 */
@Composable
fun ListRowSkeletonColumn(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    count: Int = DEFAULT_LIST_ROW_SKELETON_COUNT,
    height: Dp = dimensionResource(R.dimen.list_row_skeleton_height),
    header: (@Composable () -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_row_spacing)),
        userScrollEnabled = false,
    ) {
        if (header != null) item { header() }
        items(count) { index ->
            // The first plate carries the anchor the resolved list's first row carries: the column fills the
            // screen, so anchoring it would pass whatever the rows did.
            ListRowSkeleton(
                modifier = Modifier.layoutAnchorIf(index == 0, LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM)),
                height = height,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewListRowSkeletonColumn() {
    BingeExpressiveTheme {
        ListRowSkeletonColumn(contentPadding = PaddingValues(dimensionResource(R.dimen.padding_m)))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewListRowSkeletonColumnCompact() {
    BingeExpressiveTheme {
        ListRowSkeletonColumn(
            contentPadding = PaddingValues(dimensionResource(R.dimen.padding_m)),
            height = dimensionResource(R.dimen.avatar_size_lg),
        )
    }
}

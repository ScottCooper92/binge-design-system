package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeTheme
import com.binge.designsystem.tv.component.TvInfoGroup
import com.binge.designsystem.tv.nav.tvContentGutterStart
import kotlinx.coroutines.launch
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val INFO_COLUMNS = 5

/**
 * The Details tab: the logo, meta and full synopsis, then the [infoGroups] in columns across the width. It is one focus stop with
 * no ring: ↓ scrolls it while there is more below, and ↑ at the top passes on, back to the tabs.
 *
 * @need show a detail page's full synopsis and info fields in one scrolling focus stop
 * @group tv
 * @replaces a hand-built details tab
 */
@Composable
fun TvDetailOverview(
    title: TvDetailTitle,
    infoGroups: List<TvInfoGroup>,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val step = with(LocalDensity.current) { dimensionResource(TvR.dimen.tv_detail_scroll_step).toPx() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionDown -> scroll.canScrollForward.also { if (it) scope.launch { scroll.animateScrollBy(step) } }
                    Key.DirectionUp -> scroll.canScrollBackward.also { if (it) scope.launch { scroll.animateScrollBy(-step) } }
                    else -> false
                }
            }.focusable()
            .verticalScroll(scroll)
            .padding(start = tvContentGutterStart(), end = dimensionResource(TvR.dimen.tv_overscan_horizontal))
            .padding(bottom = dimensionResource(TvR.dimen.tv_overscan_vertical)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
    ) {
        TvDetailLogo(title)
        TvDetailMeta(title, Modifier.width(dimensionResource(TvR.dimen.tv_detail_copy_width)))
        Text(
            text = title.overview,
            style = MaterialTheme.typography.bodyLarge,
            color = BingeTheme.colors.onScrim,
            modifier = Modifier.width(dimensionResource(TvR.dimen.tv_overview_text_max_width)),
        )
        Spacer(Modifier.height(dimensionResource(DesR.dimen.padding_s)))
        TvDetailInfoColumns(infoGroups)
    }
}

/** The fields across the full width, a fixed number to a line, so a long value wraps in its own column. */
@Composable
private fun TvDetailInfoColumns(infoGroups: List<TvInfoGroup>) {
    infoGroups.filter { it.value.isNotBlank() }.chunked(INFO_COLUMNS).forEach { line ->
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_detail_column_gap))) {
            line.forEach { group ->
                Column(Modifier.weight(1f)) {
                    Text(
                        text = group.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(text = group.value, style = MaterialTheme.typography.bodyMedium, color = BingeTheme.colors.onScrim)
                }
            }
            repeat(INFO_COLUMNS - line.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

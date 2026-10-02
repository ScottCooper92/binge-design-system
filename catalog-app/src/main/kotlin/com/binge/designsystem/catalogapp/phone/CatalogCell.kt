package com.binge.designsystem.catalogapp.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import com.binge.designsystem.catalogapp.R
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.catalogapp.registry.CatalogKind
import kotlin.math.roundToInt

private const val CELL_ASPECT_RATIO = 1f
private const val NAME_MAX_LINES = 2

/**
 * One entry as a card: a visual filling the top and the name at the bottom start. A sample shows a
 * live, scaled-down render of itself; a demo shows an icon and a Demo badge, since a still render of
 * "Open sheet" says nothing about what the demo does.
 */
@Composable
fun CatalogCell(
    entry: CatalogEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(onClick = onClick, modifier = modifier.fillMaxWidth().aspectRatio(CELL_ASPECT_RATIO)) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (entry.kind) {
                CatalogKind.Sample -> ScaledPreview(content = entry.content, modifier = Modifier.fillMaxSize())
                CatalogKind.Demo -> DemoVisual(entry)
            }
            // A preview is a picture, not a control: this swallows taps meant for a button inside it
            // and opens the entry instead. Neither it nor the preview is a focus stop or a semantics node.
            Box(
                Modifier
                    .matchParentSize()
                    .clearAndSetSemantics {}
                    .focusProperties { canFocus = false }
                    .clickable(onClick = onClick),
            )
        }
        Text(
            text = entry.name,
            style = MaterialTheme.typography.titleSmall,
            minLines = NAME_MAX_LINES,
            maxLines = NAME_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(dimensionResource(R.dimen.catalog_padding_small)),
        )
    }
}

@Composable
private fun DemoVisual(entry: CatalogEntry) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = entry.demoIcon(),
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.catalog_demo_icon_size)),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.align(Alignment.TopEnd).padding(dimensionResource(R.dimen.catalog_padding_small)),
        ) {
            Text(
                text = stringResource(R.string.catalog_demo_badge),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.catalog_padding_small)),
            )
        }
    }
}

/**
 * Renders [content] at a fixed design width and scales the whole render to fit this layout's width, so
 * a sample that assumes a phone screen reads as a thumbnail rather than being clipped. The scale origin
 * is the absolute top-left, whatever the layout direction, so the thumbnail is always anchored the same.
 */
@Composable
private fun ScaledPreview(content: @Composable () -> Unit, modifier: Modifier = Modifier) {
    val canvasWidthPx = with(LocalDensity.current) { dimensionResource(R.dimen.catalog_preview_canvas_width).roundToPx() }
    Layout(
        content = {
            // Semantics and focus are separate trees: clearing one leaves the other, so both are cleared
            // and a control inside a sample is neither announced nor a Tab or D-pad stop.
            Box(Modifier.clearAndSetSemantics {}.focusProperties { canFocus = false }, contentAlignment = Alignment.Center) { content() }
        },
        modifier = modifier.clipToBounds().background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) { measurables, constraints ->
        val scale = constraints.maxWidth.toFloat() / canvasWidthPx
        val canvasHeightPx = (constraints.maxHeight / scale).roundToInt()
        val canvas = measurables.single().measure(Constraints.fixed(canvasWidthPx, canvasHeightPx))
        layout(constraints.maxWidth, constraints.maxHeight) {
            canvas.placeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

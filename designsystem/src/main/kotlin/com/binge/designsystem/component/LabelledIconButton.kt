package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.DISABLED_ALPHA
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

/**
 * A round tonal icon button with a short [label] under it, for a row of secondary actions beside a primary one (a detail page's
 * "Trailer" and "Share" next to "Request"). The circle is [ExpressiveIconButton]'s tonal button: the same size and container.
 *
 * The button is a column whose width is a floor, not a size: at least a little wider than the circle, so a short label has room and the circle
 * stays on its middle, and growing with the label up to a cap, so a long label or a large font reads in full instead of being cut to "Trail…".
 * The cap is [R.dimen.labelled_icon_button_max_width] times the font scale, so a word that fits whole at the normal size still fits whole at
 * 2x, and it is bounded by the width the parent gives. Past the cap a label of several words wraps, centred, to two lines and ellipsizes after
 * that, so no one button pushes a row of them off the screen. A single word does not wrap, since that would split it: it stays on one line and
 * ellipsizes whole when it is wider than the cap or than the parent leaves. A label of several words wraps only if each word fits the cap. A short
 * label at a normal font keeps the floor's width. The button is a plain column, so it can sit in a row sized with `IntrinsicSize`, and a caller
 * that stretches it gets the circle and label centred. The caller owns the words, and the spacing between buttons.
 *
 * The whole column is one click target: a tap on the label presses the button too. It is one accessibility item with the button role,
 * announcing [contentDescription], or the [label] when that is null, so the label is not a focus stop of its own. Pressing scales the
 * column as the carousel tiles do and ripples the circle. A button that is not [enabled] takes no taps, and its circle and label dim by
 * [DISABLED_ALPHA].
 *
 * @need offer a secondary action as a round icon with a caption
 * @group inputs
 * @replaces a hand-built Column of an icon button and a label
 */
@Composable
fun LabelledIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val announced = contentDescription ?: label
    val labelStyle = MaterialTheme.typography.labelMedium
    // The cap grows with the font, so the width that held a word at the normal size still holds it at a large one: a cap in dp would
    // stay put while the word widened, and one word would be cut at 2x that fits at 1x. The parent's width bounds it on its own.
    val cap = dimensionResource(R.dimen.labelled_icon_button_max_width) * LocalDensity.current.fontScale
    // Only a label of several words ever wraps, and only when every word fits the cap, so no word is split. One word stays on one line, whole,
    // and takes the ellipsis if it is too wide. Nothing here reads the parent's width, so the button can still be measured for intrinsics.
    val wraps = label.trim().contains(' ') && labelWordsFit(label, labelStyle, cap)
    Column(
        modifier = modifier
            .widthIn(min = dimensionResource(R.dimen.labelled_icon_button_width), max = cap)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .pressScale(interactionSource)
            // The indication is drawn on the circle below, from this same source, so the ripple is the circle's and not the column's.
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ).clearAndSetSemantics {
                this.contentDescription = announced
                role = Role.Button
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.button_tonal_size))
                .clip(BingeShapes.Pill)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .indication(interactionSource, ripple()),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_button_glyph_size)),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = label,
            style = labelStyle,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = if (wraps) 2 else 1,
            softWrap = wraps,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_xs)),
        )
    }
}

/** Whether every word of [label] fits on a line [maxWidth] wide at [style] and the current font scale, so wrapping never has to split a word. */
@Composable
private fun labelWordsFit(
    label: String,
    style: TextStyle,
    maxWidth: Dp,
): Boolean {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(label, style, density, maxWidth) {
        val maxWidthPx = with(density) { maxWidth.roundToPx() }
        label
            .split(' ')
            .filter { it.isNotEmpty() }
            .all { word -> textMeasurer.measure(word, style).size.width <= maxWidthPx }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLabelledIconButton() {
    BingeExpressiveTheme {
        LabelledIconButton(icon = Icons.Filled.Share, label = "Share", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLabelledIconButtonDisabled() {
    BingeExpressiveTheme {
        LabelledIconButton(icon = Icons.Filled.Share, label = "Share", onClick = {}, enabled = false)
    }
}

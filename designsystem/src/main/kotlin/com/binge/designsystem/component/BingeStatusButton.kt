package com.binge.designsystem.component

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.binge.designsystem.theme.BingeSentiment
import com.binge.designsystem.theme.accent
import com.binge.designsystem.theme.fill
import com.binge.designsystem.theme.tonalContainer

/** How strongly a [BingeStatusButton] stands out: [Filled] for an open action, [Tonal] for a state it reports. */
enum class BingeStatusButtonEmphasis {
    /** The primary fill, for a state the user can still act on, such as a request not yet made. */
    Filled,

    /** The sentiment's tonal wash with its accent as the label, for a state that is only reported. */
    Tonal,
}

/**
 * A request's state as the one button that opens it: filled while there is still an action to take, tonal once it only
 * reports where things stand. It carries the status dot [StatusChip] draws before its label, and it is tappable in every
 * state, because tapping it opens whatever shows the detail.
 *
 * [label] is already translated; the consumer owns the words and maps its own states onto [sentiment], one line per
 * state. [BingeSentiment.Neutral] draws no dot, for a state with no sentiment, such as an action not yet taken. A long
 * label wraps to a second line rather than ending in an ellipsis, since the label is the state. A screen reader reads
 * the label alone: the dot is decoration and the colour never carries the state by itself.
 *
 * @need show a request's state as the one button that opens it
 * @group inputs
 * @sample BingeStatusButtonSample
 * @replaces a hand-built filled or tonal Button with a status dot
 */
@Composable
fun BingeStatusButton(
    label: String,
    onClick: () -> Unit,
    sentiment: BingeSentiment,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emphasis: BingeStatusButtonEmphasis = BingeStatusButtonEmphasis.Tonal,
) {
    val accent = sentiment.accent()
    val colors = when (emphasis) {
        BingeStatusButtonEmphasis.Filled -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryFixedDim,
            contentColor = MaterialTheme.colorScheme.onPrimaryFixed,
        )
        BingeStatusButtonEmphasis.Tonal -> ButtonDefaults.buttonColors(
            containerColor = accent.tonalContainer(),
            contentColor = accent,
        )
    }
    val fill = sentiment.fill()
    BingeFilledButton(
        label = label,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = icon,
        colors = colors,
        leading = if (sentiment == BingeSentiment.Neutral) null else ({ StatusDot(fill) }),
    )
}

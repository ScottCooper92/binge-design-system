package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

/**
 * The letters of a long alphabetical list down its edge: a tap or a drag over a letter calls [onLetter] with it, so the
 * list can jump there. [current] is the letter of the section at the top of the list, lit so the rail follows the
 * scroll; while a finger is on the rail, the letter under it is lit instead.
 *
 * When the rail is too short for every letter, it shows every second letter, or third, with a dot between them for the
 * ones left out, as a contact list does. A tap or a drag then picks one of the letters drawn: the nearest to the finger,
 * so a touch on a dot picks its neighbour. [current] is lit only when it is drawn.
 */
@Composable
fun BingeLetterRail(
    letters: List<Char>,
    onLetter: (Char) -> Unit,
    modifier: Modifier = Modifier,
    current: Char? = null,
) {
    if (letters.isEmpty()) return
    val letterPx = with(LocalDensity.current) { dimensionResource(R.dimen.letter_rail_letter).roundToPx() }
    val dotPx = with(LocalDensity.current) { dimensionResource(R.dimen.letter_rail_dot).roundToPx() }
    // The plan comes from the constraints, not a measured size, so the first frame already draws the letters that fit.
    BoxWithConstraints(modifier.fillMaxHeight().width(dimensionResource(R.dimen.letter_rail_width))) {
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else 0
        val plan = remember(letters, height, letterPx, dotPx) { letterRailPlan(letters, height, letterPx, dotPx) }
        LetterRailColumn(letters, plan, height, letterPx, dotPx, onLetter, current)
    }
}

@Composable
private fun LetterRailColumn(
    letters: List<Char>,
    plan: LetterRailPlan,
    height: Int,
    letterPx: Int,
    dotPx: Int,
    onLetter: (Char) -> Unit,
    current: Char?,
) {
    var touched by remember { mutableStateOf<Char?>(null) }
    // Set when a drag takes the gesture over from the press, so the cancelled press leaves the drag's dedupe alone.
    var dragging by remember { mutableStateOf(false) }
    val planNow by rememberUpdatedState(plan)
    val heightNow by rememberUpdatedState(height)

    fun pick(y: Float) {
        if (heightNow == 0) return
        val letter = planNow.letterAt(y, heightNow, letterPx, dotPx)
        if (letter != touched) {
            touched = letter
            onLetter(letter)
        }
    }

    suspend fun PressGestureScope.pressLetter(y: Float) {
        pick(y)
        tryAwaitRelease()
        if (!dragging) touched = null
    }

    fun endDrag() {
        dragging = false
        touched = null
    }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(letters) { detectTapGestures(onPress = { pressLetter(it.y) }) }
                .pointerInput(letters) {
                    detectVerticalDragGestures(
                        onDragStart = { dragging = true },
                        onDragEnd = ::endDrag,
                        onDragCancel = ::endDrag,
                    ) { change, _ ->
                        pick(change.position.y)
                    }
                },
        verticalArrangement = if (plan.dotted) Arrangement.Center else Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        plan.shown.forEachIndexed { index, letter ->
            if (plan.dotted && index > 0) {
                Box(
                    Modifier.size(dimensionResource(R.dimen.letter_rail_letter), dimensionResource(R.dimen.letter_rail_dot)),
                    Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(dimensionResource(R.dimen.letter_rail_dot_mark))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape),
                    )
                }
            }
            val lit = letter == (touched ?: current)
            Box(
                modifier =
                    Modifier
                        .size(dimensionResource(R.dimen.letter_rail_letter))
                        .background(if (lit) MaterialTheme.colorScheme.primary else Color.Transparent, BingeShapes.ElementExtraSmall),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    letter.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (lit) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Which letters a rail draws at its height, and whether it puts a dot between them. */
internal class LetterRailPlan(
    val shown: List<Char>,
    val dotted: Boolean,
)

/**
 * Every letter when they fit [heightPx] at [letterPx] each. Otherwise every second letter, then every third and so on,
 * until the letters and the [dotPx] dots between them fit, or one letter is left.
 */
internal fun letterRailPlan(
    letters: List<Char>,
    heightPx: Int,
    letterPx: Int,
    dotPx: Int,
): LetterRailPlan {
    if (heightPx <= 0 || letters.size * letterPx <= heightPx) return LetterRailPlan(letters, dotted = false)
    var stride = 2
    while (true) {
        val shown = letters.filterIndexed { index, _ -> index % stride == 0 }
        val needed = shown.size * letterPx + (shown.size - 1) * dotPx
        if (needed <= heightPx || shown.size <= 1) return LetterRailPlan(shown, dotted = true)
        stride++
    }
}

/**
 * The letter a touch at [y] lands on, among the letters drawn. All of them, spread evenly, when they fit. When dotted,
 * the rows are packed and centred, so the nearest drawn letter to the touch wins.
 */
internal fun LetterRailPlan.letterAt(
    y: Float,
    heightPx: Int,
    letterPx: Int,
    dotPx: Int,
): Char {
    if (!dotted) return shown[(y / heightPx * shown.size).toInt().coerceIn(0, shown.lastIndex)]
    val pitch = letterPx + dotPx
    val top = (heightPx - (shown.size * letterPx + (shown.size - 1) * dotPx)) / 2f
    val index = Math.round((y - top - letterPx / 2f) / pitch)
    return shown[index.coerceIn(0, shown.lastIndex)]
}

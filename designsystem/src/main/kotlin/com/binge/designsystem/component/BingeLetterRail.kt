package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

/**
 * The letters of a long alphabetical list down its edge: a tap or a drag over a letter calls [onLetter] with it, so the
 * list can jump there. [current] is the letter of the section at the top of the list, lit so the rail follows the
 * scroll; while a finger is on the rail, the letter under it is lit instead.
 */
@Composable
fun BingeLetterRail(
    letters: List<Char>,
    onLetter: (Char) -> Unit,
    modifier: Modifier = Modifier,
    current: Char? = null,
) {
    if (letters.isEmpty()) return
    var height by remember { mutableIntStateOf(0) }
    var touched by remember { mutableStateOf<Char?>(null) }

    fun pick(y: Float) {
        if (height == 0) return
        val letter = letters[(y / height * letters.size).toInt().coerceIn(0, letters.lastIndex)]
        if (letter != touched) {
            touched = letter
            onLetter(letter)
        }
    }

    suspend fun PressGestureScope.pressLetter(y: Float) {
        pick(y)
        tryAwaitRelease()
        touched = null
    }
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .width(dimensionResource(R.dimen.letter_rail_width))
                .onSizeChanged { height = it.height }
                .pointerInput(letters) { detectTapGestures(onPress = { pressLetter(it.y) }) }
                .pointerInput(letters) {
                    detectVerticalDragGestures(onDragEnd = { touched = null }, onDragCancel = { touched = null }) { change, _ ->
                        pick(change.position.y)
                    }
                },
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        letters.forEach { letter ->
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

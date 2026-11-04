package com.binge.designsystem.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.onLongClick as semanticsLongClick

/** Adds a title's optional long press without inventing a single-tap action or replacing its readable text. */
internal fun Modifier.titleLongClick(onLongClick: (() -> Unit)?, label: String?): Modifier =
    if (onLongClick == null) {
        this
    } else {
        pointerInput(onLongClick) { detectTapGestures(onLongPress = { onLongClick() }) }
            .semantics {
                semanticsLongClick(label = label) {
                    onLongClick()
                    true
                }
            }
    }

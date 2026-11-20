package com.binge.designsystem.tv.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * ←/→ steps a focused expanding row along its titles: [onStep] gets +1 or -1. → is always taken, so at a short row's end it stops
 * there instead of moving focus off the row; ← at the first title is handed on.
 */
internal fun Modifier.tvExpandingStepKeys(
    enabled: Boolean,
    canStepRight: Boolean,
    canStepLeft: Boolean,
    onStep: (Int) -> Unit,
): Modifier =
    onPreviewKeyEvent { event ->
        if (!enabled || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        when {
            event.key == Key.DirectionRight -> {
                if (canStepRight) onStep(1)
                true
            }
            event.key == Key.DirectionLeft && canStepLeft -> {
                onStep(-1)
                true
            }
            else -> false
        }
    }

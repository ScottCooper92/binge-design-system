package com.binge.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

/**
 * Animates the height of what it wraps when its content changes the height, and only then. A node that fills all the height it is
 * offered, as a sheet does when a list inside it is long, is not changing height for its content: the space it is offered moves with
 * the sheet while the sheet is dragged, and a spring chasing that moves the sheet's own anchors, which pulls the sheet back under
 * the finger. So while it fills the space on two measures running, its height follows the space at once. A change from not filling
 * to filling, a list that has loaded and grown to the edge, still animates.
 */
@Composable
internal fun Modifier.animateSheetHeight(spec: FiniteAnimationSpec<Int>): Modifier {
    val scope = rememberCoroutineScope()
    val height = remember { Animatable(0, Int.VectorConverter, Spring.DefaultDisplacementThreshold.toInt()) }
    val last = remember { LastMeasure() }
    return layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val target = placeable.height
        val filling = target >= constraints.maxHeight
        val shown =
            if (!last.measured || (filling && last.filling)) {
                scope.launch(start = CoroutineStart.UNDISPATCHED) { height.snapTo(target) }
                target
            } else {
                if (target != height.targetValue) scope.launch { height.animateTo(target, spec) }
                height.value.coerceIn(0, constraints.maxHeight)
            }
        last.measured = true
        last.filling = filling
        layout(placeable.width, shown) { placeable.place(0, 0) }
    }
}

private class LastMeasure {
    var measured = false
    var filling = false
}

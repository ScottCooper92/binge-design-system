package com.binge.designsystem.component

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.modifiers.TextAutoSizeLayoutScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * A title size that prefers one line over two. It steps down from [max] by [step] until the text
 * lays out on a single line, and stops at [min]. If the text does not fit on one line even at [min],
 * it answers [max], and the `Text`'s own `maxLines = 2` wraps it at full size.
 *
 * A title a size or two smaller reads better than one that breaks after its first word. A title that
 * needs two lines anyway reads better at full size than shrunk and still wrapped, which is where
 * `TextAutoSize.StepBased` would leave it.
 *
 * All three sizes are in `sp`.
 */
data class OneLineOrWrapAutoSize(
    val max: TextUnit,
    val min: TextUnit,
    val step: TextUnit,
) : TextAutoSize {
    init {
        require(max.isSp && min.isSp && step.isSp) { "Sizes must be in sp" }
        require(step.value > 0f) { "step must be positive" }
        require(min.value <= max.value) { "min must not exceed max" }
    }

    override fun TextAutoSizeLayoutScope.getFontSize(constraints: Constraints, text: AnnotatedString): TextUnit =
        oneLineOrWrapSize(max = max.value, min = min.value, step = step.value) { size ->
            val layout = performLayout(constraints, text, size.sp)
            layout.lineCount == 1 && !layout.hasVisualOverflow
        }.sp
}

/** The step every hero title shrinks by on its way to one line. */
internal val HeroTitleSizeStep = 2.sp

/**
 * The size [OneLineOrWrapAutoSize] picks, in `sp`: the largest of [max], `max - step`, … down to [min]
 * for which [fitsOnOneLine] holds, or [max] when none does.
 */
internal fun oneLineOrWrapSize(
    max: Float,
    min: Float,
    step: Float,
    fitsOnOneLine: (Float) -> Boolean,
): Float {
    var size = max
    while (size >= min) {
        if (fitsOnOneLine(size)) return size
        size -= step
    }
    return max
}

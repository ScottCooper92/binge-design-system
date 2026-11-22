package com.binge.designsystem.testing

import android.graphics.Rect
import androidx.window.layout.FoldingFeature
import androidx.window.testing.layout.FoldingFeature as posedFold

/**
 * A fold lying across the middle of a 1600 by 2000 window, half open, as a phone held like a laptop has: a 40px crease centred
 * 1000px down. The numbers are the same in every test that poses one, so a frame and a layout assertion agree on where it is.
 */
fun tabletopFold(): FoldingFeature =
    posedFold(
        windowBounds = Rect(0, 0, TABLETOP_WINDOW_WIDTH_PX, TABLETOP_WINDOW_HEIGHT_PX),
        center = TABLETOP_CREASE_CENTRE_PX,
        size = TABLETOP_CREASE_SIZE_PX,
        state = FoldingFeature.State.HALF_OPENED,
        orientation = FoldingFeature.Orientation.HORIZONTAL,
    )

const val TABLETOP_WINDOW_WIDTH_PX = 1600
const val TABLETOP_WINDOW_HEIGHT_PX = 2000
const val TABLETOP_CREASE_CENTRE_PX = 1000
const val TABLETOP_CREASE_SIZE_PX = 40

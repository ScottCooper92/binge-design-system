package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

/**
 * A text style's line height in dp — the height of the bar a skeleton puts where one line of it will land.
 *
 * Skeleton text bars take their height from the type scale rather than from a dimen because that is what
 * they are standing in for: pinning them to a fixed dp re-opens the gap the first time the scale, or the
 * user's font size, moves. This is the same category as the `BingeShapes` tokens' exemption from the
 * dimens rule — a value derived from a token, not a magic number.
 */
@Composable
fun lineHeightOf(style: TextStyle): Dp = with(LocalDensity.current) { style.lineHeight.toDp() }

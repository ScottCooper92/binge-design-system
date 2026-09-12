package com.binge.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.binge.designsystem.R

private const val SIDE_SHEET_SCRIM_ALPHA = 0.5f
private const val SIDE_SHEET_ANIMATION_MS = 250

/**
 * A modal side sheet: the tablet/foldable analogue of [BingeBottomSheet], anchoring a full-height
 * panel to the end edge instead of the bottom. Dismisses on scrim tap and back press.
 *
 * Built on [Dialog] (`usePlatformDefaultWidth = false`) so it owns the whole window for the scrim
 * and gets back-press handling for free. The slide is driven by an [AnimatedVisibility] toggled on
 * first composition, so opening animates in rather than snapping.
 */
@Composable
fun BingeModalSideSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            ),
    ) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(SIDE_SHEET_ANIMATION_MS)),
                exit = fadeOut(tween(SIDE_SHEET_ANIMATION_MS)),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = SIDE_SHEET_SCRIM_ALPHA))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismissRequest,
                            ),
                )
            }

            AnimatedVisibility(
                visible = visible,
                modifier = Modifier.align(Alignment.CenterEnd),
                enter = slideInHorizontally(tween(SIDE_SHEET_ANIMATION_MS)) { it },
                exit = slideOutHorizontally(tween(SIDE_SHEET_ANIMATION_MS)) { it },
            ) {
                Column(
                    modifier =
                        modifier
                            .fillMaxHeight()
                            .width(dimensionResource(R.dimen.side_sheet_width))
                            .clip(
                                RoundedCornerShape(
                                    topStart = dimensionResource(R.dimen.side_sheet_corner),
                                    bottomStart = dimensionResource(R.dimen.side_sheet_corner),
                                ),
                            ).background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            // Swallow taps on the panel so they don't fall through to the scrim.
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            ),
                    content = content,
                )
            }
        }
    }
}

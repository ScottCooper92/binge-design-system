package com.binge.designsystem.tv.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.R as DesR

/**
 * A failure as a [TvMessagePlate]: the [kind]'s glyph and copy, and Try again when [onRetry] is set.
 *
 * A failure the caller cannot retry renders no button, because a dead button is worse than none on a remote.
 * [retryInitiallyFocused] and [retryFocusRequester] take focus as parameters, so the focused state can be
 * screenshotted. [title] and [message] override the kind's copy, for a failure only the caller can name (a missing
 * API key is not "Not allowed"), and [extraActions] follow Try again in the same row.
 */
@Composable
fun TvErrorPlate(
    kind: ErrorKind,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
    message: String? = null,
    title: String? = null,
    alignment: Alignment = Alignment.TopStart,
    retryFocusRequester: FocusRequester? = null,
    retryInitiallyFocused: Boolean = false,
    extraActions: @Composable (() -> Unit)? = null,
) {
    TvMessagePlate(
        body = message ?: stringResource(kind.messageRes),
        headline = title ?: stringResource(kind.titleRes),
        icon = kind.icon,
        modifier = modifier,
        alignment = alignment,
        actions =
            if (onRetry == null && extraActions == null) {
                null
            } else {
                {
                    onRetry?.let { retry ->
                        TvButton(
                            label = stringResource(DesR.string.error_kind_retry),
                            onClick = retry,
                            style = TvButtonStyle.Primary,
                            initiallyFocused = retryInitiallyFocused,
                            modifier = retryFocusRequester?.let { Modifier.focusRequester(it) } ?: Modifier,
                        )
                    }
                    extraActions?.invoke()
                }
            },
    )
}

package com.binge.designsystem.tv.template

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.template.ScreenAction
import com.binge.designsystem.R as DesR

/**
 * A failure as a whole page: [TvErrorPlate][com.binge.designsystem.tv.component.TvErrorPlate]'s page-level twin. The
 * [kind]'s glyph and copy, and Try again when [onRetry] is set, as a [TvMessagePage] announced as a whole.
 *
 * Use the plate where the failure sits inside a board and the page is still there around it; use this one where the
 * failure is the page, such as a detail screen that could not load. A failure the caller cannot retry renders no
 * button, because a dead button is worse than none on a remote. [title] and [message] override the kind's copy, for a
 * failure only the caller can name (a missing API key is not "Not allowed"). [secondary] sits beside Try again, and is
 * shown only with it, as a [TvMessagePage] shows its second action.
 */
@Composable
fun TvErrorPage(
    kind: ErrorKind,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
    hosting: TvPageHosting = currentTvPageHosting(),
    title: String? = null,
    message: String? = null,
    secondary: ScreenAction? = null,
) {
    TvMessagePage(
        body = message ?: stringResource(kind.messageRes),
        modifier = modifier,
        hosting = hosting,
        headline = title ?: stringResource(kind.titleRes),
        icon = kind.icon,
        primary = onRetry?.let { ScreenAction(stringResource(DesR.string.error_kind_retry), it) },
        secondary = secondary,
        announce = true,
    )
}

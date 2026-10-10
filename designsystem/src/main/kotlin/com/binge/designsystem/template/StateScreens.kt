package com.binge.designsystem.template

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.R

/**
 * A failure that replaced the screen's content: the [kind]'s glyph and copy, and Try again when [onRetry] is set.
 *
 * [title] and [message] override the kind's copy, for a failure the caller can say more precisely, such as
 * a message the server sent. The whole message is announced, not just the headline, because it replaced what
 * the user was reading.
 */
@Composable
fun ErrorScreen(
    kind: ErrorKind,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
) {
    MessageScreen(
        body = message ?: stringResource(kind.messageRes),
        headline = title ?: stringResource(kind.titleRes),
        icon = kind.icon,
        primary = onRetry?.let { ScreenAction(stringResource(R.string.error_kind_retry), it, Icons.Filled.Refresh) },
        announce = true,
        modifier = modifier,
    )
}

/** Nothing here yet, said once under its [icon], with an [action] where there is something to do about it. */
@Composable
fun EmptyScreen(
    message: String,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.empty_screen_title),
    icon: ImageVector = Icons.Filled.SearchOff,
    action: ScreenAction? = null,
) {
    MessageScreen(body = message, headline = title, icon = icon, primary = action, modifier = modifier)
}

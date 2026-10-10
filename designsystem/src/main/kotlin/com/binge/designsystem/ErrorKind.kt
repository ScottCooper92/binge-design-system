package com.binge.designsystem

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * What kind of failure a screen is showing, with the glyph and the copy that say it.
 *
 * Each app maps its own error type onto one of these, so a failure reads the same in both. The glyph and
 * the copy sit in one place, so they cannot drift apart. [Generic] is for a cause that is genuinely unknown,
 * so its glyph claims nothing specific. A failure only one app has keeps its own copy, passed as an override.
 */
enum class ErrorKind(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
) {
    Network(Icons.Filled.WifiOff, R.string.error_kind_network_title, R.string.error_kind_network_message),
    Server(Icons.Filled.CloudOff, R.string.error_kind_server_title, R.string.error_kind_server_message),
    NotFound(Icons.Filled.SearchOff, R.string.error_kind_not_found_title, R.string.error_kind_not_found_message),
    Auth(Icons.Filled.Lock, R.string.error_kind_auth_title, R.string.error_kind_auth_message),
    RateLimited(Icons.Filled.HourglassEmpty, R.string.error_kind_rate_limited_title, R.string.error_kind_rate_limited_message),
    Forbidden(Icons.Filled.Block, R.string.error_kind_forbidden_title, R.string.error_kind_forbidden_message),
    Generic(Icons.Filled.ErrorOutline, R.string.error_kind_generic_title, R.string.error_kind_generic_message),
}

package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

/** One row of a [SettingsGroup]: an icon in a tinted box, a label with an optional detail line, and a trailing slot. */
@Immutable
data class SettingsRow(
    val icon: ImageVector,
    /**
     * Rendered in place of [icon] when set, for a glyph the caller resolves itself — one loaded from
     * another package's resources, say — so the row shows it without this module knowing its source.
     */
    val iconPainter: (@Composable () -> Painter)? = null,
    /** Tint for the icon box wash and glyph; defaults to the accent on a neutral box. */
    val iconTint: Color? = null,
    val label: String,
    val detail: String? = null,
    /** Tint for [detail]; defaults to the muted caption colour. Use the error colour for problems. */
    val detailColor: Color? = null,
    /** A count badge shown before the trailing chevron; omitted when null or not positive. */
    val badgeCount: Int? = null,
    /** With [badgeCount], renders the count as a tonal pill in this colour instead of the default badge. */
    val badgeTint: Color? = null,
    val clickable: Boolean = true,
    val trailingContent: (@Composable () -> Unit)? = null,
    val onClick: () -> Unit = {},
    val onLongClick: (() -> Unit)? = null,
)

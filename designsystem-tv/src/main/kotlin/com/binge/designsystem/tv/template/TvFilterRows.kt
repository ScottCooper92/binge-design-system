package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** The divider's alpha: faint, so the rule parts the pinned rows from the values without competing with either. */
internal const val TV_FILTER_DIVIDER_ALPHA = 0.2f

/** A row in the pane: icon or logo, label and an optional second line, and a mark at the end; focus fills it white. */
@Composable
internal fun TvFilterRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    icon: ImageVector? = null,
    logoUrl: String? = null,
    trailing: ImageVector? = null,
    reserveTrailing: Boolean = false,
    excluded: Boolean = false,
    onFocused: () -> Unit = {},
    trailingContent: (@Composable (focused: Boolean) -> Unit)? = null,
    leadingText: String? = null,
    code: String? = null,
    initiallyFocused: Boolean = false,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    val scheme = MaterialTheme.colorScheme
    val content = if (focused) scheme.surface else scheme.onSurface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(if (focused) scheme.onSurface else Color.Transparent)
            .tvClickable(
                onFocusChanged = {
                    focused = it
                    if (it) onFocused()
                },
                onClick = onClick,
            ).padding(horizontal = dimensionResource(DesR.dimen.padding_m), vertical = dimensionResource(DesR.dimen.padding_s)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
    ) {
        TvFilterRowLeading(leadingText = leadingText, logoUrl = logoUrl, icon = icon, tint = content)
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = if (excluded && !focused) scheme.error else content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            detail?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (focused) scheme.surface else scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        code?.let {
            Text(
                it.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = if (focused) scheme.surface else scheme.onSurfaceVariant,
            )
        }
        trailingContent?.invoke(focused)
        // The tick's slot is kept whether or not it is ticked, so ticking a value never changes the row's height.
        if (trailing != null || reserveTrailing) TvFilterRowMark(trailing, content)
    }
}

/** What leads a row: a flag or other text, a logo, or an icon, in that order of preference. */
@Composable
private fun TvFilterRowLeading(
    leadingText: String?,
    logoUrl: String?,
    icon: ImageVector?,
    tint: Color,
) {
    when {
        leadingText != null -> Text(leadingText, style = MaterialTheme.typography.titleMedium)
        logoUrl != null -> SubcomposeAsyncImage(
            model = logoUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)).clip(MaterialTheme.shapes.extraSmall),
        )
        icon != null -> Icon(
            icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)),
        )
    }
}

/** The row's tick or cross, in a slot of the icon's size. */
@Composable
private fun TvFilterRowMark(mark: ImageVector?, tint: Color) {
    Box(Modifier.size(dimensionResource(TvR.dimen.tv_button_icon))) {
        mark?.let { Icon(it, contentDescription = null, tint = tint, modifier = Modifier.fillMaxSize()) }
    }
}

/** A pinned row: an optional icon, its text, and an optional plain-text [action] at its end; one press, white when focused. */
@Composable
internal fun TvFilterPinnedRow(
    text: String,
    focusable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    action: String? = null,
    initiallyFocused: Boolean = false,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    val scheme = MaterialTheme.colorScheme
    val content = if (focused) scheme.surface else scheme.onSurface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(if (focused) scheme.onSurface else Color.Transparent)
            .then(
                if (focusable) Modifier.tvClickable(role = Role.Button, onFocusChanged = { focused = it }, onClick = onClick) else Modifier,
            ).padding(horizontal = dimensionResource(DesR.dimen.padding_m), vertical = dimensionResource(DesR.dimen.padding_s)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
    ) {
        icon?.let {
            Icon(
                it,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)),
            )
        }
        Text(text = text, style = MaterialTheme.typography.titleMedium, color = content, modifier = Modifier.weight(1f), maxLines = 1)
        action?.let { Text(text = it, style = MaterialTheme.typography.titleSmall, color = content) }
    }
}

/** A line of text and, when [canClear], Clear at its end, the whole row one focusable press. */
@Composable
internal fun TvFilterClearRow(
    text: String,
    canClear: Boolean,
    onClear: () -> Unit,
    clearLabel: String,
    modifier: Modifier = Modifier,
    initiallyFocused: Boolean = false,
) {
    TvFilterPinnedRow(
        text = text,
        action = if (canClear) clearLabel else null,
        focusable = canClear,
        onClick = onClear,
        modifier = modifier,
        initiallyFocused = initiallyFocused,
    )
}

/** The faint rule under a sub-view's pinned rows. */
@Composable
internal fun TvFilterDivider() {
    Box(
        Modifier
            .padding(top = dimensionResource(DesR.dimen.padding_xs))
            .fillMaxWidth()
            .height(dimensionResource(DesR.dimen.hairline_thickness))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = TV_FILTER_DIVIDER_ALPHA)),
    )
}

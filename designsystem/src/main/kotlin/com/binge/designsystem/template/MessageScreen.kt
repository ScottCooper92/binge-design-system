package com.binge.designsystem.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.BingeLoadingIndicator
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.navOverlayPadding
import com.binge.designsystem.theme.BingeShapes

/** One way out of a [MessageScreen]: what the button says, its icon, and what pressing it does. */
@Immutable
data class ScreenAction(
    val label: String,
    val onClick: () -> Unit,
    val leadingIcon: ImageVector? = null,
)

/**
 * A screen, or the part of one under its bar, that says one thing: nothing here yet, something went wrong,
 * or a gate to pass first. An icon in a pill, a [headline], the [body], and up to two actions.
 *
 * [actions] is for a message with more than two ways out, or a destructive one: when set it replaces the
 * [primary]/[secondary] row and stacks full width under the body, at the same gap.
 *
 * Both apps render their loading, empty and error arms through this, so they read alike; each maps its own
 * error model onto the copy. [announce] makes the whole message a polite live region, for a failure that
 * replaced content the user was reading. It centres clear of a floating navigation bar.
 */
@Composable
fun MessageScreen(
    body: String,
    modifier: Modifier = Modifier,
    headline: String? = null,
    icon: ImageVector? = null,
    primary: ScreenAction? = null,
    secondary: ScreenAction? = null,
    announce: Boolean = false,
    actions: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(navOverlayPadding()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = dimensionResource(R.dimen.content_max_width))
                    .padding(horizontal = dimensionResource(R.dimen.message_screen_padding_h))
                    .then(if (announce) Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite } else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            icon?.let {
                MessageIcon(it)
                Spacer(Modifier.height(dimensionResource(R.dimen.message_screen_icon_gap)))
            }
            headline?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_s)))
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (actions != null) {
                Spacer(Modifier.height(dimensionResource(R.dimen.message_screen_action_gap)))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = actions,
                )
            } else if (primary != null || secondary != null) {
                Spacer(Modifier.height(dimensionResource(R.dimen.message_screen_action_gap)))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    secondary?.let { BingeTextButton(label = it.label, onClick = it.onClick, leadingIcon = it.leadingIcon) }
                    primary?.let { BingeFilledButton(label = it.label, onClick = it.onClick, leadingIcon = it.leadingIcon) }
                }
            }
        }
    }
}

/** The frame while a screen loads: the indicator alone, centred where a [MessageScreen] would sit. */
@Composable
fun LoadingMessageScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(navOverlayPadding()), contentAlignment = Alignment.Center) {
        BingeLoadingIndicator()
    }
}

@Composable
private fun MessageIcon(icon: ImageVector) {
    Box(
        modifier =
            Modifier
                .size(dimensionResource(R.dimen.state_icon_container_size))
                .clip(BingeShapes.Pill)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.message_screen_icon_size)),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

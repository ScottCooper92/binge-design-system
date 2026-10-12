package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.template.ScreenAction
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.component.TvMessagePlate
import com.binge.designsystem.tv.focus.TvFocusSink
import com.binge.designsystem.tv.nav.LocalTvRailEntry
import com.binge.designsystem.tv.theme.TvButtonStyle

/** An [action] as a TV button: its label, icon and modifier, with [modifier] added for the page's own focus wiring. */
@Composable
internal fun TvActionButton(
    action: ScreenAction,
    style: TvButtonStyle,
    modifier: Modifier = Modifier,
    initiallyFocused: Boolean = false,
) {
    TvButton(
        label = action.label,
        onClick = action.onClick,
        modifier = modifier.then(action.modifier),
        style = style,
        leadingIcon = action.leadingIcon,
        initiallyFocused = initiallyFocused,
    )
}

/**
 * A whole page that says one thing: a gate, a first-run blocker, a screen-wide failure, or the frame shown
 * while a page loads. A centred [TvMessagePlate] with up to two actions.
 *
 * Focus arrives on [primary] as [hosting] places it. With no action there is nothing to land on, so the page
 * holds a [TvFocusSink] and focus cannot fall through to whatever was behind it. [loading] shows the body
 * alone with no actions. The plate is centred and reserves nothing, so when the real message replaces it, an
 * icon, a headline or an action row that arrives moves the copy. A page that resolves to its own content, not
 * to a message, never sees that.
 *
 * [announce] makes the plate one polite live region carrying the headline and body, as `TvErrorPlate` is and the
 * phone's `MessageScreen` can be. Set it for a screen-wide failure: otherwise a screen reader hears only the focused
 * button, or, with no action, nothing at all (#596).
 *
 * Two shapes differ from the phone's `MessageScreen` on purpose. [loading] is a flag here, not a separate page,
 * because the page must keep holding focus while it loads. And there is no free `actions` slot: focus has to land
 * on a known [primary], so a TV message offers at most the two.
 */
@Composable
fun TvMessagePage(
    body: String,
    modifier: Modifier = Modifier,
    hosting: TvPageHosting = currentTvPageHosting(),
    headline: String? = null,
    icon: ImageVector? = null,
    primary: ScreenAction? = null,
    secondary: ScreenAction? = null,
    loading: Boolean = false,
    announce: Boolean = false,
) {
    val entry = remember { FocusRequester() }
    val sinkEntry = remember { FocusRequester() }
    val hasAction = !loading && primary != null
    val arrival =
        tvPageArrival(
            hosting = hosting,
            entry = if (hasAction) entry else sinkEntry,
            enabled = true,
            key = if (hasAction) primary.label else null,
        )
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .then(arrival)
                .padding(tvPagePadding(hosting)),
    ) {
        // In a rail destination the sink is all there is to focus, so the key toward the rail opens it (#368).
        val railEntry = LocalTvRailEntry.current.takeIf { hosting == TvPageHosting.RailDestination }
        if (!hasAction) Box(Modifier.focusRequester(sinkEntry)) { TvFocusSink(startEntry = railEntry) }
        TvMessagePlate(
            body = body,
            headline = headline,
            icon = icon,
            alignment = Alignment.Center,
            announce = announce,
            actions =
                if (hasAction) {
                    {
                        TvActionButton(primary, TvButtonStyle.Primary, Modifier.focusRequester(entry))
                        secondary?.let { TvActionButton(it, TvButtonStyle.Secondary) }
                    }
                } else {
                    null
                },
        )
    }
}

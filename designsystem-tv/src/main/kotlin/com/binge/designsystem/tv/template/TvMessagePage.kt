package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.component.TvMessagePlate
import com.binge.designsystem.tv.focus.TvFocusSink
import com.binge.designsystem.tv.theme.TvButtonStyle

/** One way out of a [TvMessagePage]: what the button says, and what pressing it does. */
@Immutable
data class TvPageAction(
    val label: String,
    val onClick: () -> Unit,
)

/**
 * A whole page that says one thing: a gate, a first-run blocker, a screen-wide failure, or the frame shown
 * while a page loads. A centred [TvMessagePlate] with up to two actions.
 *
 * Focus arrives on [primary] as [hosting] places it. With no action there is nothing to land on, so the page
 * holds a [TvFocusSink] and focus cannot fall through to whatever was behind it. [loading] shows the body
 * alone with no actions — the same plate, so the swap to the real message does not reflow the page.
 */
@Composable
fun TvMessagePage(
    body: String,
    modifier: Modifier = Modifier,
    hosting: TvPageHosting = currentTvPageHosting(),
    headline: String? = null,
    icon: ImageVector? = null,
    primary: TvPageAction? = null,
    secondary: TvPageAction? = null,
    loading: Boolean = false,
) {
    val entry = remember { FocusRequester() }
    val hasAction = !loading && primary != null
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .then(tvPageArrival(hosting, entry.takeIf { hasAction }, enabled = hasAction, key = primary?.label))
                .padding(tvPagePadding(hosting)),
    ) {
        if (!hasAction) TvFocusSink()
        TvMessagePlate(
            body = body,
            headline = headline,
            icon = icon,
            alignment = Alignment.Center,
            actions =
                if (hasAction && primary != null) {
                    {
                        TvButton(
                            label = primary.label,
                            onClick = primary.onClick,
                            modifier = Modifier.focusRequester(entry),
                            style = TvButtonStyle.Primary,
                        )
                        secondary?.let {
                            TvButton(label = it.label, onClick = it.onClick, style = TvButtonStyle.Secondary)
                        }
                    }
                } else {
                    null
                },
        )
    }
}

package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * The one anatomy a TV surface uses to say something went wrong, or that there is nothing here — an optional
 * headline, a body, and an optional row of actions, laid out for a ten-foot read.
 *
 * [headline] is optional because an *empty* state is often a single sentence and inventing a headline over it
 * would be writing copy to satisfy a signature. A **failure** always has one — a failure plate derives it from
 * the error's kind and never omits it — so the headline stays mandatory where it carries meaning.
 *
 * [icon] is the art above the copy. Optional and null by default, so a caller with nothing meaningful
 * to draw renders exactly what it did before rather than a shrug glyph. Art above short copy reads well at ten feet,
 * and this is the one anatomy every TV empty and failure state goes through.
 *
 * The actions are pinned: when the headline and body are taller than the plate (a long message at a large font), they scroll
 * above the actions, and the actions stay on screen. With room, or in a container that gives the plate no height, nothing is
 * scrolled and the plate is laid out as before.
 *
 * [announce] makes the whole plate a polite live region, merged, as `MessageScreen`'s is on the phone: for a failure
 * that replaced what the user was reading, so a screen reader says the headline and body and not only the focused button.
 */
@Composable
fun TvMessagePlate(
    body: String,
    modifier: Modifier = Modifier,
    headline: String? = null,
    icon: ImageVector? = null,
    alignment: Alignment = Alignment.TopStart,
    announce: Boolean = false,
    actions: @Composable (() -> Unit)? = null,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize(), contentAlignment = alignment) {
        PlateContent(
            headline = headline,
            body = body,
            icon = icon,
            announce = announce,
            actions = actions,
            bounded = constraints.hasBoundedHeight,
        )
    }
}

/**
 * Placement is the container's business (hence [TvMessagePlate.alignment]): a full-screen failure centres —
 * a message in the corner of a ten-foot panel reads as a broken render — while a plate inside a pane sits
 * `TopStart`, so it stays with the band it belongs to.
 */
@Composable
private fun PlateContent(
    headline: String?,
    body: String,
    icon: ImageVector?,
    announce: Boolean,
    actions: @Composable (() -> Unit)?,
    bounded: Boolean,
) {
    // Centred on its own axis: one short block of text under a piece of art, with no
    // second column for the eye to track back to. The same reasoning that makes centred copy wrong for a list
    // makes it right here.
    Column(
        modifier = Modifier
            .padding(dimensionResource(TvR.dimen.tv_focus_ring_bleed))
            .widthIn(max = dimensionResource(TvR.dimen.tv_message_plate_max_width))
            .then(if (announce) Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite } else Modifier),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // The copy takes the height the actions leave and scrolls when it is taller, so the actions are always on screen:
        // at a large font the art, headline and body can outgrow a panel. Where there is room it is its natural height and
        // looks as it always did. In a container with no height to give (a scrolling pane) it is not scrolled.
        Column(
            modifier = if (bounded) Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()) else Modifier,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // The settings pane's illustration treatment, shared rather than approximated ([TvIllustration]).
            icon?.let { TvIllustration(icon = it) }
            headline?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        actions?.let {
            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_sm)),
            ) {
                it()
            }
        }
    }
}

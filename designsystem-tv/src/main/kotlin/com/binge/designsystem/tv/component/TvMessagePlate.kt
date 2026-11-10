package com.binge.designsystem.tv.component

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvFocusGroup
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.tv.focus.tvFocusTarget
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
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
 * When the copy does overflow it takes focus, so a remote can read it: Up from the actions moves into it, Down and Up scroll it
 * under a focus ring, and a press at either end moves on out of it. Down from the end returns to the action focus came up from. A plate whose copy fits has no extra focus stop.
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
    Box(modifier = modifier.fillMaxSize(), contentAlignment = alignment) {
        PlateContent(headline = headline, body = body, icon = icon, announce = announce, actions = actions)
    }
}

/**
 * Placement is the container's business (hence [TvMessagePlate.alignment]): a full-screen failure centres —
 * a message in the corner of a ten-foot panel reads as a broken render — while a plate inside a pane sits
 * `TopStart`, so it stays with the band it belongs to.
 *
 * The copy and the actions are laid out by a plain [Layout], not by `BoxWithConstraints` and not by a weighted `Column`: a
 * plate sits under lazy items and under intrinsic-size parents (`height(IntrinsicSize.Min)`), and a subcomposing layout throws
 * when one of those asks it for an intrinsic size. This one measures the actions first and gives the copy the height they leave.
 */
@Composable
private fun PlateContent(
    headline: String?,
    body: String,
    icon: ImageVector?,
    announce: Boolean,
    actions: @Composable (() -> Unit)?,
) {
    val spacing = with(LocalDensity.current) { dimensionResource(DesR.dimen.padding_m).roundToPx() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var copyFocused by remember { mutableStateOf(false) }
    // The scroll state reports no limit until it has been measured; only a real, positive one makes the copy a focus stop.
    val overflows by remember { derivedStateOf { scrollState.maxValue in 1 until Int.MAX_VALUE } }
    // Centred on its own axis: one short block of text under a piece of art, with no
    // second column for the eye to track back to. The same reasoning that makes centred copy wrong for a list
    // makes it right here.
    Layout(
        modifier = Modifier
            .padding(dimensionResource(TvR.dimen.tv_focus_ring_bleed))
            .widthIn(max = dimensionResource(TvR.dimen.tv_message_plate_max_width))
            .then(if (announce) Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite } else Modifier),
        content = {
            // The copy takes the height the actions leave and scrolls when it is taller, so the actions are always on screen:
            // at a large font the art, headline and body can outgrow a panel. Where there is room it is its natural height and
            // looks as it always did.
            Column(
                modifier = Modifier
                    .then(
                        if (overflows) {
                            Modifier
                                .onKeyEvent { event ->
                                    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                                    val down = event.key == Key.DirectionDown
                                    val up = event.key == Key.DirectionUp
                                    val canScroll = (down && scrollState.canScrollForward) || (up && scrollState.canScrollBackward)
                                    if (canScroll) {
                                        val step = scrollState.viewportSize / 2f
                                        scope.launch { scrollState.animateScrollBy(if (down) step else -step) }
                                    }
                                    canScroll
                                }.tvFocusTarget { copyFocused = it }
                                .tvFocusIndicator(isFocused = copyFocused, shape = BingeShapes.MediaCard)
                        } else {
                            Modifier
                        },
                    ).verticalScroll(scrollState),
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
                    // A focus group that restores, so focus coming back from the copy lands on the action it left, not the nearest one.
                    modifier = Modifier.tvFocusGroup(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_sm)),
                ) {
                    it()
                }
            }
        },
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val actionsPlaceable = measurables.getOrNull(1)?.measure(loose)
        val used = if (actionsPlaceable == null) 0 else actionsPlaceable.height + spacing
        // With no height to give (a scrolling pane) the copy is offered a limit no content reaches, so it is its natural height
        // and a scroll container is never measured with an infinite one.
        val room = if (constraints.hasBoundedHeight) (constraints.maxHeight - used).coerceAtLeast(0) else UNBOUNDED_COPY_PX
        val copy = measurables[0].measure(loose.copy(maxHeight = room))
        val width = constraints.constrainWidth(maxOf(copy.width, actionsPlaceable?.width ?: 0))
        val height = constraints.constrainHeight(copy.height + used)
        layout(width, height) {
            copy.placeRelative(centred(width, copy.width), 0)
            actionsPlaceable?.placeRelative(centred(width, actionsPlaceable.width), copy.height + spacing)
        }
    }
}

/** The offset that centres [size] in [space], rounding half up as `Alignment.CenterHorizontally` does, so frames do not move. */
private fun centred(space: Int, size: Int): Int = ((space - size) / 2f).roundToInt()

/** A copy height no real content reaches, offered where the plate has no height to give. */
private const val UNBOUNDED_COPY_PX = 16383

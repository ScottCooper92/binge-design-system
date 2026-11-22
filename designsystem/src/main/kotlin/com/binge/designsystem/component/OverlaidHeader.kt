package com.binge.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeTheme

private enum class OverlaidHeaderSlot { Header, Content }

/**
 * A pinned [header] drawn over full-height [content], with the content running to the top of the window
 * and dissolving under the header through a short scrim.
 *
 * The alternative — stacking header above content in a `Column` — puts the content's top edge wherever
 * the header ends, so rows crop hard against it and nothing bleeds under the status bar.
 *
 * [content] is handed the inset the overlay occupies. A lazy child folds it into its own
 * `contentPadding`, so its first row starts below the header while later rows travel beneath; a child
 * that does not scroll applies it as padding, or it sits behind the header.
 *
 * **Subcomposed rather than measured into state.** The inset has to be known *before* the content is
 * composed, because it feeds a lazy list's `contentPadding` and so changes how that list measures.
 * Reporting the header's height through `onSizeChanged` would supply it a frame late: on device that
 * self-corrects too fast to see, but a screenshot renders one frame and captures the content sitting
 * under the header. Subcomposing measures the header and composes the content in the same pass.
 *
 * Window insets are deliberately not applied here — a screen nested in a scaffold has had them handled
 * already, and one that owns its window puts them in [header].
 *
 * [headerBackground] fills behind the header and is the colour its dissolve ramps from, so
 * [Color.Transparent] leaves the header with no ground of its own and both become no-ops — content
 * then passes visibly behind it rather than under an opaque band.
 *
 * [scrimFraction] is the alternative to that ground: a [TopBarScrim] spanning the whole header, so a
 * screen whose top bar is also transparent gets **one** ramp over bar and header together rather than
 * a scrim for the bar and a band for the header meeting at a seam. Ramp it with the bar's collapse —
 * at rest the content starts below the header and a resting scrim would paint the band it removes.
 * [scrimColor] defaults to a theme-following colour (`MaterialTheme.colorScheme.background`), same as
 * [BingeTopBar]/[BingeMediumTopBar] — every current caller overlays a plain, predictable surface.
 * Override to [BingeTheme.colors.scrim] only for a header floating over genuinely unpredictable
 * content, the way [DetailOverlayTopBar] does over a hero.
 *
 * @need pin a header over content that scrolls beneath it
 * @group bars
 * @replaces a hand-built Box with a scrim under a pinned header
 */
@Composable
fun OverlaidHeaderContent(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    headerBackground: Color = MaterialTheme.colorScheme.background,
    scrimFraction: Float = 0f,
    scrimColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable (contentPadding: PaddingValues) -> Unit,
) {
    val background = headerBackground
    val scrimHeight = dimensionResource(R.dimen.filter_pager_scrim_height)
    SubcomposeLayout(modifier) { constraints ->
        val headerPlaceables =
            subcompose(OverlaidHeaderSlot.Header) {
                Box {
                    TopBarScrim(scrimFraction, scrimColor = scrimColor, solidFadeHeight = scrimHeight)
                    Column {
                        Column(modifier = Modifier.fillMaxWidth().background(background)) { header() }
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(scrimHeight)
                                .background(Brush.verticalGradient(listOf(background, Color.Transparent))),
                        )
                    }
                }
            }.map { it.measure(constraints.copy(minHeight = 0)) }
        val headerHeight = headerPlaceables.maxOfOrNull { it.height } ?: 0

        val contentPlaceables =
            subcompose(OverlaidHeaderSlot.Content) {
                content(PaddingValues(top = headerHeight.toDp()))
            }.map { it.measure(constraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceables.forEach { it.place(0, 0) }
            // After the content, so the header sits over whatever scrolls beneath it.
            headerPlaceables.forEach { it.place(0, 0) }
        }
    }
}

/**
 * The scrim fraction a pager draws behind its header: [scrimFraction] as it is when the caller passes one, and otherwise a fade that follows
 * whether the page that is shown is [scrolled] off its top, so the scrim is up on a page that is already scrolled as soon as it is shown.
 * Shared by [BingeTabbedPager] and [BingeFilterChipPager], so the two fade alike.
 */
@Composable
internal fun rememberPagerScrim(scrimFraction: Float?, scrolled: Boolean): Float {
    val followed = rememberScrolledScrim(scrimFraction == null && scrolled)
    val scrim = scrimFraction ?: followed
    LocalPagerScrimProbe.current?.invoke(scrim)
    return scrim
}

/** The scrim fade every header shares: up when [scrolled], down otherwise, over [PAGER_SCRIM_FADE_MILLIS]. */
@Composable
internal fun rememberScrolledScrim(scrolled: Boolean): Float {
    val scrim by animateFloatAsState(
        targetValue = if (scrolled) 1f else 0f,
        animationSpec = tween(durationMillis = PAGER_SCRIM_FADE_MILLIS),
        label = "pager scrim",
    )
    return scrim
}

/** How long the scrim takes to fade in or out as the shown page leaves or returns to its top. */
private const val PAGER_SCRIM_FADE_MILLIS = 150

/** A test's way to read the scrim fraction a pager or a screen scaffold is drawing, which is otherwise only a colour in the header; null in production. */
internal val LocalPagerScrimProbe = compositionLocalOf<((Float) -> Unit)?> { null }

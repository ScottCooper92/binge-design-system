package com.binge.designsystem.tv.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.DISABLED_ALPHA

/**
 * The role a TV control surface plays, which fixes its resting colours via [tvButtonColors].
 *
 * Two roles are red, for two kinds of danger. [Destructive] is an irreversible action (remove, delete, sign out): a
 * solid `error` fill at rest, and a light border added when focused, since the fill is already the colour focus would
 * bring. [DestructiveOutline] is a reversible one (block): an `error` outline and label at rest, filling solid `error`
 * when focused as every other style fills with the accent. Both stay red under focus, because a dangerous action is at
 * its most dangerous the instant before OK.
 *
 * [Text] and [TextDestructive] are a dialog's answers: a label alone at rest, with no fill and no edge, filling with the
 * accent, or with `error`, under focus. The dialog's own panel is the shape, so the answers need none of their own.
 */
enum class TvButtonStyle {
    Primary,
    Secondary,
    Destructive,
    DestructiveOutline,
    Text,
    TextDestructive,
}

/** Container / content / border for a TV control in one state — focus included, see [tvButtonColors]. */
@Immutable
data class TvButtonColors(
    val container: Color,
    val content: Color,
    val border: Color,
)

/**
 * The TV accent model in one place — see `docs/tv-foundation.md`.
 *
 * **On a button, the accent means focus and nothing else.** No control carries the accent at rest; a focused
 * one fills solid with the accent and dark content. That is tv-material's own behaviour, and it keeps the accent
 * from having to mean "primary" and "focused" at once — the ambiguity that cost this model two rewrites.
 *
 * **Emphasis is shape, not colour.** Following the platform's button hierarchy, a
 * [TvButtonStyle.Primary] rests as a *filled* neutral (the higher-emphasis form) and everything else
 * rests as an *outline* — so a screen still reads as one prominent action among quieter ones, in greys,
 * and the accent appears only under the user's focus.
 *
 * Two exceptions, both deliberate:
 * - The two red styles keep their hue **at rest**, because danger is a property of the action rather than of where
 *   the remote happens to be. [TvButtonStyle.Destructive] rests as a solid `error` fill and shows focus with a light
 *   `onError` border; [TvButtonStyle.DestructiveOutline] rests as an `error` outline and fills solid `error` when focused.
 * - A text style ([TvButtonStyle.Text], [TvButtonStyle.TextDestructive]) has no edge at all, enabled or not: it sits on
 *   a dialog's panel, which is the shape. Disabled, it is its dimmed label alone.
 * - A **disabled** control never takes the focus fill. It stays focusable so ↓ isn't a dead end, but a
 *   filled disabled button would promise a press that does nothing.
 *
 * ## A disabled control is an outline, and it must not depend on what it sits on
 *
 * Disabled used to be `container = colors.surface` with `border = Color.Transparent`. That reads as a pill on
 * the *component's* own preview background — and as **bare text** on any panel, because TV panels are backed by
 * `colors.surface` too, so the container composited to nothing and there was no outline underneath it. The
 * control stopped reading as a control at all. Found in the baselines, where the same button appears as a
 * filled pill in one frame and as centred text in the next.
 *
 * So disabled is now `container = Color.Transparent` with a **dimmed border**: the shape is drawn rather than
 * filled, which is the only form that survives every backdrop. Picking a different container token was rejected
 * — a single screen can use `surface` *and* `surfaceVariant` as backgrounds, so no
 * opaque token is safe against the surface it is drawn on.
 *
 * Disabled and an enabled [TvButtonStyle.Secondary] are therefore both outlines, separated by border alpha and
 * content colour (`onSurfaceVariant` against `onSurface`) rather than by shape. That separation is the part of
 * this worth checking on a baseline rather than in code; if it proves too weak at ten feet the fallback is
 * content-only with no border.
 */
@Composable
fun tvButtonColors(
    style: TvButtonStyle,
    enabled: Boolean,
    isFocused: Boolean,
): TvButtonColors {
    val colors = MaterialTheme.colorScheme
    val focusFilled = isFocused && enabled
    val solidRed = enabled && style == TvButtonStyle.Destructive
    val outlineRed = enabled && (style == TvButtonStyle.DestructiveOutline || style == TvButtonStyle.TextDestructive)
    val red = solidRed || outlineRed
    val text = style == TvButtonStyle.Text || style == TvButtonStyle.TextDestructive
    val filledAtRest = enabled && !isFocused && style == TvButtonStyle.Primary
    return TvButtonColors(
        container = when {
            solidRed -> colors.error
            focusFilled -> if (red) colors.error else colors.primary
            // Transparent, not `surface` — a disabled control is drawn as an outline so its shape cannot be
            // erased by a panel that happens to use the same token. See the KDoc for what that cost.
            !enabled -> Color.Transparent
            // The neutral fill that marks the primary. Nothing else rests filled in neutral, so "filled" never
            // reads as "ready" on a control that isn't.
            filledAtRest -> colors.surfaceVariant
            else -> Color.Transparent
        },
        content = when {
            red && (focusFilled || solidRed) -> colors.onError
            focusFilled -> colors.onPrimary
            !enabled -> colors.onSurfaceVariant
            outlineRed -> colors.error
            else -> colors.onSurface
        },
        border = when {
            // The solid style's fill is the same at rest and focused, so the border is what says it has focus.
            solidRed -> if (isFocused) colors.onError else Color.Transparent
            focusFilled || filledAtRest || text -> Color.Transparent
            // The disabled control's only visible edge, so it is dimmed rather than removed: the line that keeps it a
            // button on any panel. Applied to `border`, which is opaque, so the result is not a function of the backdrop.
            !enabled -> colors.border.copy(alpha = DISABLED_ALPHA)
            outlineRed -> colors.error
            else -> colors.border
        },
    )
}

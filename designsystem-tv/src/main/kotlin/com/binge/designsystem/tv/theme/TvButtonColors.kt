package com.binge.designsystem.tv.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme

/**
 * The role a TV control surface plays, which fixes its resting colours via [tvButtonColors].
 *
 * [Destructive] marks an irreversible action (sign out): a red-tinted container and red border at rest, and —
 * because the accent model makes the *fill* the focus channel while the hue stays the role — a solid **red** fill when
 * focused, where every other style fills amber. A dangerous action is at its most dangerous the instant before
 * OK, which is precisely when it is focused.
 */
enum class TvButtonStyle { Primary, Secondary, Destructive }

/**
 * A Destructive container at rest is its error colour at low alpha — a warning wash. Focused, it goes
 * to the solid error fill.
 */
private const val DESTRUCTIVE_CONTAINER_ALPHA = 0.16f

/**
 * A disabled outline is the border dimmed rather than a second border token, so the shape survives on any
 * background while still reading as quieter than an enabled Secondary — see [tvButtonColors]. Applied to
 * `border`, which is opaque, so the result is deterministic and not a function of the backdrop.
 */
private const val DISABLED_BORDER_ALPHA = 0.38f

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
 * **On a button, amber means focus and nothing else.** No control carries the accent at rest; a focused
 * one fills solid amber with dark content. That is tv-material's own behaviour, and it keeps the accent
 * from having to mean "primary" and "focused" at once — the ambiguity that cost this model two rewrites.
 *
 * **Emphasis is shape, not colour.** Following the platform's button hierarchy, a
 * [TvButtonStyle.Primary] rests as a *filled* neutral (the higher-emphasis form) and everything else
 * rests as an *outline* — so a screen still reads as one prominent action among quieter ones, in greys,
 * and the amber appears only under the user's focus.
 *
 * Two exceptions, both deliberate:
 * - [TvButtonStyle.Destructive] keeps a red wash and red label **at rest**, because danger is a property
 *   of the action rather than of where the remote happens to be, and fills solid `error` when focused.
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
 * — `TvAccountSignedOut` already uses `surface` *and* `surfaceVariant` as backgrounds within one file, so no
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
    val destructive = style == TvButtonStyle.Destructive
    val filledAtRest = enabled && !isFocused && style == TvButtonStyle.Primary
    val destructiveAtRest = enabled && !isFocused && destructive
    return TvButtonColors(
        container = when {
            focusFilled -> if (destructive) colors.error else colors.primary
            // Transparent, not `surface` — a disabled control is drawn as an outline so its shape cannot be
            // erased by a panel that happens to use the same token. See the KDoc for what that cost.
            !enabled -> Color.Transparent
            destructiveAtRest -> colors.error.copy(alpha = DESTRUCTIVE_CONTAINER_ALPHA)
            // The neutral fill that marks the primary. Nothing else rests filled in neutral, so "filled" never
            // reads as "ready" on a control that isn't.
            filledAtRest -> colors.surfaceVariant
            else -> Color.Transparent
        },
        content = when {
            focusFilled -> if (destructive) colors.onError else colors.onPrimary
            !enabled -> colors.onSurfaceVariant
            destructiveAtRest -> colors.error
            else -> colors.onSurface
        },
        border = when {
            focusFilled || filledAtRest -> Color.Transparent
            // The disabled control's only visible edge, so it is dimmed rather than removed — this is the line
            // that keeps a disabled button a button on a surface-backed panel.
            !enabled -> colors.border.copy(alpha = DISABLED_BORDER_ALPHA)
            destructiveAtRest -> colors.error
            else -> colors.border
        },
    )
}

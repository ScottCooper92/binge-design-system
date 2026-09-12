package com.binge.designsystem.preview

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper

/*
 * Shared multipreview annotations for previews and Compose screenshot tests. Stack one with
 * `@PreviewTest` (screenshotTest source set) on a `@Composable` to expand into one screenshot per
 * cell; without `@PreviewTest` they still drive the Studio preview pane. [ScreenPreviews] covers full
 * screens (one cell per device class, dark-default), [ComponentPreviews] wrap-content atoms (light +
 * dark), [FontScalePreviews] adds the font-scale axis.
 *
 * Each carries [ScreenshotTheme] via `@PreviewWrapper`, so a preview does not wrap it by hand and cannot
 * forget to: the `uiMode` axis switches light/dark and colours stay deterministic either way. Wrapping
 * anyway is harmless — the nested theme resolves to the same colours — so the manual calls still in the
 * tree render identically and can be removed as they are touched.
 *
 * Keep preview `name`s compact/space-free (e.g. `phone-land`): the plugin bakes the name into each
 * reference filename, and long paths blow past Windows' 260-char MAX_PATH in worktree temp dirs (#298).
 */

/** Standard phone — the de-facto Pixel-class width the wrap-content previews render at. */
const val STANDARD_PHONE_WIDTH_DP = 412

/**
 * The workhorse for **wrap-content** components — chips, buttons, cards, rows, dialogs. Light + dark
 * at the standard phone width; height wraps. Only the theme axis matters: a component's reflow at 360
 * vs 480 isn't a distinct layout branch.
 */
@PreviewWrapper(ScreenshotThemeWrapper::class)
@Preview(name = "light", widthDp = STANDARD_PHONE_WIDTH_DP, uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "dark", widthDp = STANDARD_PHONE_WIDTH_DP, uiMode = UI_MODE_NIGHT_YES)
annotation class ComponentPreviews

/**
 * Default and large (1.5×) font scale, light + dark, at the standard phone width.
 * Use for text-dense surfaces (account, detail screens, onboarding analytics) to catch
 * wrap/overflow regressions.
 */
@PreviewWrapper(ScreenshotThemeWrapper::class)
@Preview(name = "font10-light", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 1.0f, uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "font15-light", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 1.5f, uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "font10-dark", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 1.0f, uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "font15-dark", widthDp = STANDARD_PHONE_WIDTH_DP, fontScale = 1.5f, uiMode = UI_MODE_NIGHT_YES)
annotation class FontScalePreviews

/**
 * The locale axis, at the standard phone width, dark: one cell per locale that ships **other than
 * the source locale**. English is not a cell here because every surface that takes this annotation
 * also takes [FontScalePreviews], whose `font10-dark` cell is the same window, theme and locale — the
 * two rendered byte-identical on all six surfaces when measured, so an `en` cell would be a second
 * baseline for one frame.
 *
 * Stack on **text-dense** surfaces where the copy drives the layout — the same places
 * [FontScalePreviews] is warranted. Spanish runs ~25% longer than English, and this app is full of
 * one-line top bars, chip rows and stat sub-labels that have no room to give.
 *
 * **There is deliberately no pseudolocale cell.** `locale = "en-rXA"` renders byte-identical to
 * `en` here — measured, not assumed — because AAPT2 only generates the pseudolocale for a variant
 * whose build type asks for it, and doing that on the library build type does not reach the
 * screenshot renderer either. A cell that silently falls back to English is worse than no cell:
 * it reads as expansion coverage while asserting nothing. `isPseudoLocalesEnabled` is set on the
 * app's debug build type, so en-XA is reachable on-device through the system language picker —
 * which is where it currently has to be exercised.
 */
@PreviewWrapper(ScreenshotThemeWrapper::class)
@Preview(name = "es", widthDp = STANDARD_PHONE_WIDTH_DP, locale = "es", uiMode = UI_MODE_NIGHT_YES)
annotation class LocalePreviews

/**
 * Full-screen matrix: one cell per **device class** (phone / foldable / tablet), dark by default
 * (Binge is dark-first) with a single light spot-check. Stack on a **fillMaxSize** content preview so
 * the device spec — not a fixed frame — drives the layout.
 *
 * | cell          | window        | class / tier                              | theme |
 * |---------------|---------------|-------------------------------------------|-------|
 * | `phone`       | 411×891 port  | phone, compact                            | dark  |
 * | `phone-light` | 411×891 port  | phone, compact                            | light |
 * | `phone-land`  | 891×411 land  | phone landscape — short-height stress     | dark  |
 * | `foldable`    | 840×1180 port | unfolded foldable — expanded portrait     | dark  |
 * | `tablet`      | 1280×800 land | tablet — expanded landscape               | dark  |
 *
 * `phone-land`, `foldable`, and `tablet` all cross the ≥840dp two-pane breakpoint at different
 * shapes: short-landscape stresses vertical space, foldable is tall-portrait, tablet is wide.
 * A hardcoded `darkTheme = …` inside the content still pins every cell to one theme.
 */
@PreviewWrapper(ScreenshotThemeWrapper::class)
@Preview(name = "phone", device = "spec:width=411dp,height=891dp,orientation=portrait", uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "phone-light", device = "spec:width=411dp,height=891dp,orientation=portrait", uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "phone-land", device = "spec:width=411dp,height=891dp,orientation=landscape", uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "foldable", device = "spec:width=840dp,height=1180dp,orientation=portrait", uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "tablet", device = "spec:width=800dp,height=1280dp,orientation=landscape", uiMode = UI_MODE_NIGHT_YES)
annotation class ScreenPreviews

/**
 * One cell — [ScreenPreviews]' `phone` cell, dark, byte-for-byte the same spec — for a **state** of a
 * screen whose layout frame already carries [ScreenPreviews].
 *
 * The matrix answers a layout question: does this screen reflow at the 840dp breakpoint, and how
 * does it sit in a short landscape window. That is asked once per layout, on the canonical frame. A
 * loading skeleton, an empty arm, an append footer, a cancelled badge or a request button changes
 * content *within* that layout, and its foldable and tablet cells re-answer a settled question at the
 * two most expensive render sizes in the suite. Measured on 2026-09-10: on the hubs, person detail
 * and the grids, those cells were byte-identical between state variants and their canonical frame.
 *
 * Same spec as the `phone` cell so converting a frame keeps its committed `phone` baseline — only the
 * other four PNGs go. A variant that genuinely changes the layout (a different step, a different cell
 * type in a grid, content sized to overflow the window) stays on [ScreenPreviews].
 */
@PreviewWrapper(ScreenshotThemeWrapper::class)
@Preview(name = "phone", device = "spec:width=411dp,height=891dp,orientation=portrait", uiMode = UI_MODE_NIGHT_YES)
annotation class ScreenStatePreview
